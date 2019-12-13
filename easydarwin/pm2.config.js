const cfg = require('cfg');
const path = require('path');

module.exports = {
  apps: [
    {
      name: "EasyDarwin",
      script: 'app.js',
      cwd: __dirname,
      env: {
        NODE_PATH: __dirname,
        NODE_ENV: 'production'
      },
      log_date_format : "YYYY-MM-DD HH:mm:ss",
      watch: true,
      max_restarts: 300,
      // autorestart: false,
      error_file: path.resolve(cfg.dataDir, "logs/EasyDarwin.log"),
      out_file: path.resolve(cfg.dataDir, "logs/EasyDarwin.log")
    }
  ]
};
