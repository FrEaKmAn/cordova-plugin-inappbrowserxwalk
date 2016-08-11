/*global cordova, module*/

function InAppBrowserXwalk() {

}

var callbacks = [];

InAppBrowserXwalk.prototype = {
    close: function () {
        cordova.exec(null, null, "InAppBrowserXwalk", "close", []);
    },
    addEventListener: function (eventname, func) {
        callbacks[eventname] = func;
    },
    removeEventListener: function (eventname) {
        callbacks[eventname] = undefined;
    },
    show: function () {
        cordova.exec(null, null, "InAppBrowserXwalk", "show", []);
    },
    hide: function () {
        cordova.exec(null, null, "InAppBrowserXwalk", "hide", []);
    },
	executeScript: function(script) {
		cordova.exec(null, null, "InAppBrowserXwalk", "executeScript", [script]);
	},
	getUrl: function(callback, errorCallback) {
		cordova.exec(callback, errorCallback, "InAppBrowserXwalk", "getUrl", []);
	}
};

var callback = function(event) {
	var names = ['loadstart', 'loadstop', 'exit', 'updatevisithistory'];
    var index = names.indexOf(event.type);

	if(index > -1) {
		var name = names[index];
		if(callbacks[name] !== undefined) {
			callbacks[name](event);
		}
	}
};

module.exports = {
    open: function (url, options) {
        options = (options === undefined) ? "{}" : JSON.stringify(options);
        cordova.exec(callback, null, "InAppBrowserXwalk", "open", [url, options]);
        return new InAppBrowserXwalk();
    }
};
