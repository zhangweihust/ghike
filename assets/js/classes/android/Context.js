hike.require('hike.android.Cache', 'hike.android.History', function(Cache, History){
	
	var cache = null;
	var dpiLevel = '';
	
	hike.defineClass('hike.android.Context', 'hike.fwk.Context', {
		
		Context : function(executor, viewMgr, config, renderer){
			this.callSuper(executor, viewMgr, config);
			delete this.history.events;
			this.history = History.getInstance();
			this.history.bind('statechange', function(e){
				this.purged = false;
				this.reversing = e.reverse;
				this.forward(e.action, e.dataset);
			}, this);
			this.renderer = renderer;
		},
		
		invokeAction : function(actionName, dataset, pushHistroy){
			hike.log.info('Push Action into Histroy: ' + pushHistroy);
			if(pushHistroy === true){
				this.callSuper(actionName, dataset);
			}else if(pushHistroy === false){
				this.forward(actionName, dataset);
			}else{
				var state = this.history.getState();
				if(state == undefined || state.action != actionName){
					this.callSuper(actionName, dataset);
					return;
				}
				this.reversing = false;
				this.action = {
					name:actionName,
					dataset:dataset
				};
				this.purged = false;
				this.forward(actionName, dataset);
				return;
			}
		},
		
		forward : function(actionName, dataset){
			this.callSuper(actionName, dataset);
			this.logAction = actionName;
			this.logDataset = dataset;
		},
		
		reload : function(){
			var state = History.getInstance().getState();
			if(state == null)
				return;
			this.purged = false;
			this.reloading = true;
			this.forward(state.action, state.dataset);
		},
		
		trigger : function(evt, data){
			this.callSuper(evt, data);
			if(evt == 'afteropenview' && this.reloading){
				this.reloading = false;
			}
		},
		
		getCache : function(){
			var userId = hike.callNative('Account', 'getUserId');
			if(cache == null)
				cache = new Cache((hike.callNative('Platform', 'getVersionName') || '0.0.0') + '_' + userId);
			return cache;
		},
		
		openView : function(name, data){
		    this.currentView = name;
		    this.callSuper(name, data);
		},
		
		getViewName : function(){
			return this.currentView;
		},
		
		getRenderTpl : function(tplName, data, callback){
			return $.tmpl(tplName, data);
		}
		
	});
	
});