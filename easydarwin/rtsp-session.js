const net = require('net');
const event = require('events');
const fs = require('fs');
const shortid = require('shortid');
const url = require('url');
const path = require('path');
const crypto = require('crypto');
const rtpParser = require('./rtp-parser');
const BufferPool = require('./buffer-pool');
const utils = require('./utils');
const sdpParser = require('sdp-transform');
const getPort = require('get-port');
const dgram = require('dgram');
const cfg = require('./cfg');
const log4j = require('./log4j');
const logger = log4j.getLogger('console');

class RTSPRequest {
    constructor() {
        this.method = '';
        this.url = '';
        this.raw = '';
    }
}

class RTSPSession extends event.EventEmitter {

    constructor(socket, server) {
        super();
        this.type = '';
        this.url = '';
        this.path = '';
        this.aControl = '';
        this.vControl = '';
        this.pushSession = null;
        this.transType = 'tcp';
        this.vSeq = 0;
        this.vTime = 0;
        this.aSeq = 0;
        this.aTime = 0;

        //-- tcp trans params
        this.aRTPChannel = 4;
        this.aRTPControlChannel = 5;
        this.vRTPChannel = 0;
        this.vRTPControlChannel = 0;
        //-- tcp trans params end

        //-- udp trans params
        this.aRTPClientPort = 0;
        this.aRTPClientSocket = null;
        this.aRTPControlClientPort = 0;
        this.aRTPControlClientSocket = null;
        this.vRTPClientPort = 0;
        this.vRTPClientSocket = null;
        this.vRTPControlClientPort = 0;
        this.vRTPControlClientSocket = null;

        this.aRTPServerPort = 0;
        this.aRTPServerSocket = null;
        this.aRTPControlServerPort = 0;
        this.aRTPControlServerSocket = null;
        this.vRTPServerPort = 0;
        this.vRTPServerSocket = null;
        this.vRTPControlServerPort = 0;
        this.vRTPControlserverSokcet = null;
        //-- udp trans params end

        //-- sdp info
        this.sdp = null;
        this.sdpRaw = '';

        this.aCodec = '';
        this.aRate = '';
        this.aPayload = '';

        this.vCodec = '';
        this.vRate = '';
        this.vPayload = '';
        //-- sdp info end

        //-- stats info
        this.inBytes = 0;
        this.outBytes = 0;
        this.startAt = new Date();
        //-- stats info end

        this.sid = shortid.generate(); // session id
        this.socket = socket;
        this.host = this.socket.address().address;
        this.server = server;
        this.bp = new BufferPool(this.genHandleData());
        this.bp.init();
        this.gopCache = [];
        this.audioCache = [];
        this.vControlCache  = null;
        this.aControlCache  = null;
        this.lastVideoTimestamp = 0;
        this.lastAudioTimestamp = 0;
        this.newestAudioTimestamp = 0;
        this.newestAudioControlCache = null;
        this.limitSize = cfg.limitSize;
        this.startTimestamp = Math.floor(new Date().getTime()/1000);

        //设置超时时间
        this.socket.setTimeout(1000 * cfg.timeOutSecond,function() {
            logger.info('客户端在' + cfg.timeOutSecond + 's内未通信，将断开连接...');
        });

        this.socket.on("data", data => {
            //logger.info('playSession:'+this.server.playSessions.length);
            this.bp.push(data);
        }).on("close", (had_error) => {
            logger.info('stop:' + this.type + ':' + this.server.pushSessions[this.path] + ":had_error:" + had_error);
            //不断开设备推流socket，等待echoshow播放端断开20秒后自动断开设备连接
            //if(this.type == 'player' && this.server.pushSessions[this.path]){
            //    this.server.pushSessions[this.path].stop();
            //}
            if(this.bp){
                this.stop();
            }
        }).on("error", err => {
            logger.info("error event:" + err)
            this.stop();
        }).on("timeout", () => {
            this.stop();
        });

        this.on("request", this.handleRequest);
    }

    * genHandleData() {
        try{
            while (true) {
                if (this.bp.need(1)) {
                    if (yield) return;
                }
                let buf = this.bp.read(1);
                if (buf.readUInt8() == 0x24) { // rtp over tcp
                    if (this.bp.need(3)) {
                        if (yield) return;
                    }
                    buf = this.bp.read(3);
                    let channel = buf.readUInt8();
                    let rtpLen = buf.readUInt16BE(1);
                    if (this.bp.need(rtpLen)) {
                        if (yield) return;
                    }
                    let rtpBody = this.bp.read(rtpLen);
                    //logger.info('channel:' + channel + ':vRTPChannel:' + this.vRTPChannel + ':aRTPChannel:' + this.aRTPChannel);
                    if (channel == this.aRTPChannel) {
                        this.audioCache.push(rtpBody);
                        this.broadcastAudio(rtpBody);
                    } else if (channel == this.vRTPChannel) {
                        this.broadcastVideo(rtpBody);
                        if (this.vCodec.toUpperCase() == 'H264') {
                            let rtp = rtpParser.parseRtpPacket(rtpBody);
                            //logger.info(this.path + ':' + '<vedioTimestamp>' + rtp.timestamp + '<timestampDiff>' + (rtp.timestamp - this.lastVideoTimestamp));
                            if (rtpParser.isKeyframeStart(rtp.payload)) {
                                logger.info(`find key frame, current gop cache size[${this.gopCache.length}]`);
                                this.gopCache = [];
                                let audioCacheLength = this.audioCache.length;
                                if(audioCacheLength > 0){
                                    this.audioCache = [this.audioCache[audioCacheLength-1]];
                                    this.lastAudioTimestamp = rtpParser.parseRtpPacket(this.audioCache[0]).timestamp;
                                }
                                this.lastVideoTimestamp = rtp.timestamp;
                            }
                            this.gopCache.push(rtpBody);
                        }
                    } else if (channel == this.aRTPControlChannel) {
                        //logger.info(this.type + ':aRTPControl' + rtpBody.length);
                        let rtp = rtpParser.parseControlPacket(rtpBody);
                        logger.info('aControl:' + ':' + rtp.NTPtimestamp + ':' + rtp.RTPtimestamp);
                        if(this.aControlCache == null || this.audioCache == [] || rtp.RTPtimestamp <= this.lastAudioTimestamp){
                            this.aControlCache = rtpBody;
                        }else if(this.newestAudioTimestamp != 0 && this.newestAudioTimestamp <= this.lastAudioTimestamp){
                            this.aControlCache = this.newestAudioControlCache;
                        }
                        this.newestAudioTimestamp = rtp.RTPtimestamp;
                        this.newestAudioControlCache = rtpBody;
                        this.broadcastAudioControl(rtpBody);
                    } else if (channel == this.vRTPControlChannel) {
                        //logger.info(this.type + ':vRTPControl' + rtpBody.length);
                        let rtp = rtpParser.parseControlPacket(rtpBody);
                        logger.info('vControl:' + ':' + rtp.NTPtimestamp + ':' + rtp.RTPtimestamp);
                        if(this.vControlCache == null || this.gopCache == [] || rtp.RTPtimestamp <= this.lastVideoTimestamp){
                            this.vControlCache = rtpBody;
                        }
                        this.broadcastVideoControl(rtpBody);
                    }
                    this.inBytes += (rtpLen + 4);
                } else { // rtsp method
                    let reqBuf = Buffer.concat([buf], 1);
                    let readSize = 0;
                    while (reqBuf.toString().indexOf("\r\n\r\n") < 0) {
                        if(readSize++ > this.limitSize){
                            logger.info('客户端发送字节数超过限制：' + this.limitSize + ',断开socket');
                            this.stop();
                            return;
                        }
                        if (this.bp.need(1)) {
                            if (yield) return;
                        }
                        buf = this.bp.read(1);
                        reqBuf = Buffer.concat([reqBuf, buf], reqBuf.length + 1);
                    }
                    let req = this.parseRequestHeader(reqBuf.toString());
                    this.inBytes += reqBuf.length;
                    if (req['Content-Length']) {
                        let bodyLen = parseInt(req['Content-Length']);
                        if (this.bp.need(bodyLen)) {
                            if (yield) return;
                        }
                        this.inBytes += bodyLen;
                        buf = this.bp.read(bodyLen);
                        let bodyRaw = buf.toString();
                        if (req.method.toUpperCase() == 'ANNOUNCE') {
                            this.sdp = sdpParser.parse(bodyRaw);
                            // logger.info(JSON.stringify(this.sdp, null, 1));
                            this.sdpRaw = bodyRaw;
                            this.sdpRaw = this.sdpRaw.replace('audio 0 RTP/AVP 0\r\nb=AS:64','audio 0 RTP/AVP 97\r\na=rtpmap:97 PCMU/8000');
                            if (this.sdp && this.sdp.media && this.sdp.media.length > 0) {
                                for (let media of this.sdp.media) {
                                    if (media.type == 'video') {
                                        this.vControl = media.control;
                                        if (media.rtp && media.rtp.length > 0) {
                                            this.vCodec = media.rtp[0].codec;
                                            this.vRate = media.rtp[0].rate;
                                            this.vPayload = media.rtp[0].payload;
                                        }
                                    } else if (media.type == 'audio') {
                                        this.aControl = media.control;
                                        if (media.rtp && media.rtp.length > 0) {
                                            this.aCodec = media.rtp[0].codec;
                                            this.aRate = media.rtp[0].rate;
                                            this.aPayload = media.rtp[0].payload;
                                        }
                                    }
                                }
                            }
                        }
                        req.raw += bodyRaw;
                    }
                    this.emit('request', req);
                }
            }
        }catch (e) {
            this.stop();
            logger.info(e)
        }

    }

    /**
     *
     * @param {Object} opt
     * @param {Number} [opt.code=200]
     * @param {String} [opt.msg='OK']
     * @param {Object} [opt.headers={}]
     */
    makeResponseAndSend(opt = {}) {
        let def = { code: 200, msg: 'OK', headers: {} };
        opt = Object.assign({}, def, opt);
        let raw = `RTSP/1.0 ${opt.code} ${opt.msg}\r\n`;
        for (let key in opt.headers) {
            raw += `${key}: ${opt.headers[key]}\r\n`;
        }
        raw += `\r\n`;
        logger.info(`>>>>>>>>>>>>> response[${opt.method}] >>>>>>>>>>>>>`);
        logger.info(raw);
        this.socket.write(raw);
        this.outBytes += raw.length;
        if (opt.body) {
            logger.info(new String(opt.body).toString());
            this.socket.write(opt.body);
            this.outBytes += opt.body.length;
        }
        return raw;
    }

    parseRequestHeader(header = '') {
        let ret = new RTSPRequest();
        ret.raw = header;
        let lines = header.trim().split("\r\n");
        if (lines.length == 0) {
            return ret;
        }
        let line = lines[0];
        let items = line.split(/\s+/);
        ret.method = items[0];
        ret.url = items[1];
        for (let i = 1; i < lines.length; i++) {
            line = lines[i];
            items = line.split(/:\s+/);
            ret[items[0]] = items[1];
        }
        return ret;
    }

    /**
     *
     * @param {RTSPRequest} req
     */
    async handleRequest(req) {
        logger.info(`<<<<<<<<<<< request[${req.method}] <<<<<<<<<<<<<`);
        logger.info(req.raw);
        let pushSession;
        let res = {
            method: req.method,
            headers: {
                CSeq: req['CSeq'],
                Session: this.sid
            }
        };
        try{
            switch (req.method) {
                case 'OPTIONS':
                    res.headers['Public'] = "DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE, OPTIONS, ANNOUNCE, RECORD";
                    break;
                case 'ANNOUNCE':
                    this.type = 'pusher';
                    this.url = req.url;
                    let urlObject = url.parse(this.url);
                    //不满足请求的url则直接断开
                    let checkRes = this.checkUrlValid(urlObject);
                    if(!checkRes){
                        throw "not valid url";
                    }
                    this.path = urlObject.pathname;
                    pushSession = this.server.pushSessions[this.path];
                    if (pushSession) {
                        res.code = 406;
                        res.msg = 'Not Acceptable';
                    } else {
                        this.server.addSession(this);
                    }
                    break;
                case 'SETUP':
                    debugger;
                    let ts = req['Transport'] || "";
                    let control = req.url.substring(req.url.lastIndexOf('/') + 1);
                    if(ts.indexOf('unicast') == -1){
                        ts += ';unicast';
                    }
                    let mtcp = ts.match(/interleaved=(\d+)(-(\d+))?/);
                    let mudp = ts.match(/client_port=(\d+)(-(\d+))?/);
                    if (mtcp) {
                        this.transType = 'tcp';
                        if (control == this.vControl) {
                            this.vRTPChannel = parseInt(mtcp[1]) || 0;
                            this.vRTPControlChannel = parseInt(mtcp[3]) || 0;
                        }
                        if (control == this.aControl) {
                            this.aRTPChannel = parseInt(mtcp[1]) || 0;
                            this.aRTPControlChannel = parseInt(mtcp[3]) || 0;
                        }
                    } else if (mudp) {
                        this.transType = 'udp';
                        if (control == this.aControl) {
                            this.aRTPClientPort = parseInt(mudp[1]) || 0;
                            this.aRTPClientSocket = dgram.createSocket(this.getUDPType());
                            this.aRTPControlClientPort = parseInt(mudp[3]) || 0;
                            if(this.aRTPControlClientPort) {
                                this.aRTPControlClientSocket = dgram.createSocket(this.getUDPType());
                            }
                            if (this.type == 'pusher') {
                                this.aRTPServerPort = await getPort();
                                this.aRTPServerSocket = dgram.createSocket(this.getUDPType());
                                this.aRTPServerSocket.on('message', buf => {
                                    this.inBytes += buf.length;
                                    this.broadcastAudio(buf);
                                }).on('error', err => {
                                    logger.info(err);
                                })
                                await this.bindUDPPort(this.aRTPServerSocket, this.aRTPServerPort);
                                this.aRTPControlServerPort = await getPort();
                                this.aRTPControlServerSocket = dgram.createSocket(this.getUDPType());
                                this.aRTPControlServerSocket.on('message', buf => {
                                    this.inBytes += buf.length;
                                    this.broadcastAudioControl(buf);
                                }).on('error', err => {
                                    logger.info(err);
                                })
                                await this.bindUDPPort(this.aRTPControlServerSocket, this.aRTPControlServerPort);
                                ts = ts.split(';');
                                ts.splice(ts.indexOf(mudp[0]) + 1, 0, `server_port=${this.aRTPServerPort}-${this.aRTPControlServerPort}`);
                                ts = ts.join(';');
                            }
                        }
                        if (control == this.vControl) {
                            this.vRTPClientPort = parseInt(mudp[1]) || 0;
                            this.vRTPClientSocket = dgram.createSocket(this.getUDPType());
                            this.vRTPControlClientPort = parseInt(mudp[3]) || 0;
                            if(this.vRTPControlClientPort) {
                                this.vRTPControlClientSocket = dgram.createSocket(this.getUDPType());
                            }
                            if (this.type == 'pusher') {
                                this.vRTPServerPort = await getPort();
                                this.vRTPServerSocket = dgram.createSocket(this.getUDPType());
                                this.vRTPServerSocket.on('message', buf => {
                                    this.inBytes += buf.length;
                                    this.broadcastVideo(buf);
                                    if (this.vCodec.toUpperCase() == 'H264') {
                                        let rtp = rtpParser.parseRtpPacket(buf);
                                        if (rtpParser.isKeyframeStart(rtp.payload)) {
                                            // logger.info(`find key frame, current gop cache size[${this.gopCache.length}]`);
                                            this.gopCache = [];
                                        }
                                        this.gopCache.push(buf);
                                    }
                                }).on('error', err => {
                                    logger.info(err);
                                })
                                await this.bindUDPPort(this.vRTPServerSocket, this.vRTPServerPort);
                                this.vRTPControlServerPort = await getPort();
                                this.vRTPControlserverSokcet = dgram.createSocket(this.getUDPType());
                                this.vRTPControlserverSokcet.on('message', buf => {
                                    this.inBytes += buf.length;
                                    this.broadcastVideoControl(buf);
                                })
                                await this.bindUDPPort(this.vRTPControlserverSokcet, this.vRTPControlServerPort);
                                ts = ts.split(';');
                                ts.splice(ts.indexOf(mudp[0]) + 1, 0, `server_port=${this.vRTPServerPort}-${this.vRTPControlServerPort}`);
                                ts = ts.join(';');
                            }
                        }
                    }
                    res.headers['Transport'] = ts;
                    break;
                case 'DESCRIBE':
                    debugger;
                    this.type = 'player';
                    this.url = req.url;
                    //logger.info(req);
                    this.path = url.parse(this.url).pathname;
                    //logger.info(this.path);
                    pushSession = this.server.pushSessions[this.path];
                    if(pushSession && pushSession.sdpRaw && pushSession.sdpRaw.indexOf('a=range:')== -1){
                        let indexOfT = pushSession.sdpRaw.indexOf('t=0 0');
                        pushSession.sdpRaw = pushSession.sdpRaw.substring(0,indexOfT) +
                            "a=range:npt=now-\r\na=type:broadcast\r\na=control:*\r\n" +
                            pushSession.sdpRaw.substring(indexOfT);
                        indexOfT = pushSession.sdpRaw.indexOf('a=control:streamid=0');
                        pushSession.sdpRaw = pushSession.sdpRaw.substring(0,indexOfT) +                "b=AS:50\r\n" + pushSession.sdpRaw.substring(indexOfT);
                    }
                    if (pushSession && pushSession.sdpRaw) {
                        res.headers['Content-Length'] = pushSession.sdpRaw.length;
                        res.headers['Content-Type'] = 'application/sdp';
                        res.body = pushSession.sdpRaw;
                        this.sdp = pushSession.sdp;
                        this.sdpRaw = pushSession.sdpRaw;
                        this.pushSession = pushSession;
                        logger.info(this.sdp);
                        if (this.sdp && this.sdp.media && this.sdp.media.length > 0) {
                            for (let media of this.sdp.media) {
                                if (media.type == 'video') {
                                    this.vControl = media.control;
                                    if (media.rtp && media.rtp.length > 0) {
                                        this.vCodec = media.rtp[0].codec;
                                        this.vRate = media.rtp[0].rate;
                                        this.vPayload = media.rtp[0].payload;
                                    }
                                } else if (media.type == 'audio') {
                                    this.aControl = media.control;
                                    if (media.rtp && media.rtp.length > 0) {
                                        this.aCodec = media.rtp[0].codec;
                                        this.aRate = media.rtp[0].rate;
                                        this.aPayload = media.rtp[0].payload;
                                    }
                                }
                            }
                        }
                    } else {
                        res.code = 404;
                        res.msg = 'NOT FOUND';
                    }

                    break;
                case 'PLAY':
                    this.server.addSession(this);
                    if(req['Range'] === undefined){
                        req['Range'] = 'npt=now-';
                        res.headers['Range'] = 'npt=now-';
                    }
                    req['Range'] = 'npt=now-';
                    let temp_raw = req['raw'];
                    req['raw'] = temp_raw.substr(0,temp_raw.length-2)+'Range: npt=now-\r\n\r\n';
                    logger.info(req);
                    this.gopCache = this.pushSession.gopCache;
                    let vUrl = this.url.substring(0,this.url.lastIndexOf('/') + 1) + this.vControl;
                    let aUrl = this.url.substring(0,this.url.lastIndexOf('/') + 1) + this.aControl;
                    let rtp = rtpParser.parseRtpPacket(this.gopCache[0]);
                    this.vSeq = rtp.sequenceNumber;
                    this.vTime = rtp.timestamp;
                    let audioRtp = rtpParser.parseRtpPacket(this.pushSession.audioCache[0]);
                    this.aSeq = audioRtp.sequenceNumber;
                    this.aTime = audioRtp.timestamp;
                    let rtpInfo = `url=${vUrl};seq=${this.vSeq};rtptime=${this.vTime},url=${aUrl};seq=${this.aSeq};rtptime=${this.aTime}`;
                    //let rtpInfo = `url=${vUrl};seq=${this.vSeq};rtptime=${this.vTime}`;
                    res.headers['RTP-Info'] = rtpInfo;
                    process.nextTick(async () => {
                        debugger;
                        await this.sendGOPCache();
                    })
                    if(req['Range'] === undefined){
                        req['Range'] = 'npt=now-';
                        res.headers['Range'] = 'npt=now-';
                    }else{
                        res.headers['Range'] = req['Range'];
                    }
                    break;
                case 'RECORD':
                    break;
                case 'PAUSE':
                    this.makeResponseAndSend(res);
                    this.stopPushAndPlayer();
                    return;
                case 'TEARDOWN':
                    this.makeResponseAndSend(res);
                    this.stopPushAndPlayer();
                    return;
            }

            this.makeResponseAndSend(res);
        }catch (e) {
            logger.info(e)
            //res.code = 404;
            //res.msg = 'NOT FOUND';
            this.makeResponseAndSend(res);
            this.stopPushAndPlayer();
        }

    }

    stopPushAndPlayer(){
        if(this.server.playSessions[this.path]){
            for(let playerSession of this.server.playSessions[this.path]){
                playerSession.socket.end();
            }
        }
        if(this.server.pushSessions[this.path]){
            this.server.pushSessions[this.path].socket.end();
        }
    }

    stop() {
        try{
            this.bp.stop();
            this.bp = null;
            if( this.audioCache ) {
                this.audioCache = null;
            }
            if (this.gopCache) {
                this.gopCache = null;
            }
            this.server.removeSession(this);

            this.aRTPClientSocket && this.aRTPClientSocket.close();
            this.aRTPControlClientSocket && this.aRTPControlClientSocket.close();
            this.vRTPClientSocket && this.vRTPClientSocket.close();
            this.vRTPControlClientSocket && this.vRTPControlClientSocket.close();

            this.aRTPServerSocket && this.aRTPServerSocket.close();
            this.aRTPControlServerSocket && this.aRTPControlServerSocket.close();
            this.vRTPServerSocket && this.vRTPServerSocket.close();
            this.vRTPControlserverSokcet && this.vRTPControlserverSokcet.close();

            this.socket.destroy();
            logger.info(`rtsp session[type=${this.type}, path=${this.path}, sid=${this.sid}] end`);
        }catch (e) {
            logger.info(e);
        }
    }

    sendGOPCache() {
        return new Promise(async (resolve, reject) => {
            try{
                if (!this.pushSession) {
                    resolve();
                    return;
                }
                debugger;
                let rtpBuf = this.pushSession.aControlCache;
                let len = rtpBuf.length + 4;
                let headerBuf = Buffer.allocUnsafe(4);
                headerBuf.writeUInt8(0x24, 0);
                headerBuf.writeUInt8(this.aRTPControlChannel, 1);
                headerBuf.writeUInt16BE(rtpBuf.length, 2);
                this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
                let rtp = rtpParser.parseControlPacket(rtpBuf);
                logger.info('aControlCache:' + rtp.sr + ':' + rtp.NTPtimestamp + ':' + rtp.RTPtimestamp);
                this.outBytes += len;
                this.pushSession.outBytes += len;

                rtpBuf = this.pushSession.vControlCache;
                len = rtpBuf.length + 4;
                headerBuf = Buffer.allocUnsafe(4);
                headerBuf.writeUInt8(0x24, 0);
                headerBuf.writeUInt8(this.vRTPControlChannel, 1);
                headerBuf.writeUInt16BE(rtpBuf.length, 2);
                this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
                rtp = rtpParser.parseControlPacket(rtpBuf);
                logger.info('vControlCache:' + rtp.sr + ':' +  rtp.NTPtimestamp + ':' + rtp.RTPtimestamp);
                this.outBytes += len;
                this.pushSession.outBytes += len;

                for (let rtpBuf of this.pushSession.gopCache) {
                    if (this.transType == 'tcp') {
                        let len = rtpBuf.length + 4;
                        let headerBuf = Buffer.allocUnsafe(4);
                        headerBuf.writeUInt8(0x24, 0);
                        headerBuf.writeUInt8(this.vRTPChannel, 1);
                        headerBuf.writeUInt16BE(rtpBuf.length, 2);
                        this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
                        //fs.writeFile('byteLog'+this.sid,Buffer.concat([headerBuf,rtpBuf],len),{flag:'a'});
                        //let rtp = rtpParser.parseRtpPacket(rtpBuf);
                        //logger.info('gopCache:' + rtp.timestamp );
                        this.outBytes += len;
                        this.pushSession.outBytes += len;
                    } else if (this.transType == 'udp' && this.vRTPClientSocket) {
                        await this.sendUDPPack(rtpBuf, this.vRTPClientSocket, this.vRTPClientPort, this.host);
                        this.outBytes += rtpBuf.length;
                        this.pushSession.outBytes += rtpBuf.length;
                    }
                }
                for (let rtpBuf of this.pushSession.audioCache) {
                    if (this.transType == 'tcp') {
                        let len = rtpBuf.length + 4;
                        let headerBuf = Buffer.allocUnsafe(4);
                        headerBuf.writeUInt8(0x24, 0);
                        headerBuf.writeUInt8(this.aRTPChannel, 1);
                        headerBuf.writeUInt16BE(rtpBuf.length, 2);
                        this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
                        //fs.writeFile('byteLog'+this.sid,Buffer.concat([headerBuf,rtpBuf],len),{flag:'a'});
                        //let rtp = rtpParser.parseRtpPacket(rtpBuf);
                        //logger.info('audioCache:' + rtp.timestamp);
                        this.outBytes += len;
                        this.pushSession.outBytes += len;
                    } else if (this.transType == 'udp' && this.aRTPClientSocket) {
                        await this.sendUDPPack(rtpBuf, this.aRTPClientSocket, this.aRTPClientPort, this.host);
                        this.outBytes += rtpBuf.length;
                        this.pushSession.outBytes += rtpBuf.length;
                    }
                }
                resolve();
            }catch (e) {
                resolve();
                logger.info(e);
            }

        })
    }

    async sendVideo(rtpBuf) {
        if (this.transType == 'tcp') {
            let len = rtpBuf.length + 4;
            let headerBuf = Buffer.allocUnsafe(4);
            headerBuf.writeUInt8(0x24, 0);
            headerBuf.writeUInt8(this.vRTPChannel, 1);
            headerBuf.writeUInt16BE(rtpBuf.length, 2);
            //this.socketStartDate = process.uptime();
            this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
            //logger.info(this.path + ':socketTime' + (process.uptime() - this.socketStartDate));
            this.outBytes += len;
            //fs.writeFile('byteLog'+this.sid,Buffer.concat([headerBuf,rtpBuf],len),{flag:'a'});
            this.pushSession.outBytes += len;
        } else if (this.transType == 'udp' && this.vRTPClientSocket) {
            this.vRTPClientSocket.send(rtpBuf, this.vRTPClientPort, this.host);
            this.outBytes += rtpBuf.length;
            this.pushSession.outBytes += rtpBuf.length;
        }
    }

    sendVideoControl(rtpBuf) {
        if (this.transType == 'tcp') {
            let len = rtpBuf.length + 4;
            let headerBuf = Buffer.allocUnsafe(4);
            headerBuf.writeUInt8(0x24, 0);
            headerBuf.writeUInt8(this.vRTPControlChannel, 1);
            headerBuf.writeUInt16BE(rtpBuf.length, 2);
            this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
            this.outBytes += len;
            //fs.writeFile('byteLogControl'+this.sid,Buffer.concat([headerBuf,rtpBuf],len),{flag:'a'});
            this.pushSession.outBytes += len;
        } else if (this.transType == 'udp' && this.vRTPControlClientSocket) {
            this.vRTPControlClientSocket.send(rtpBuf, this.vRTPControlClientPort, this.host);
            this.outBytes += rtpBuf.length;
            this.pushSession.outBytes += rtpBuf.length;
        }
    }

    sendAudio(rtpBuf) {
        if (this.transType == 'tcp') {
            let len = rtpBuf.length + 4;
            let headerBuf = Buffer.allocUnsafe(4);
            headerBuf.writeUInt8(0x24, 0);
            headerBuf.writeUInt8(this.aRTPChannel, 1);
            headerBuf.writeUInt16BE(rtpBuf.length, 2);
            this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
            this.outBytes += len;
            this.pushSession.outBytes += len;
        } else if (this.transType == 'udp' && this.aRTPClientSocket) {
            this.aRTPClientSocket.send(rtpBuf, this.aRTPClientPort, this.host);
            this.outBytes += rtpBuf.length;
            this.pushSession.outBytes += rtpBuf.length;
        }
    }

    sendAudioControl(rtpBuf) {
        if (this.transType == 'tcp') {
            let len = rtpBuf.length + 4;
            let headerBuf = Buffer.allocUnsafe(4);
            headerBuf.writeUInt8(0x24, 0);
            headerBuf.writeUInt8(this.aRTPControlChannel, 1);
            headerBuf.writeUInt16BE(rtpBuf.length, 2);
            this.socket.write(Buffer.concat([headerBuf, rtpBuf], len));
            this.outBytes += len;
            this.pushSession.outBytes += len;
        } else if (this.transType == 'udp' && this.aRTPControlClientSocket) {
            this.aRTPControlClientSocket.send(rtpBuf, this.aRTPControlClientPort, this.host);
            this.outBytes += rtpBuf.length;
            this.pushSession.outBytes += rtpBuf.length;
        }
    }

    broadcastVideo(rtpBuf) {
        let playSessions = this.server.playSessions[this.path] || [];
        for (let playSession of playSessions) {
            playSession.sendVideo(rtpBuf);
        }
    }

    broadcastVideoControl(rtpBuf) {
        let playSessions = this.server.playSessions[this.path] || [];
        for (let playSession of playSessions) {
            playSession.sendVideoControl(rtpBuf);
        }
    }

    broadcastAudio(rtpBuf) {
        let playSessions = this.server.playSessions[this.path] || [];
        for (let playSession of playSessions) {
            playSession.sendAudio(rtpBuf);
        }
    }

    broadcastAudioControl(rtpBuf) {
        let playSessions = this.server.playSessions[this.path] || [];
        for (let playSession of playSessions) {
            playSession.sendAudioControl(rtpBuf);
        }
    }

    sleep(timeout = 1000) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                resolve();
            }, timeout);
        })
    }

    getUDPType() {
        return this.socket.address().family == 'IPv6' ? 'udp6' : 'udp4';
    }

    sendUDPPack(buf, socket, port, host) {
        return new Promise((resolve, reject) => {
            socket.send(buf, port, host, (err, len) => {
                resolve();
            })
        })
    }

    bindUDPPort(socket, port) {
        return new Promise((resolve, reject) => {
            socket.bind(port, () => {
                // logger.info(`UDP socket bind on ${port} done.`);
                resolve();
            })
        })
    }

    checkUrlValid(urlObject) {
        try{
            let startTime = new Date().getTime();
            if(!urlObject || !urlObject.pathname){
                return false;
            }

            let params = urlObject.query.split("&");
            let t = params[0].replace("t=","");
            let sign = params[1].replace("sign=","");
            let urlPaths = urlObject.pathname.split("/");
            let userID = urlPaths[2];
            let licenseID = urlPaths[3];

            if(!userID || ! licenseID || !t || !sign){
                return false;
            }

            let limitTime = Math.floor(startTime/1000) + cfg.limitSecondTime;
            logger.info(`limitTime: ${limitTime}`)
            if(t >= limitTime){
                return false;
            }

            let result =  utils.aesDecSign(`${userID}|${licenseID}|${t}`,sign);
            let endTime = new Date().getTime();
            logger.info(`aes spend time: ${endTime-startTime}`)
            return result;
        }catch (e) {
            logger.info(e);
            return false;
        }



    }
}

module.exports = RTSPSession;
