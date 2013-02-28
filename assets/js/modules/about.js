(function(){
	
	hike.registerAction('about.version', function(dataset, ctx){

		var versionName = hike.callNative('Platform', 'getVersionName')||'0.0.0';
		hike.log.info("zhw --- " + versionName);
		ctx.openView('about/version', {'versionName':versionName});
	});
	
	
	hike.registerAction('about.checkversion', function(dataset, ctx){
		hike.log.info("callNative  --- requestCheckUpdatesDialog");
		hike.callNative('Platform', 'requestCheckUpdatesDialog', true);
		
	});
	
})();