(function(){
	
	var storage = window.sessionStorage;
	
	hike.defineClass('hike.widget.NativeTab', 'hike.widget.Widget', {
		
		NativeTab : function(el, context){
			this.callSuper(el, context);
			this.initTabs(this.element.getDataset());
			this.initProxy();
		},
		
		initTabs : function(ds){
			var r = this.getContext().reversing || this.getContext().reloading;
			this.pages = $(this.element).children();
			this.showTip = (ds.showTip == 'true');
			var viewName = this.getContext().getViewName();
			var savedIndex = storage.getItem("NativeTab@" + viewName);
			if(savedIndex != null){
				savedIndex == parseInt(savedIndex);
			}
			var at = ds['activeTab'];
			at = at ? parseInt(at) : 0;
			this.tabs = [];
			this.pages.each(function(i, p){
				this.tabs.push(p.getDataset()['title']);
			}.delegate(this));
			this.setActiveTab(r?(savedIndex || at):at);
		},
		
		initProxy : function(){
			UIProxy.setActiveTab = this.setActiveTab.delegate(this);
			hike.callNative('NativeUI', 'setTabs', this.tabs.join('_'), this.index, this.showTip);
		},
		
		setActiveTab : function(index){
			this.index = index;
			this.pages.each(function(i, p){
				if(i != this.index){
					$(p).css('display', 'none');
				}else{
					$(p).css('display', 'block');
				}
			}.delegate(this));
			var viewName = this.getContext().getViewName();
			storage.setItem("NativeTab@" + viewName, index);
			this.trigger('tabchange', this);
		}
		
	});
})();