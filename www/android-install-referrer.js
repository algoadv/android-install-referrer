var exec = require('cordova/exec');

function InstallReferrer() { };

InstallReferrer.prototype.get = function(success, error) {
    exec(success, error, 'AndroidInstallReferrer', 'get', []);
}

var instance = new InstallReferrer();
module.exports = instance;
