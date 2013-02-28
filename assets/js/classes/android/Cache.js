(function(){
	
	var ss = window.sessionStorage;
	
	var ls = window.localStorage;
	
	var decode = function(item, key){
		return hike.util.decodeJson(item)[key];
	};
	
	var encode = function(key, value){
		var item = {};
		item[key] = value;
		return hike.util.encodeJson(item).replace(/\r/g, '\\r').replace(/\n/g, '\\n').replace(/"/g, '\\"');
	};
	
	hike.defineClass('hike.android.Cache', {
		
		Cache : function(version){
			this.version = version;
		},
		
		get : function(key){
			key = key + '_' + this.version;
			var item = ss.getItem(key) || ls.getItem(key);
			if(item == null)
				return null;
			try{
				hike.log.debug('Get from cache: ' + key);
				return decode(item, key);
			}catch(e){
				hike.log.error('Failed to decode cache: ' + key);
				return null;
			}
		},
		
		set : function(key, value){
			key = key + '_' + this.version;
			ss.setItem(key, encode(key, value));
		},
		
		persist : function(key, value){
			key = key + '_' + this.version;
			ls.setItem(key, encode(key, value));
		},
		
		clear : function(key){
			key = key + '_' + this.version;
			ss.removeItem(key);
			ls.removeItem(key);
		},
		
		clearAll : function(){
			ls.clear();
			ss.clear();
		}
		
	});
	
})();

