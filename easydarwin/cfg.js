const path = require("path");
const os = require("os");

module.exports = {
    http_port: 10008,
    rtsp_tcp_port: 8554,
    defaultPwd: '123456',
    rootDir: __dirname,
    heartBeatTime: 20,//单位为秒
    url: 'www.windhome.me',
    ip: '192.168.1.21',
    balanceServerIp: '192.168.1.21',
    balanceServerPort: 10007,
    aesKey: 'balanceSecret',
    heartBeatPath: '/heartBeat',
    timeOutSecond: 20,
    limitSize: 2000,
    limitSecondTime: 120,
    pushTimeoutSecond: 60,
    wwwDir: path.resolve(__dirname, "www"),
    dataDir: path.resolve(os.homedir(), ".easydarwin")
}
