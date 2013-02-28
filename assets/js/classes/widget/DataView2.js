hike.defineClass('hike.widget.DataView2', 'hike.widget.DataView', {

	setHint : function(hint){
		this.reset("<div class='hint'>" +
			"<div class='tip'>" +hint + "</div>" +
			"<div class='logo'></div>" +
		"</div>", true);
	}
	
});