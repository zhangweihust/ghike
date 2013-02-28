/**
 * @namespace Hike root namespace
 */
var hike = {};
(function(hike){
	
	
	(function () {
	var ie = !!(window.attachEvent && !window.opera);
	var wk = /webkit\/(\d+)/i.test(navigator.userAgent) && (RegExp.$1 < 525);
	var fn = [];
	var run = function () { for (var i = 0; i < fn.length; i++) fn[i](); };
	var d = document;
	d.ready = function (f) {
	    if (!ie && !wk && d.addEventListener)
	      return d.addEventListener('DOMContentLoaded', f, false);
	    if (fn.push(f) > 1) return;
	    if (ie)
	      (function () {
	        try { d.documentElement.doScroll('left'); run(); }
	        catch (err) { setTimeout(arguments.callee, 0); }
	      })();
	    else if (wk)
	      var t = setInterval(function () {
	        if (/^(loaded|complete)$/.test(d.readyState))
	          clearInterval(t), run();
	      }, 0);
	};
	})();
	
	hike.extend = function(obj1, obj2){
		if(arguments.length == 1){
			this.extend(this, obj1);
			return this;
		}
		for(var i in obj2)
			obj1[i] = obj2[i];
		return obj1;
	};
	
	var isReady = false;
	var callbacks = null;
	hike.ready =  function(fn, scope){
		if(isReady){
			fn.call(scope || document);
			return;
		}
		if(callbacks == null){	//first time
			callbacks = [];
			document.addEventListener('DOMContentLoaded', function(e){
				isReady = true;
				document.removeEventListener('DOMContentLoaded', arguments.callee);
				while(callbacks.length != 0){
					var cb = callbacks.shift();
					cb.fn.call(cb.scope || document, e);
				}
			});
		}
		callbacks.push({
			fn : fn,
			scope : scope
		});
	};
	
	var logLevel = 'info';
	
	var levels = ['error', 'warn', 'info', 'debug'];
	
	var _console = console || {};
	
	var isEnabled = function(level){
		return levels.indexOf(logLevel) >= levels.indexOf(level);
	};
	
	var createLog = function(level){
		return (function(msg){
			if(isEnabled(level)){
				msg = '[' + level.toUpperCase() + ']\t' + msg;
				_console[level]?_console[level](msg):(_console['log']?_console['log'](msg):null);
			}
		});
	};
	
	hike.log = function(msg){
		_console['log']?_console['log'](msg):null;
	}
	hike.extend(hike.log, {
		
		setLevel : function(level){
			if(levels.indexOf(level) == -1)
				return;
			logLevel = level;
		},

		error : createLog('warn'),
		
		warn : createLog('warn'),
		
		info : createLog('info'),
		
		debug : createLog('debug'),
		
		log : createLog(),
		
		trace : function(){
			if(_console.trace())
				_console.trace();
		}
		
	});
	
	hike.iframeContiner = {};
	
	/**
	 * @namespace Public request function
	 */
	hike.net = {};
	hike.extend(hike.net, {
		/**
		 * Insert doc here
		 * @param 
		 * @returns
		 * @author 
		 */
		ajax : function(options, callback){
			
			var createXHR =function() {
			    return hike.util.tryFunctions(
			      function() {return new XMLHttpRequest()},
			      function() {return new ActiveXObject('Msxml2.XMLHTTP')},
			      function() {return new ActiveXObject('Microsoft.XMLHTTP')}
			    ) || false;
			};
			var type = (typeof options.type)=='undefined'?'get':options.type;
			var url = (typeof options.url)=='undefined'?'':options.url;
			var callback = (typeof callback)=='undefined'?function(){}:callback;
			var contentType = (typeof options.contentType)=='undefined'?"application/x-www-form-urlencoded":options.contentType;
			var dataType = (typeof options.dataType)=='undefined'?'json':options.dataType;
			var async = (typeof options.async)=='undefined'?true:options.async;
			var data = (typeof options.data)=='undefined'?{}:options.data;
			
			var hikeXHR = ((typeof options.hikeXHR) == "undefined")?createXHR():options.hikeXHR;
			delete options.hikeXHR;
			hikeXHR.open(type,url,async);
			hikeXHR.setRequestHeader("Content-type",contentType);
			hikeXHR.setRequestHeader("Cache-Control","no-cache");
			hikeXHR.onreadystatechange = function(){
				if(hikeXHR.readyState==4){
					if(hikeXHR.status == 200 || hikeXHR.status == 0){
						var result = hikeXHR.responseText;
						if(dataType.toLowerCase()=='json') callback(eval("("+result+")"));
						else callback(result);
					}else{
						hike.log('Server is not responding.\r\nstatus:'+hikeXHR.status);
					}
				}
			}
			hikeXHR.send(data);
		},
		
		jsonp : function(url, callback){
			var c = 'callbackHandler';
			url = url + "&callback=" + c;
			// Handle JSONP-style loading
			
			window[ c ] = window[ c ] || function( data ) {
				
				callback(data);
				// Garbage collect
				window[ c ] = undefined;
				try {
					delete window[ c ];
				} catch(e) {}
				if ( head ) {
					head.removeChild( script );
				}
			};
			var head = document.getElementsByTagName("head")[0] || document.documentElement;
			var script = document.createElement("script");
			script.src = url;
			// Handle Script loading
			var done = false;
			// Attach handlers for all browsers
			script.onload = script.onreadystatechange = function() {
				if ( !done && (this.readyState === "loaded" || this.readyState === "complete") ) {
					done = true;
					// Handle memory leak in IE
					script.onload = script.onreadystatechange = null;
					if ( head && script.parentNode ) {
						head.removeChild( script );
					}
				}
			};
			head.insertBefore( script, head.firstChild );
		}
		
	});
	
	/**
	 * @namespace Public dom function
	 */
	hike.dom = {};
	hike.extend(hike.dom, {
		
		getEl:function(element){
			if (arguments.length > 1) {
				for (var i = 0, elements = [], length = arguments.length; i < length; i++)
				  elements.push(this.getEl(arguments[i]));
				return elements;
			}
		    if ((typeof element)=='string')
			    return document.getElementById(element);
		},
		
		createIframe:function(url,id,display,onloadHandler){
			var iframe = document.createElement("iframe");
			if (id) {
				iframe.setAttribute("id",id);
				iframe.setAttribute("name",id);
			}
			iframe.setAttribute("frameborder","0");
			iframe.setAttribute("src",url);
			iframe.setAttribute("style","display:"+display+"");
			document.body.appendChild(iframe);
			
			hike.dom.addEventListener(iframe,'load',onloadHandler);
			return iframe;
		},
		
		addEventListener:function(srcEl,eventName,fn){
	    	if (srcEl.addEventListener){
	    		srcEl.addEventListener(eventName, fn, false);
	    	}
	    	else {
	    		srcEl.attachEvent("on"+eventName,fn);
	    	}
	    },
	    
	    removeEventListener:function(srcEl,eventName,fn){
	    	if (srcEl.removeEventListener){
	    		srcEl.removeEventListener(eventName, fn, false);
			} 
	    	else {
				srcEl.detachEvent('on' + eventName, fn);
			}
	    }
	});

	/**
	 * @namespace Public common utility function
	 */
	hike.util = {};
	hike.extend(hike.util, {
		
		encodeQuote : function(x){
			x = x.replace(/%/g, "%25");
			x = x.replace(/'/g, "%27");
			return x;
		},
		
		decodeQuote : function(x){
			x = x.replace(/%27/g, "'");
			x = x.replace(/%25/g, "%");
			return x;
		},
		
		decodeQuotes : function(o){
			for(var i in o) {
				if('string'===(typeof o[i])){
					o[i] = hike.util.decodeQuote(o[i]);
				}
				if('object'===(typeof o[i]) && o[i]!==null){
					hike.util.decodeQuotes(o[i]);
				}
			}
		},
		
		decodeJson : function(jsonstr){
			var x = eval("("+jsonstr+")");
			hike.util.decodeQuotes(x);
			return x;
		},
		
		encodeJson : function(o){
			if(typeof o == 'object'){
				if(o===null){
					return 'null';
				}
				if(o instanceof Array){
					var buf = [];
					for(var i=0;i<o.length;i++){
						buf.push(hike.util.encodeJson(o[i]));
					}
					return '[' + buf.join(',') + ']';
				}else{
					var buf = [];
					for(var i in o){
						buf.push("'" + i + "':" + hike.util.encodeJson(o[i]));
					}
					return '{' + buf.join(',') + '}';
				}
			}else if(typeof o == 'string'){
				return "'" +  hike.util.encodeQuote(o) + "'";
			}else if(typeof o == 'number' || typeof o == 'boolean' || typeof o == 'undefined'){
				return "" + o + "";
			}else{
				return "";
			}
		},
		
		tryFunctions : function() {
		    var returnValue;
		    for (var i = 0, length = arguments.length; i < length; i++) {
		      try {
		        returnValue = arguments[i]();
		        break;
		      } catch (e) { }
		    }
		    return returnValue;
		},
		
		params:function(o){
	    	var arr = [];
	    	var fmt = function(s){
	    		if (typeof s == 'object' && s != null){
	    			return this.params(s);
	    		}
	    		return s;
	    	}
	    	for (var i in o){
	    		arr.push( i + "=" + fmt(o[i]));
	    	}
			return  arr.join('&');
	    },
	    
	    trim:function(str){
	    	if(typeof str == "string")
	    		return str.replace(/(\s*$)/g,"");
	    	return str;
	    },
	    
	    isEmpty:function(str){
	    	if(!str) return true;
	    	var typeReg = /^object|array$/;
	    	if(typeReg.test(typeof str)){
	    		return  hike.util.size(str)==0;
	    	}
	    			
	    	return hike.util.trim(str)=="";
	    },
	    size : function(obj) {
	        var sz = 0;
	        for(var key in obj) {
	          if (obj.hasOwnProperty(key)) sz++;
	        }
	        return sz;
	    },
	    
	    isInt:function(str){
	    	return /^\d{1,16}$/.test(str);
	    },
	    
	    getTimestamp:function(){
	    	var timestamp = new Date();
	    	return timestamp.getTime();
	    },
	    
	    encodeBase64 : function(str){
	        var out, i, j, len;
	        var c1, c2, c3;

	        len = str.length;
	        i = j = 0;
	        out = [];
	        while (i < len) {
	            c1 = str.charCodeAt(i++) & 0xff;
	            if (i == len)
	            {
	                out[j++] = base64EncodeChars[c1 >> 2];
	                out[j++] = base64EncodeChars[(c1 & 0x3) << 4];
	                out[j++] = "==";
	                break;
	            }
	            c2 = str.charCodeAt(i++) & 0xff;
	            if (i == len)
	            {
	                out[j++] = base64EncodeChars[c1 >> 2];
	                out[j++] = base64EncodeChars[((c1 & 0x03) << 4) | ((c2 & 0xf0) >> 4)];
	                out[j++] = base64EncodeChars[(c2 & 0x0f) << 2];
	                out[j++] = "=";
	                break;
	            }
	            c3 = str.charCodeAt(i++) & 0xff;
	            out[j++] = base64EncodeChars[c1 >> 2];
	            out[j++] = base64EncodeChars[((c1 & 0x03) << 4) | ((c2 & 0xf0) >> 4)];
	            out[j++] = base64EncodeChars[((c2 & 0x0f) << 2) | ((c3 & 0xc0) >> 6)];
	            out[j++] = base64EncodeChars[c3 & 0x3f];
	        }
	        return out.join('');
	    },
	    
	    decodeBase64 : function(str){
	    	var c1, c2, c3, c4;
	        var i, j, len, out;

	        len = str.length;
	        i = j = 0;
	        out = [];
	        while (i < len) {
	             c1 
	            do {
	                c1 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
	            } while (i < len && c1 == -1);
	            if (c1 == -1) break;

	             c2 
	            do {
	                c2 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
	            } while (i < len && c2 == -1);
	            if (c2 == -1) break;

	            out[j++] = String.fromCharCode((c1 << 2) | ((c2 & 0x30) >> 4));

	             c3 
	            do {
	                c3 = str.charCodeAt(i++) & 0xff;
	                if (c3 == 61) return out.join('');
	                c3 = base64DecodeChars[c3];
	            } while (i < len && c3 == -1);
	            if (c3 == -1) break;

	            out[j++] = String.fromCharCode(((c2 & 0x0f) << 4) | ((c3 & 0x3c) >> 2));

	             c4 
	            do {
	                c4 = str.charCodeAt(i++) & 0xff;
	                if (c4 == 61) return out.join('');
	                c4 = base64DecodeChars[c4];
	            } while (i < len && c4 == -1);
	            if (c4 == -1) break;
	            out[j++] = String.fromCharCode(((c3 & 0x03) << 6) | c4);
	        }
	        return out.join('');
	    }
	    
	});
	
	var base64EncodeChars = [
	    "A", "B", "C", "D", "E", "F", "G", "H",
	    "I", "J", "K", "L", "M", "N", "O", "P",
	    "Q", "R", "S", "T", "U", "V", "W", "X",
	    "Y", "Z", "a", "b", "c", "d", "e", "f",
	    "g", "h", "i", "j", "k", "l", "m", "n",
	    "o", "p", "q", "r", "s", "t", "u", "v",
	    "w", "x", "y", "z", "0", "1", "2", "3",
	    "4", "5", "6", "7", "8", "9", "+", "/"];

	var base64DecodeChars = [
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
	    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
	    -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
	    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
	    -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
	    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1];
	
	var scriptLoaded = {};
	var loadCallbacks = {};
	
	hike.loadScript = function(src, callback){
		hike.log.debug('[ScriptLoader]Try to load script: ' + src);
		if(scriptLoaded[src] === true){	//loaded
			callback();
			return;
		};
		if(scriptLoaded[src] === false){	//loading
			if(typeof callback == 'function'){
				loadCallbacks[src].push(callback);
			}
			return;
		};
		var doc = document.createElement("script");
		doc.src = src;
		loadCallbacks[src] = [];
		if(typeof callback == 'function'){
			loadCallbacks[src].push(callback);
		}
		doc.onload = function(){
			scriptLoaded[src] = true;
			for(var i=0;i<loadCallbacks[src].length;i++){
				loadCallbacks[src][i](true);
			}
			hike.log.debug('[ScriptLoader]Load script: ' + src + ' successful');
		};
		doc.onerror = function(){
			hike.log.debug('[ScriptLoader]Failed to load script: ' + src);
			scriptLoaded[src] = true;
			for(var i=0;i<loadCallbacks[src].length;i++){
				loadCallbacks[src][i](false);
			}
		};
		scriptLoaded[src] = false;
		document.getElementsByTagName('head')[0].appendChild(doc);
	};
	
})(hike);


(function(hike){
	var baseDomain = 'deve.ghike.in';
	var baseStaticDomain = 'devestatic.ghike.in';
	hike.domain = {
		ROOT_DOMAIN: baseDomain,
		     SOURCE: 'hike',  //  let  SOUCE to be hike or zone 
		DOMAIN_SNS: 'm.' + baseDomain,
		DOMAIN_APPS: 'apps.' + baseDomain,
		DOMAIN_API: 'api.' + baseDomain,
		DOMAIN_ADMIN: 'admin.' + baseDomain,
		DOMAIN_STAT: 'stat.' + baseDomain,
		DOMAIN_STATIC : baseStaticDomain,
		URL_SNS_TOUCH: 'http://m.' + baseDomain + '/touch',
		URL_SNS_PAY: 'http://m.' + baseDomain + '/pay',
		URL_API_V1: 'http://api.' + baseDomain + '/1.0',
		URL_API_V1_SSL: 'https://api.' + baseDomain + '/1.0',
		URL_GADGET: 'http://api.' + baseDomain + '/gadget',
		// smart phone app container
		URL_APPS: 'http://apps.' + baseDomain,
		URL_ADMIN: 'https://admin.' + baseDomain,
		URL_PAY_ADMIN: 'https://admin.' + baseDomain + '/pay',
		URL_APPMGT: 'https://admin.' + baseDomain + '/appManagement',
		URL_STAT_ADMIN: 'https://admin.' + baseDomain + '/stat',
		URL_STAT: 'http://stat.' + baseDomain,
		URL_STAT_SSL: 'https://stat.' + baseDomain,
		URL_STATIC_IMG: 'http://' + baseStaticDomain + '/img',
		URL_STATIC_IMG_SSL: 'https://' + baseStaticDomain + '/img',
		URL_STATIC_UI: 'http://' + baseStaticDomain + '/ui',
		URL_STATIC_UI_SSL: 'https://' + baseStaticDomain + '/ui'
	};
})(hike);



 //oop support 
(function(hike){

	var classpath = {};
	
	var classes = {
		'Object':Object,
		'Array':Array,
		'Number':Number,
		'String':String,
		'Function':Function
	};
	
	var callbacks = {};
	
	var loading = 0;
	
	var ensurePkg = function(className){
		if(typeof className != 'string' || className.indexOf('.') == -1)
			return {
				pkg:window,
				name:className
			};
		var pkgs = className.split('.');
		className = pkgs.pop();
		var pkg = window;
		while(pkgs.length > 0){
			var p = pkgs.shift();
			if(!(p in pkg))
				pkg[p] = {};
			pkg = pkg[p];
		}
		return  {
			pkg:pkg,
			name:className
		};
	};
	
	var require = function(){
		var names = [].slice.call(arguments);
		var cb = undefined;
		if(typeof names[names.length - 1] == 'function')
			cb = names.pop();
		if(names.length == 0)
			return cb();
		loadClasses(names, {
			required:names,
			loaded:new Array(names.length),
			callback:cb
		});
	};
	
	var loadClasses = function(names, handler){
		for(var i=0;i<names.length;i++){
			var className = names[i];
			if(callbacks[className] == undefined){
				callbacks[className] = [handler];
			}else{
				callbacks[className].push(handler);
			}
			if(classes[className] != undefined){
				notifyLoadClass(className, classes[className]);
				continue;
			}else{
				(function(name){
					setTimeout(function(){
						if(classes[name] != undefined){
							notifyLoadClass(name, classes[name]);
							return;
						}
						loading ++ ;
						hike.loadScript(getClassURL(name), function(){
							loading -- ;
						});
					}, 1);
				})(className);
			}
		}
	};
	
	var getClassURL = function(className){
		var ns = className.split('.');
		var pkg = ns.shift();
		return classpath[pkg] + '/' + ns.join('/') + '.js';
	};
	
	var notifyLoadClass = function(className, clazz){
		classes[className] = clazz;
		var handlers = callbacks[className];
		if(handlers == undefined || handlers.length == 0)
			return;
		for(var i=0;i<handlers.length;i++){
			var handler = handlers[i];
			required = handler.required;
			var idx = required.indexOf(className);
			if(idx != -1){
				required[idx] = true;
				handler.loaded[idx] = clazz;
			}
			if(!isRequiredReady(required))
				continue;
			var callback = handler.callback;
			if(callback == undefined)
				continue;
			callback.apply(this, handler.loaded);
		}
		delete callbacks[className];
	};
	
	var isRequiredReady = function(required){
		for(var i=0;i<required.length;i++){
			if(required[i] !== true)
				return false;
		}
		return true;
	};
	
	var defineClass = function(className, superclass, prototype){
		if(getClass(superclass) != undefined){
			createClass(className, superclass, prototype);
			return;
		}
		hike.require(superclass, function(){
			//setTimeout(function(){
			createClass(className, superclass, prototype);
			//}, 10);
		});
	};
	
	var createClass = function(className, superclass, prototype){
		prototype = prototype || {};
		var res = ensurePkg(className);
		var pkg = res.pkg;
		var name = res.name;
		superclass = eval(superclass);
		var constr = ( prototype[name] && prototype[name] != Object )?prototype[name]:(function(){
			this.callSuper.apply(this, arguments);
		});
		constr.superclass = superclass;
		constr.prototype = (function(){
			var tmp = function(){};
			tmp.prototype = superclass.prototype;
			var proto = new tmp();
			for(var i in prototype){
				proto[i] = prototype[i];
				if(typeof proto[i] == 'function')
					proto[i].declaredClass = constr;
			}
			return proto;
		})();
		constr.declaredClass = constr;
		constr.className = className;
		constr.prototype.constructor = constr;
		constr.prototype.callSuper = function(){
			var fn = arguments.callee.caller;
			var targetClass = fn.declaredClass.superclass;
			var supr = targetClass.prototype;
			if(fn == fn.declaredClass){
				targetClass.apply(this, arguments);
				return;
			}
			for(var i in this){
				if(this[i] == fn){
					if(typeof supr[i] == 'function'){
						supr[i].apply(this, arguments);
					}
				}
			}
		};
		if(prototype.statics != null){
			var statics = prototype.statics;
			delete prototype.statics;
			for(var i in statics){
				constr[i] = statics[i];
			}
			if(typeof constr.initialize == 'function'){
				var initializer = constr.initialize;
				delete constr.initialize;
				initializer.call(constr);
			}
		}
		pkg[name] = constr;
		hike.log.debug('Class ' + className + ' has been loaded');
		notifyLoadClass(className, constr);
	};
	
	var getClass = function(className){
		return classes[className];
	};
	
	hike.defineClass = function(){
		if(arguments.length == 2){
			defineClass(arguments[0], 'Object', arguments[1]);
		}else{
			defineClass(arguments[0], arguments[1], arguments[2]);
		}
	};
	
	hike.require = function(){
		var args = arguments;
		require.apply(hike, args);
	};
	
	hike.getClass = function(className){
		return getClass(className);
	};
	
	hike.classpath = function(path){
		classpath = path;
	}
	
	hike.emptyMethod = function(){
		hike.log.debug('Empty method call.');
	};
	
	Function.prototype.delegate = function(scope){
		var that = this;
		return function(){
			that.apply(scope, arguments);
		};
	};
	
})(hike);


//Post message support
(function(hike){

	var proxyFrames = {};
	
	var loadingCallback = {};
	
	var createFrame = function(url, callback){
		if(loadingCallback[url] != undefined){
			loadingCallback[url].push(callback);
			return;
		}
		var iframe = document.createElement("iframe");
		iframe.setAttribute("src", url);
		iframe.style.display = 'none';
		loadingCallback[url] = [];
		loadingCallback[url].push(callback);
		document.body.appendChild(iframe);
		hike.dom.addEventListener(iframe, "load", function(){
				hike.log.info('Proxy ' + url + ' loaded');
				proxyFrames[url] = iframe;
				var cb = loadingCallback[url];
				while(cb.length > 0)
					cb.shift()(iframe);
				delete loadingCallback[url];
		});
		return iframe;
	};
	
	var bindMapping = {};
	
	var inited = false;
	
	var initPostMessage = function(){
		if(inited){
			return;
		}
		hike.dom.addEventListener(window, 'message', onMessageCall);
		inited = true;
	};
	
	var onMessageCall = function(evt){
		var data = evt.data;
		var source = evt.source;
		var origin = evt.origin;
		var content = data.content;
		var action = data.action;
		var token = data.token;
		if(typeof callbackMapping[token] == 'function'){
			hike.log.debug('Receive action ' + action + ' callback');
			callbackMapping[token](content);
		}
		if(typeof bindMapping[action] == 'function')
			bindMapping[action](content, function(returnValue){
				hike.log.debug('Action ' + action + ' return');
				postMessage(source, origin, {
					content:returnValue,
					token:token,
					action:action
				});
			});
	};
	
	var callbackMapping = {};
	
	var postMessage = function(target, domain, data, callback){
		data = data || {};
		data.token = data.token || new Date().getTime();
		if(callback != undefined){
			callbackMapping[data.token] = callback;
		}
		target.postMessage(data, domain);
	};
	
	var Proxy = function(proxyUrl, domain){
		this.domain = domain;
		this.proxyUrl = proxyUrl;
	};
	Proxy.prototype = {
		
		invoke : function(action, data, callback){
			var that = this;
			if(proxyFrames[that.proxyUrl] == undefined){
				createFrame(that.proxyUrl, function(){
					hike.log.debug('Post action ' + action + ' to ' + that.proxyUrl);
					postMessage(proxyFrames[that.proxyUrl].contentWindow, that.domain, {
						content : data,
						action : action
					}, function(){
						hike.log.debug('Post action ' + action + ' callback');
						callback.apply(this, arguments);
					});
				});
			}else{
				hike.log.debug('Post action ' + action + 'to ' + that.proxyUrl);
				postMessage(proxyFrames[that.proxyUrl].contentWindow, that.domain, {
					content : data,
					action : action
				}, function(){
					hike.log.debug('Post action ' + action + ' callback');
					callback.apply(this, arguments);
				});
			}
		}
		
	};
	
	var instances = {};
	
	hike.Proxy = {
		
		bind : function(action, fn){
			bindMapping[action] = fn;
			hike.log.debug('Bind postMessage action ' + action);
		},
		
		unbind : function(action){
			delete bindMapping[action];
			hike.log.debug('Unbind postMessage action ' + action);
		},
		
		getInstance : function(proxyUrl, domain){
			if(instances[proxyUrl] == undefined){
				instances[proxyUrl] = new Proxy(proxyUrl, domain);
			}
			return instances[proxyUrl];
		}
		
	};
	
	initPostMessage();
	
})(hike);

(function(hike){
	
	hike.forwardSNS = function(action, dataset){
		var hash = '#!' + action;
		var state = {};
		hike.extend(state, dataset);
		hash += "?" + hike.util.encodeBase64(hike.util.encodeJson(state));
		window.location = hike.domain.URL_SNS_TOUCH + '/' + hash;
	};
	
})(hike);