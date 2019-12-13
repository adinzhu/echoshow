const utils = require("utils");
const log4j = require('../log4j');
const logger = log4j.getLogger('console');

var apiAuthed = utils.db.read().get("apiAuthed").cloneDeep().value();

setInterval(() => {
    apiAuthed = utils.db.read().get("apiAuthed").cloneDeep().value();
}, 3000)

module.exports = async (req, res) => {
    if(apiAuthed) {
        if (!req.session || !req.session.user) {
            logger.info(`access denied[${req.path}]`);
            throw new Error('access denied');
        }
    }
};