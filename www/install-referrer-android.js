var exec = require('cordova/exec');

function InstallReferrer() { };

InstallReferrer.prototype.get = function(success, error) {
    exec(success, error, 'AndroidInstallReferrer', 'get', []);
}

InstallReferrer.prototype.getData = function(success, error) {
    exec(success, error, 'AndroidInstallReferrer', 'getData', []);
}

var instance = new InstallReferrer();
module.exports = instance;