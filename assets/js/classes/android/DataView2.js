hike.defineClass('hike.android.DataView2', 'hike.widget.DataView', {

	setHint : function(hint){
		this.reset("<div class='nogame'>" +
			"<div class='tip'>" +hint + "</div>" +
			"<div class='logo'></div>" +
		"</div>", true);
	}
	
});