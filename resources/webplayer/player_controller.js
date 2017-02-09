var player;
	videojs("web_video", {
		"controls": true,
		"autoplay": true,
		"preload": "auto",
		"fluid": true
	}, function() {
		player = this;
		console.log("Loaded");
	});