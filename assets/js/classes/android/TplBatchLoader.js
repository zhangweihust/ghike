(function(){
	
	hike.defineClass('hike.android.TplBatchLoader', {
		
		TplBatchLoader : function(tpls){
			this.tpls = tpls || [];
		},
		loadAllCallback : function(callback,scope){
			this.callback = callback;
			this.scope = scope;
			this.loadAll();
		},
		loadAll : function(){			
			var tpls = this.tpls.shift();
			if(tpls == undefined) {
				if (this.callback && typeof this.callback == 'function') this.callback.call(this.scope || this);
				return;
			}			
			for(var key in tpls){
				this.loadTpl(key,'newsfeed/'+tpls[key],function(name,tpl){
					$.template(name,tpl);
					hike.log.debug('loadAll load ' + name);
					this.loadAll();
				}.delegate(this));
			}
		},
		loadTpl : function(name, src, callback, scope){
			var file = 'file:///android_asset/tpl/' + src + '.html';
			hike.log.debug('Try to load TplBatchLoader ' + src + ' from ' + file);
			$.get(file,  function(content){
				hike.log.debug('Template ' + name + ' loaded from assets folder ');
				callback.call(scope || this, name, content);
			});
		}		
	});	
})();

