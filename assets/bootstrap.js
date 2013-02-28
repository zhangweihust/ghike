hike.log.setLevel('debug');
hike.classpath({
	'hike' : 'file:///android_asset/js/classes'
});
hike.require('hike.android.Launcher', function(Launcher) {
	Launcher.launch();
});