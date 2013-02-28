(function() {
	hike.registerAction('challenge.request.detail', function(dataset, ctx) {
		var challengeId = dataset["challengeId"];
		var url = "challenge/request/detail?challengeId=" + challengeId;
		ctx.get(url, null, {
			"0" : function(data) {
            	var challengeRequestCount = data.challengeRequestCount;
        		var tabGroup = ctx.getWidget('gameNotify');
        		tabGroup.setTitle(0, 'Challenges(<em>'+challengeRequestCount+'</em>)');
				ctx.openView('notify/detail-gamecompete', data);
			}
		});
	});

	hike.registerAction('challenge.request.later', function(dataset, ctx) {
		var challengeId = dataset["challengeId"];
		var url = "challenge/request/later?challengeId=" + challengeId;
		ctx.get(url, null, {
			"0" : function(data) {
            	var challengeRequestCount = data.challengeRequestCount;
        		var tabGroup = ctx.getWidget('gameNotify');
        		tabGroup.setTitle(0, 'Challenges(<em>'+challengeRequestCount+'</em>)');
        		ctx.forward("notify.game");
			}
		});
	});

	hike.registerAction('challenge.later', function(dataset, ctx) {
		var challengeId = dataset["challengeId"];
		var url = "challenge/request/later?challengeId=" + challengeId;
		ctx.get(url, null, {
			"0" : function(data) {
            	var challengeRequestCount = data.challengeRequestCount;
        		var tabGroup = ctx.getWidget('gameNotify');
        		tabGroup.setTitle(0, 'Challenges(<em>'+challengeRequestCount+'</em>)');
        		ctx.forward("notify.game");
			}
		});
	});

	hike.registerAction('challenge.request.reject', function(dataset, ctx) {
		var challengeId = dataset["challengeId"];
		var url = "challenge/request/reject?challengeId=" + challengeId;
		ctx.get(url, null, {
			"0" : function(data) {
				var challengeRequestCount = data.challengeRequestCount;
				var tabGroup = ctx.getWidget('gameNotify');
				tabGroup.setTitle(0, 'Challenges(<em>'+challengeRequestCount+'</em>)');
				ctx.forward("notify.game");
			},
		});
	});
	
	hike.registerAction('challenge.request.ignore', function(dataset, ctx) {
		var challengeId = dataset["challengeId"];
		var url = "challenge/request/ignore?challengeId=" + challengeId;
		var cache = ctx.getCache();
		var cacheData = cache.get("allNotifyCount");
		ctx.get(url, null, {
			"0" : function(data) {
	     		var ignore = $('div[name="ignore'+challengeId+'"]');
	    		ignore.removeAttr('data-action');
	    		ignore.removeClass('del');
	    		ignore.addClass('ignored');
	    		ignore.html("<span class='btn disablebtn'>Ignored</span>");
	    		$('div[name="play'+challengeId+'"]').hide();
	    		$('li[name="unread'+challengeId+'"]').removeClass('unread');
//        		var ignore = $('span[name="ignore'+challengeId+'"]');
//        		var unread = $('li[name="unread'+challengeId+'"]');
//        		unread.removeClass('unread');
//        		ignore.removeAttr('data-action');
//        		ignore.removeClass('graybtn del');
//        		ignore.addClass('disablebtn ignored');
//        		ignore.html('Ignored');
//        		$('span[name="play'+challengeId+'"]').hide();
        		if(data.challengeRequest.status == 0){
        			hike.log.info('challengeRequestStatus='+data.challengeRequest.status);
        			var challengeRequestCount = parseInt(cacheData.gameNotifyUnreadCount)-parseInt('1');
        			hike.callNative('NativeUI', 'setNotifyCount',cacheData.friendNotifyUnreadCount,challengeRequestCount,cacheData.messageNotifyUnreadCount,cacheData.total);
        			hike.log.info('challengeRequestCount minus one'+challengeRequestCount);
        		}
        		ctx.forward('notify.updateAllGameNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
			},
		}, true);
	});


	hike.registerAction('challenge.result.detail', function(dataset, ctx) {
		var resultId = dataset["resultId"];
		var url = "challenge/result/detail?resultId=" + resultId;
		ctx.get(url, null, {
			"0" : function(data) {
            	var challengeResultCount = data.challengeResultCount;
        		var tabGroup = ctx.getWidget('gameNotify');
        		tabGroup.setTitle(2, 'Results(<em>'+challengeResultCount+'</em>)');
				ctx.openView('notify/detail-gameresult', data);
			}
		});
	});

	hike.registerAction('challenge.result.delete', function(dataset, ctx) {
		var challengeId = dataset["challengeId"];
		var url = "challenge/result/delete?challengeId=" + challengeId;
		ctx.get(url, null, {
			"0" : function(data) {
            	var challengeResultCount = data.challengeResultCount;
        		var tabGroup = ctx.getWidget('gameNotify');
        		tabGroup.setTitle(2, 'Results(<em>'+challengeResultCount+'</em>)');
            	ctx.forward("challenge.result.back");
			}
		});
	});
	
	hike.registerAction('challenge.result.back', function(dataset, ctx) {
		hike.backward();
	});
})();