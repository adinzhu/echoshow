const cfg = require('../cfg');
const shortid = require('shortid');
const lowdb = require('lowdb');
const FileSync = require('lowdb/adapters/FileSync.js');
const path = require('path');
const crypto = require("crypto");
const fs = require('fs-extra');
const moment = require('moment');
const url = require('url');
const os = require('os');
const log4j = require('../log4j');
const logger = log4j.getLogger('console');

exports.md5 = str => {
    return crypto.createHash('md5').update(str, "utf8").digest('hex');
}

exports.xhr = req => {
    return req.xhr || (req.get('accept') || "").match(/\/json$/);
}

exports.shortid = function() {
    return shortid.generate();
}

exports.formatDate = function(d) {
    return moment(d).format('YYYY-MM-DD')
}

exports.formatDateTime = function(d) {
    return moment(d).format('YYYY-MM-DD HH:mm:ss')
}

exports.getCPUInfo = function() { 
    let cpus = os.cpus();
    let idle = 0;
    let total = 0;
    for(let cpu in cpus){
        if (!cpus.hasOwnProperty(cpu)) continue;	
        for(let key in cpus[cpu].times) {
            if(!cpus[cpu].times.hasOwnProperty(key)) continue;
            total += cpus[cpu].times[key];
            if(key == 'idle') {
                idle += cpus[cpu].times[key];
            }
        }
    }
	
    return {
        'idle': idle, 
        'total': total
    };
}

fs.ensureDirSync(cfg.dataDir);
const db = lowdb(new FileSync(path.resolve(cfg.dataDir, 'db.json')));
db.defaults({
    users: [{id: exports.shortid(), name: 'admin', password: exports.md5(cfg.defaultPwd)}],
    demo: false,
    apiAuthed: false
}).write();
exports.db = db;




let CBC = 'cbc';
let ECB = 'ecb';
let NULL_IV = new Buffer([]);

let IV = new Buffer([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
let cipherMode = CBC;
let keySize = 128;
let algorithm;
setAlgorithm();
let outputEncoding = 'base64';
let inputEncoding = 'utf8';

function setAlgorithm() {
    algorithm = 'aes-' + keySize + '-' + cipherMode;
}

function setCipherMode(mode) {
    if (mode !== CBC && mode !== ECB) {
        throw ('AES.setCipherMode error: ' + mode);
    }
    cipherMode = mode;
    setAlgorithm();
}

function setKeySize(size) {
    if (size !== 128 && size !== 256) {
        throw ('AES.setKeySize error: ' + size);
    }
    keySize = size;
    setAlgorithm();
    // logger.info('setKeySize:%j',keySize);
}

/**
 * the key must match the keySize/8 , like:16 ,32
 * @param  {Buffer} key
 * @return {}
 */
function checkKey(key) {
    if (!key) {
        throw 'AES.checkKey error: key is null ';
    }
    if (key.length !== (keySize / 8)) {
        throw 'AES.checkKey error: key length is not ' + (keySize / 8) + ': ' + key.length;
    }
}

/**
 * buffer/bytes encription
 * @param  {Buffer} buff
 * @param  {Buffer} key  the length must be 16 or 32
 * @param  {Buffer} [newIv]   default is [0,0...0]
 * @return {encripted Buffer}
 */
function encBytes(buff, key, newIv) {
    checkKey(key);
    let iv = newIv || IV;
    if (cipherMode === ECB) iv = NULL_IV;
    let cipher = crypto.createCipheriv(algorithm, key, iv);
    cipher.setAutoPadding(true);
    let re = Buffer.concat([cipher.update(buff), cipher.final()]);
    // logger.info('enc re:%s,len:%d', printBuf(re), re.length);
    return re;
}

/**
 * text encription
 * @param  {string} text
 * @param  {Buffer} key         the length must be 16 or 32
 * @param  {Buffer} [newIv]       default is [0,0...0]
 * @param  {string} [input_encoding]  ["utf8" -default,"ascii","base64","binary"...](https://nodejs.org/api/buffer.html#buffer_buffer)
 * @param  {string} [output_encoding] ["base64" -default,"hex"]
 * @return {string}                 encription result
 */
function encText(text, key, newIv, input_encoding, output_encoding) {
    checkKey(key);
    let iv = newIv || IV;
    if (cipherMode === ECB) iv = NULL_IV;
    let inEncoding = input_encoding || inputEncoding;
    let outEncoding = output_encoding || outputEncoding;
    let buff = new Buffer(text, inEncoding);
    let out = encBytes(buff, key, iv);
    let re = new Buffer(out).toString(outEncoding);
    return re;
}

/**
 * buffer/bytes decription
 * @param  {Buffer} buff
 * @param  {Buffer} key  the length must be 16 or 32
 * @param  {Buffer} [newIv] default is [0,0...0]
 * @return {encripted Buffer}
 */
function decBytes(buff, key, newIv) {
    checkKey(key);
    let iv = newIv || IV;
    if (cipherMode === ECB) iv = NULL_IV;
    let decipher = crypto.createDecipheriv(algorithm, key, iv);
    decipher.setAutoPadding(true);
    let out = Buffer.concat([decipher.update(buff), decipher.final()]);
    return out;
}

/**
 * text decription
 * @param  {string} text
 * @param  {Buffer} key         the length must be 16 or 32
 * @param  {Buffer} [newIv]       default is [0,0...0]
 * @param  {string} [input_encoding]  ["utf8" - default,"ascii","base64","binary"...](https://nodejs.org/api/buffer.html#buffer_buffer)
 * @param  {string} [output_encoding] ["base64"- default ,"hex"]
 * @return {string}                 decription result
 */
function decText(text, key, newIv, input_encoding, output_encoding) {
    checkKey(key);
    let iv = newIv || IV;
    if (cipherMode === ECB) iv = NULL_IV;
    let inEncoding = input_encoding || inputEncoding;
    let outEncoding = output_encoding || outputEncoding;
    let buff = new Buffer(text, outEncoding);
    let re = new Buffer(decBytes(buff, key, iv)).toString(inEncoding);
    return re;
}

let key = new Buffer("c4b84456c1379bec99c4d1b7e9f131aa", 'hex');
setCipherMode(CBC);
let iv = new Buffer("abcdefgh12345678","utf8");//字符串一定是16位
//let buffer_encrypt = encBytes(buffer,key,iv);


exports.aesDecSign = function(content,sign) {
    try{
        let buffer = new Buffer(sign,"base64");
        let crypto_buffer =decBytes(buffer,key,iv);
        let decSign = crypto_buffer.toString();
        logger.info(`content:${content},sign:${sign},decSign:${decSign}`)
        if(decSign === content ){
            return true;
        }
    }catch (e) {
        logger.info(e);
    }
    return false;
}

exports.aesEncryptContent = function(content) {
    try{
        return encText(content,key,iv)
    }catch (e) {
        logger.info(e);
    }
    return "";
}