hike.defineClass('hike.android.History', 'hike.util.Observable', {
	
	statics : {
		getInstance : function(){
			if(this.instance === undefined){
				this.instance = new this();
			}
			return this.instance;
		}
	},
	
	History : function(){
		this.callSuper();
		this.states = [];
		this.index = -1;
	},
	
	pushState : function(action, dataset){
		this.index = this.index + 1;
		this.states[this.index] = {
			action : action,
			dataset : dataset
		};
		this.states = this.states.slice(0, this.index + 1);
	},
	
	replaceState : function(action, dataset){
		this.states[this.index] = {
			action : action,
			dataset : dataset
		};
	},
	
	clear : function(){
		this.states = [];
		this.index = -1;
	},
	
	isActive : function(actionName){
		return true
	},
	
	getState : function(){
		return this.states[this.index];
	},
	
	setDefaultState : function(state){
		this.defaultState = state;
	},
	
	backward : function(){
		this.index = this.index - 1;
		var state = this.states[this.index];
		if(state != null){
			this.onStateChange(state.action, state.dataset, true);
		}
	},
	
	onStateChange : function(action, dataset, reverse){
		this.trigger('statechange', {
			action:action,
			dataset:dataset,
			reverse:reverse
		});
	}
	
});