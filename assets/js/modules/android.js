(function(){
	appManagerProxy={};
	appManagerProxy.onFinishedInstall = function(){};
	appManagerProxy.onBeginDownload = function(){};
	appManagerProxy.onFinishedDownload = function(){};
	appManagerProxy.onDownloadError = function(){};
	appManagerProxy.onStartInstall = function(){};
	appManagerProxy.onDownloadProgress = function(){};
	hike.registerRequestInterceptor(/^game\/\d*$/, {
		after : function(invoker){
			if(invoker.result.game.platformType==1)
			{
				var packageName = invoker.result.game.packageName;
				var isInstall = hike.callNative('AppManager', 'isAppInstalled', packageName) == 'true';
				//var isInstall = false;
				invoker.result['isInstall']=isInstall;
				invoker.result['isApp'] = true;
			}else{
				invoker.result['isInstall']=true;
				invoker.result['isApp'] = false;
			}
			invoker.next();
		}
	});
	
	hike.registerRequestInterceptor(/^profile\/\d*\/getRecentlyPlayed$/, {
		after : function(invoker){
			var app ="";
			dataView = invoker.context.getWidget("pofilerecentlyplayed");
			dataView.preload();
			var data = invoker.result;
			if (data.recentlyPlayed != null && data.recentlyPlayed.length > 0) {
				var recentlyPlayedStr="" ;
                var list = data.recentlyPlayed;
                var len = list.length;                
                var boardStr = "",isInstall;
                for (var i = 0; i < len; i++) {
                var item = list[i];
                var scorestr = "" ; 
                if(item.score>0){
                	scorestr =  item.score + "points " 
                }
                var action ="android.play";
                if(item['platformType']==1){
                	app ="<span class='app'>app</span>";
                	var isInstall = hike.callNative('AppManager', 'isAppInstalled', item['packageName']);					  
					  if(isInstall == true){
						 action = "android.play";
					  }else{
						 action = "android.pofilerecentlyplayeddownload";
					  }	
                 }else{
                	 app="";
                	 action = "android.htmlplay";
                 }
                recentlyPlayedStr += '<li data-widget="Button" data-game-title="'+item.title+'" data-downloadurl="'+item.downloadUrl+'" data-package-name="'+item.packageName+'" data-action="'+action+'"  data-game-id = "'+item.gameId+'" ><div class="wrapper">'
                	+ app
                	+ '<img src="' + hike.domain.URL_STATIC_IMG + '/' + item.appIconCover.touchIcon + '" />'
                	+ '<div class="info"><div class="name">' + item.title + '</div><div class="mutual">' + scorestr + '</div></div>'
                	+ '</div></li>';
                }
                dataView.append(recentlyPlayedStr,data.hasNextPage);
                }else{
                	var list = data.featuredGame;
                    var len = list.length;
                    var boardStr = '<div class="itemtitle">Featured Game</div>';
                    for (var i = 0; i < len; i++) {
                        var item = list[i];
                        if(item['platformType']==1){
                        	app ="<span class='app'>app</span>";
                        	var isInstall = hike.callNative('AppManager', 'isAppInstalled', item['packageName']);					  
    						  if(isInstall == true){
    							 action = "android.play";
    						  }else{
    							 action = "android.pofilerecentlyplayeddownload";
    						  }	
                         }else{
                        	 app="";
                        	 action = "android.htmlplay";
                         }
                        boardStr += '<li data-widget="Button" data-action="'+action+'"  data-game-id = "'+item.appId+'" ><div class="wrapper">'
                            + app
                            + '<img src="' + hike.domain.URL_STATIC_IMG + '/' + item.appIconCover.touchIcon + '" />'
                        	+ '<div class="info"><div class="name">' + item.title + '</div></div>'
                        	+ '</div></li>';
                    }
                    dataView.append(boardStr,data.hasNextPage);
                }
            }
	    });

	/*hike.registerAction('android.gameinstallpop', function(dataset, ctx){
		var downloadurl = dataset['downloadurl'].indexOf('http')>-1 ? dataset['downloadurl'] : hike.domain.URL_STATIC_UI+dataset['downloadurl'];
		hike.callNative('AppManager', 'install', dataset['gameTitle'], dataset['packageName'], downloadurl);
		var btn = $(".gamelist").find("[data-game-id='" + dataset['gameId'] + "']");
	    appManagerProxy.onFinishedInstall = function(){
			hike.ui.closeDialog();
			btn.data('action','android.play');
	    }
	});*/
	
	hike.registerAction('android.pofilerecentlyplayedinstallpop', function(dataset, ctx){
		var downloadurl = dataset['downloadurl'].indexOf('http')>-1 ? dataset['downloadurl'] : hike.domain.URL_STATIC_UI+dataset['downloadurl'];
		hike.callNative('AppManager', 'install', dataset['gameTitle'], dataset['packageName'], downloadurl);
		var btn = $(".friend-list").find("[data-game-id='" + dataset['gameId'] + "']");
	    appManagerProxy.onFinishedInstall = function(){
			hike.ui.closeDialog();
			btn.data('action','android.play');
	    }
	});
	
	/*hike.registerAction('android.pofilenewsfeedinstallpop', function(dataset, ctx){
		var downloadurl = dataset['downloadurl'].indexOf('http')>-1 ? dataset['downloadurl'] : hike.domain.URL_STATIC_UI+dataset['downloadurl'];
		hike.callNative('AppManager', 'install', dataset['gameTitle'], dataset['packageName'], downloadurl);
		var btn = $(".news-list .detail").find("[data-game-id='" + dataset['gameId'] + "']");
	    appManagerProxy.onFinishedInstall = function(){
			hike.ui.closeDialog();
			btn.data('action','android.play');
	    }
	});*/
	
	hike.registerAction('android.pofilerecentlyplayeddownload', function(dataset, ctx){		
		var config = {
				title : '',
				closable : true,
				content : '<div class="android">This game need to be downloaded frist.If you have downloaded.Please open the app to play. Thanks!'+				  
						  '<div class="downloadbtn"><input data-game-id="'+dataset['gameId']+'" data-widget-id="downloadbtn" data-game-title="'+dataset['gameTitle']+'" data-package-name="'+dataset['packageName']+'" data-downloadurl="'+dataset['downloadurl']+'" type="button" data-action="android.pofilerecentlyplayedinstallpop" value="Download" class="greenbtn" /></div>'
			};
			hike.ui.showDialog(config);
	});
	
	/*hike.registerAction('android.htmlChallenge', function(dataset, ctx){
        var gameId=dataset["gameId"];
        var challengeType=dataset["challengeType"];
        var requestId=dataset["requestId"];
        var requestScore=dataset["requestScore"];
        var challengeId = dataset["challengeId"];
        var play="play/"+gameId+"/new";
        ctx.get(play,{
           "challengeType":challengeType,
            "requestId":requestId,
            "requestScore":requestScore,
            "challengeId":challengeId
        },{
            "0":function(data){
            	hike.callNative('AppManager', 'startURL', data.playUrl+"?challengeId="+challengeId);
            },
            "1":function(){
                alert("error");
            },
            "101":function(){
                ctx.forward("home.launch");
            },"301":function(){
                alert("Game does not exist");
            }
        });
    });*/
	
	/*hike.registerAction('android.onChallenge', function(dataset, ctx){
        var gameId=dataset["gameId"];
        var challengeType=dataset["challengeType"];
        var requestId=dataset["requestId"];
        var requestScore=dataset["requestScore"];
        var challengeId = dataset["challengeId"];
        var platformType = dataset["platformType"];
        var packageName = dataset["packageName"];
        var play="play/"+gameId;
		 if(platformType == 1 && (hike.callNative('AppManager', 'isAppInstalled', packageName) != 'true')){
	    	var detailUrl="game/"+gameId;
	        ctx.get(detailUrl,null,function(data){
	            data.animation='fade';
	            ctx.invokeAction('notify.clearChallengeRequestCache');
	            ctx.invokeAction('notify.clearAllGameNotifyCache');
	            ctx.invokeAction('notify.updateGameCountCache');
	            ctx.openView('game/game-detail',data);
	        });
          }else if(platformType == 1 && (hike.callNative('AppManager', 'isAppInstalled', packageName) == 'true')){
            $("#challenge_"+challengeId).html('<input type="button" class="weak" value="Accepted">');
        	ctx.get(play,{
  	           "challengeType":challengeType,
  	            "requestId":requestId,
  	            "requestScore":requestScore,
  	            "challengeId":challengeId
  	        },{
  	            "0":function(data){
  	            	ctx.invokeAction('notify.clearChallengeRequestCache');
  	            	ctx.invokeAction('notify.clearAllGameNotifyCache');
  	            	ctx.invokeAction('notify.updateGameCountCache');
  	            	hike.callNative('AppManager', 'startForCompete', challengeId, packageName);
  	            },
  	            "1":function(){
  	                alert("error");
  	            },
  	            "101":function(){
  	                ctx.forward("home.launch");
  	            },"301":function(){
  	                alert("Game does not exist");
  	            }
  	        });
        }else{
        	$("#challenge_"+challengeId).html('<input type="button" class="weak" value="Accepted">');   
	        ctx.get(play,{
	           "challengeType":challengeType,
	            "requestId":requestId,
	            "requestScore":requestScore,
	            "challengeId":challengeId
	        },{
	            "0":function(data){
		             ctx.invokeAction('notify.clearChallengeRequestCache');
		             ctx.invokeAction('notify.clearAllGameNotifyCache');
		             ctx.invokeAction('notify.updateGameCountCache');
		             hike.callNative('AppManager', 'startURL', data.playUrl+"?challengeId="+challengeId);
	            },
	            "1":function(){
	                alert("error");
	            },
	            "101":function(){
	                ctx.forward("home.launch");
	            },"301":function(){
	                alert("Game does not exist");
	            }
	        });
        }
    });*/
	
})();
