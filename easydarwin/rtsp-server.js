const tls = require('tls');
const net = require('net');
const fs = require('fs');
const ip = require('@penggy/internal-ip');
const RTSPSession = require('./rtsp-session');
const events = require('events');
const cfg = require('./cfg');
const log4j = require('./log4j');
const logger = log4j.getLogger('console');

class RTSPServer extends events.EventEmitter {

    constructor(port ) {
        super();
        this.port = port;
        this.pushSessions = {};
        this.playSessions = {};
        this.server = net.createServer();
        this.server.on("connection", socket => {
            new RTSPSession(socket, this);
        }).on("error", err => {
            logger.info('rtsp server error:', err);
        }).on("listening", async () => {
            let host = await ip.v4();
            let env = process.env.NODE_ENV || "development";
            logger.info(`EasyDarwin rtsp server listening on rtsp://${host}:${this.port} in ${env} mode1`);
        })

        const options = {
            key: fs.readFileSync('/root/easydarwin/key/3196384__meari.com.cn.key'),
            cert: fs.readFileSync('/root/easydarwin/key/3196384__meari.com.cn.pem'),
        };
        this.sslServer = tls.createServer(options);
        this.sslServer.on("secureConnection", socket => {
            logger.info(`tls connection get: ${socket} : ${socket.authorized} ,protocol: ${socket.getProtocol()}, cipher: ${JSON.stringify(socket.getCipher(), null, 1)}` );
            new RTSPSession(socket, this);
        }).on("clientError", err => {
            logger.info('tls rtsp client error:', err);
        }).on("error", err => {
            logger.info('tls rtsp server error:', err);
        }).on("listening", async () => {
            let host = await ip.v4();
            let env = process.env.NODE_ENV || "development";
            logger.info(`EasyDarwin tls rtsp server listening on rtsp://${host}:443 in ${env} mode1`);
        })

        let server = this;
        setInterval(() => {
            try{
                let timestamp = Math.floor(new Date().getTime()/1000);
                let pushSession;
                for (let pushSessionsKey in server.pushSessions) {
                    pushSession = server.pushSessions[pushSessionsKey]
                    logger.info(`pushSessionsKey: ${pushSessionsKey},startTimestamp: ${pushSession.startTimestamp}(${pushSession.startTimestamp + cfg.pushTimeoutSecond}),timestamp: ${timestamp},playSessions:${server.playSessions[pushSessionsKey]}`)
                    if(pushSession.startTimestamp + cfg.pushTimeoutSecond <= timestamp && (!server.playSessions[pushSessionsKey] || server.playSessions[pushSessionsKey].length === 0)){
                        pushSession.stop();
                    }
                }
            }catch(error){
                logger.info(error);
            }

        }, 5000);
    }

    start() {
        this.server.listen(this.port);
        this.sslServer.listen("443");
        this.stats();
    }

    stats() {
        require('./routes/stats').rtspServer = this;
    }

    addSession(session) {
        if(session.type == 'pusher') {
            this.pushSessions[session.path] = session;
            //logger.info('pushSession:' + this.pushSessions.length);
        } else if(session.type == 'player') {
            let playSessions = this.playSessions[session.path];
            if(!playSessions) {
                playSessions = [];
                this.playSessions[session.path] = playSessions;
            }
            //logger.info('playSession:' + this.playSessions.length);
            if(playSessions.indexOf(session) < 0) {
                playSessions.push(session);
            }
            //logger.info(session);
        }
    }

    removeSession(session) {
        if(session.type == 'pusher') {
            delete this.pushSessions[session.path];
        } else if(session.type == 'player') {
            let playSessions = this.playSessions[session.path];
            if(playSessions && playSessions.length > 0) {
                let idx = playSessions.indexOf(session);
                if(idx >= 0) {
                    playSessions.splice(idx, 1);
                }
            }
        }
    }
}

module.exports = RTSPServer;
