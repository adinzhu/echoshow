const express = require("express");
const cfg = require("../cfg");
const utils = require('../utils');
const os = require('os');
const http = require('http');
const crypto = require('crypto');
const log4j = require('../log4j');
const logger = log4j.getLogger('console');

const r = express.Router();

r.post("/login", async (req, res) => {
    let username = req.body.username;
    let password = req.body.password;
    if (!username) {
        throw new Error("用户名不能为空");
    }
    if (!password) {
        throw new Error("密码不能为空");
    }
    let user = utils.db.read().get('users').find({ name: username }).cloneDeep().value();
    if (!user) {
        throw new Error("用户名不存在");
    }
    if (user.password !== password) {
        throw new Error("密码错误");
    }
    delete user.password;
    req.session.user = user;
    res.sendStatus(200);
});

r.get("/logout", async (req, res) => {
    delete req.session;
    if (utils.xhr(req)) {
        res.sendStatus(200);
        return;
    } else {
        res.redirect("/");
    }
});

r.post("/userInfo", async (req, res) => {
    if (req.session.user) {
        let user = utils.db.get('users').find({ name: req.session.user.name }).cloneDeep().value();
        if(user) {
            delete user.password;
            res.json(user);
            return;
        }
    }
    res.json(null);
});

let _cpuInfo = utils.getCPUInfo();
let cpuUsage = 0;
setInterval(() => {
    let cpuInfo = utils.getCPUInfo();
    let idle = cpuInfo.idle - _cpuInfo.idle;
    let total = cpuInfo.total - _cpuInfo.total;
    let per = Math.floor(idle / total * 100) / 100;
    cpuUsage = 1 - per;
    _cpuInfo = cpuInfo;
}, 300)

let memData = [], cpuData = [], pusherData = [], playerData = [];
let duration = 30;
let stats = require('routes/stats');
setInterval(() => {
    let totalmem = os.totalmem();
    let freemem = os.freemem();
    let memUsage = (totalmem - freemem) / totalmem;
    let now = new Date();
    while(memData.length >= duration) {
        memData.shift();
    }
    memData.push({
        time: now,
        '使用': memUsage
    });
    while(cpuData.length >= duration) {
        cpuData.shift();
    }
    cpuData.push({
        time: now,
        '使用': cpuUsage
    })
    while(pusherData.length >= duration) {
        pusherData.shift();
    }
    let pusherCnt = 0;
    if(stats.rtspServer) {
        pusherCnt = Object.keys(stats.rtspServer.pushSessions).length;
    }
    pusherData.push({
        time: now,
        '总数': pusherCnt
    })
    while(playerData.length >= duration) {
        playerData.shift();
    }
    let playerCnt = 0;
    if(stats.rtspServer) {
        for(let path in stats.rtspServer.playSessions) {
            playerCnt += stats.rtspServer.playSessions[path].length;
        }
    }
    playerData.push({
        time: now,
        '总数': playerCnt
    })
}, 1000);

r.post('/serverInfo', async (req, res) => {
    res.json({
        memData: memData,
        cpuData: cpuData,
        pusherData: pusherData,
        playerData: playerData
    })
})

setInterval(() => {
    let pushCnt = 0;
    if(stats.rtspServer) {
        pushCnt = Object.keys(stats.rtspServer.pushSessions).length;
    }
    
    let playCnt = 0;
    if(stats.rtspServer) {
        for(let path in stats.rtspServer.playSessions) {
            playCnt += stats.rtspServer.playSessions[path].length;
        }
    }

    let t = new Date().getTime();
    
    let sendDataInfo = {
        url: cfg.url,
        pushCnt: pushCnt,
        playCnt: playCnt,
        t: t,
        sign: utils.aesEncryptContent(`${cfg.url}|${pushCnt}|${playCnt}|${t}`)
    }
    sendHeartBeat(sendDataInfo);
    
    
}, cfg.heartBeatTime*1000);

let sendHeartBeat = function(post_data) {
        //logger.info(new Date().toLocaleString());
        try{
            let qs = require('querystring');
            let content = qs.stringify(post_data);
            logger.info(content);

            let options = {
                hostname: cfg.balanceServerIp,
                path: `${cfg.heartBeatPath}?${content}`,
                method: 'GET',
                timeout: 5000,
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            };

            let req = http.request(options, function (res) {
                logger.info('STATUS: ' + res.statusCode);
                res.setEncoding('utf8');
                res.on('data', function (chunk) {
                    logger.info('BODY: ' + chunk);
                });
            });

            req.on('error', function (e) {
                logger.info('problem with request: ' + e.message);
            });

            req.end();
        }catch (e) {
            logger.info(`heart beat error: ${e}`)
        }
    }

let t = new Date().getTime();
let sendFirstDataInfo = {
    url: cfg.url,
    pushCnt: 0,
    playCnt: 0,
    t: t,
    sign: utils.aesEncryptContent(`${cfg.url}|0|0|${t}`)
}
sendHeartBeat(sendFirstDataInfo)
        
module.exports = r;
