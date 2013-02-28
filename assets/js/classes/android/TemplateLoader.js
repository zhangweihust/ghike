(function(){
	
	hike.defineClass('hike.android.TemplateLoader', {
		
		TemplateLoader : function(tpls){
						
		},
		
		loadTpl : function(name, callback, scope){
			var file = 'file:///android_asset/tpl/' + name + '.html';
			hike.log.debug('Try to load template ' + name + ' from ' + file);
			$.get(file,  function(content){
				hike.log.debug('Template ' + name + ' loaded from assets folder: ');
				callback.call(scope || this, name, content);
			});
		}	
	});	
})();

