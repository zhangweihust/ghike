(function(){
	
	hike.registerAction('games.openGameIndex', function(dataset, ctx){
            ctx.openView('game/categories', null);
	});
	
	hike.registerAction('games.screenshot', function(dataset, ctx) {
		var urlId = dataset["urlId"];
		
		if (urlId.length = 0) {
			return;
		}
		var index = dataset.index;
		var urls ="" ;
		var strs = new Array();
		strs = urlId.split("|");
		for (i = 0; i < strs.length; i++) {
			if(strs[i].length==0){
				continue;
			}
			urls += "%"+hike.domain.URL_STATIC_IMG + "/" + strs[i];
		}
		hike.callNative('NativeUI', 'previewImages', urls.substring(1), index);
	});
	
	hike.registerAction('games.mygames', function(dataset, ctx){
		var games = hike.util.decodeJson(hike.callNative('LocalGames', 'getAllGameJson')); 
		ctx.openView('game/game-mygames',dataset);
	});
	
	hike.registerAction('games.openMyGamelist', function(dataView, ctx){
		//*** The data page render encapsulated into a separate method 
		var randerListTpl = function(data){
			var entry={},gameStr='';
			for(var w =0,len = data.length; w < len; w++){
				 entry= data[w];
				 if(undefined == entry['appIconCover'].touchIcon){
					 icon = hike.util.decodeJson(entry['appIcon']).touchIcon;
				 }else{
					 icon = entry['appIconCover'].touchIcon;
				 }
				 if(w==0 || w % 4 == 0){
					 gameStr += "<li>" ;
				 }
				  if(entry['platformType']==1)
					{
					 var action = 'games.tryToPlay';
					 gameStr += "<div class='item' data-widget='MultiMenu'>"
					 + "<img src='" + hike.domain.URL_STATIC_IMG + "/" + icon + "'/>"
	                + "<span class='name'>" + entry['title'] + "</span>"
	                + "<div class='inner'><div class='item' data-game-title='"+entry["title"]+"' data-downloadurl='"+entry["downloadUrl"]+"' "
	                + "data-package-name='"+entry['packageName']+"' data-game-type='"+entry['platformType']+"' data-action='"+action+"' "
	                + "data-game-id='"+entry['appId']+"'><span class='btn'></span><span class='des'>Play</span></div>"
	                + "<div class='item' data-action='games.openDetail' data-game-id='"+entry['appId']+"'><span class='btn'></span><span class='des'>Activity</span></div></div>"
	                + "</div>";
					}else{
					gameStr += "<div class='item' data-widget='MultiMenu'>"
					 + "<img src='" + hike.domain.URL_STATIC_IMG + "/" + icon + "'/>"
	                + "<span class='name'>" + entry['title'] + "</span>"
	                + "<div class='inner'>" 
	                + "<div class='item' data-action='games.htmlplay' data-game-id='"+entry['appId']+"'><span class='btn'></span><span class='des'>Play</span></div>"
	                + "<div class='item' data-action='games.openDetail' data-game-id='"+entry['appId']+"'><span class='btn'></span><span class='des'>Activity</span></div></div>"
	                + "</div>";
	                }
				  if((w+1) % 4 == 0){
					  gameStr += "</li>" ;
	       	  }
			 }
			return gameStr ; 
		};
		
		dataView.preload();
		var url = "android/myGames";		
		var myGameListcache = ctx.getCache();	
		var myListCache = myGameListcache.get("myGameList") || null;
		if(null !== myListCache && '' !== myListCache && myListCache.length > 0){
			var dataViewContent = randerListTpl(myListCache);
			dataView.append(dataViewContent,true);
		}
		//*** For local installation all game json string
		var installedGames = hike.util.decodeJson(hike.callNative('LocalGames', 'getAllInstalledGameJson'));
		ctx.get(url,null,{"0":function(data){
			var gameList = data['recentlyPlayed'],
			 	i=0,
			 	o=0,
			 	installGame,
			 	recentGame,
			 	len=installedGames.length,
			    flag = false; 
			 
			 //*** Merge data and filter repetitive data 
			 if(gameList != null && gameList.length >0 && len >0){
				 for(var g =0;g<len;g++){
					 installGame = installedGames[g];
					 flag = false;
					   for(var jj=0; jj<gameList.length; jj++){
						   recentGame = gameList[jj];
						   if(installGame.appId ==recentGame.appId){
							   flag= true;
							   break;
						   }
					   }
					   if(!flag){
						  gameList[gameList.length] = installGame;
						  continue; 
					   }
				 }
			 }else if((gameList == null || gameList.length==0) && len >0){
				 gameList = installedGames;
			 }
			 //**** Not installed android game also did not play any games
			 if((gameList ==null || gameList.length == 0) && installedGames.length == 0){
				 var defaultStr ="";
				 defaultStr += "<div class='nogame'>"
				 + "<div class='tip'>You haven't installed or played any game yet.</div>"
				 + "<div class='logo'></div>"
				 + "</div>"
				 dataView.append(defaultStr,true);
			 }
			//**** If the data have updated the update cache
            if(gameList != myListCache){
				var dataViewContent = randerListTpl(gameList);
				dataView.reset(dataViewContent,true);
				myGameListcache.persist("myGameList",gameList); 
			}
			 
		}
		},true);
		
	});
	
	hike.registerAction('games.tryToPlay', function(dataset, ctx){
		var isInstall = hike.callNative('AppManager', 'isAppInstalled', dataset['packageName']);
		if(isInstall == 'true'){
			ctx.forward('games.play', dataset);
		}else{
			ctx.forward('games.openDetail', dataset);
		}
	});
	
	hike.registerAction('games.playFriends', function(dataset, ctx){
		var gameId = dataset["gameId"];
        ctx.openView('game/playfriends', dataset);
	});
	
	hike.registerAction('games.my-friends', function(dataView, ctx){
	var url = "android/invite-friends/" + dataView["gameId"];
	var gameId = dataView["gameId"];
	dataView.preload();
    ctx.get(url, {
        "page":dataView["curPage"],
        "pagesize":dataView["pageSize"]
    },{
        "0":function(data) {
            if (data.list != null && data.list.length > 0) {
                var list = data.list;
                var len = list.length;
                var boardStr = "";
                for (var i = 0; i < len; i++) {
                    var item = list[i];
                    var addId = "add"+item.id ;
                    var sentId = "sent"+item.id ; 
                    boardStr += '<li><div class="wrapper">'
                    	+ '<img src="' + item.headUrl + '" data-action="profile.friends" data-user-id="' + item.id + '">'
                    	+ '<div class="info" data-action="profile.friends" data-user-id="' + item.id + '">'
                    	+ '<div class="name">' + item.nickName+'</div>'
        			    + '</div>'
        			    + '<div id = "'+addId+'"  class="gameinvite" data-action="games.inviteSent" data-game-id="'+gameId+'" data-friend-id="'+item.id+'"><span class="bluebtn invite">Invite</span></div>'
        			    + '<div id = "'+sentId+'" style ="display:none" class="gameinvite" ><a href="javascript:void(0)" class="disablebtn">Request Sent</a></div>'
						+ '</div></li>';
                }
                dataView.append(boardStr,data.notHasNextPage);
            }else{
            	dataView.setHint("You have no friend yet.") ;
	            }
	        }
	    }, true);
	}, true);
	
	hike.registerAction('games.inviteSent', function(dataset, ctx){	
		var gameId = dataset["gameId"];
		var friendIds = dataset["friendId"];
		var url = "android/invite/"+gameId;
		ctx.get(url, {"gameId":gameId,"inviteId":friendIds}, {
			"0": function(data) {
			  $("#add"+friendIds).hide();
			  $("#sent"+friendIds).show();
			},
			"1008": function(data) {
				hike.ui.showError('Invite user donot accept request !');
				return;
			},		
		    "1001": function(data) {
		    	hike.ui.showError("Invite user is donot exist.");
		    	return;
			},
			"1006": function(data) {
				hike.ui.showError("Can not invite yourself.");
				return;
			},
			"1007": function(data) {
				hike.ui.showError("Invite request has been send.");
				return;
			}
		})

	});
	
	hike.registerAction('games.pupdownload', function(dataset, ctx){	
		var gameId = dataset['gameId'];
		//hike.callNative("NativeUI", "requestAlert", "Message", "Please download this game first and then come back to challenge again!");
		hike.callNative("NativeUI", "requestDialog", {
			title : 'Message',
			text : 'Please download this game first and then come back to challenge again!',
			confirmButton : 'OK'
		});
		window.onDialogConfirm  = function(){
			ctx.invokeAction("games.openDetail", {'gameId':gameId} );
		};
		window.onDialogCancel = function(){};
		
	});
	var gamePackage;
	
	hike.registerAction('games.openDetail', function(dataset, ctx){
		var gameId=dataset["gameId"];
		 //validate game
		var valiUrl = "game/"+gameId+"/status";
		ctx.get(valiUrl,null,{
		        	"0":function(){
		        		 var detailUrl="android/game/"+gameId;
		        	        ctx.get(detailUrl,null,function(data){
		        	            data.animation='fade';
		        	            var game = data.game;
		        	            var gameUserList = data.gameUserList;
		        	            var gameTemp = new Array();
		        	            if(gameUserList != null && gameUserList.length < 3)
		        	            {
		        	            	for(var v = 0;v < 3; v++){
		        	            		if(gameUserList.length-1 >= v){
		        	            			continue;
		        	            		}else{
		        	            			gameTemp.headUrl = "file:///android_asset/img/person_place_holder.png";
		        	            			gameUserList.push(gameTemp);
		        	            		}
		        	            	}
		        	            	data.gameUserList = gameUserList;
		        	            }
		        				gamePackage = game.packageName || null;
		        	            if(game.platformType==1)
		        				{
		        	            	packageName = game.packageName;
		        					var isInstall = hike.callNative('AppManager', 'isAppInstalled', packageName) == 'true';
		        					data.isInstall = isInstall;
		        					data.isApp = true;
		        					data.progress = parseInt(hike.callNative('AppManager', 'getProgress', packageName));
		        				}else{
		        					data.isInstall = true;
		        					data.isApp = false;
		        					data.progress = -1;
		        				}
		        	            	ctx.openView('game/game-detail',data);
		        	        });
		        	},"1009":function(data){
		        	ctx.openView('game/game-remove',data);
		        	}
		});
	});
	
	 hike.registerAction('games.openTopicList', function(dataView, ctx){
		   var gameId=dataView["gameId"];
	        var url = "game/topic/list/"+gameId +"/new";
	        dataView.preload();
	        ctx.get(url, {
	            "page":dataView["curPage"],
	            "pageSize":dataView["pageSize"]
	        }, {
	            "0":function(data) {
	            	if(data.list!=null && data.list.length>0){
	            		var str = ""; 
	            		$.each(data.list,function(entryIndex,entry){
	            			var topicContent = entry.topic.contentStyle ; 
	            			var userSaid = "<span class='name' data-action ='profile.friends' data-user-id='"+entry.user.id+"' >"+entry.user.nickName+"</span> <em>said</em>";
	            			var topicAction ="data-action='topic.discusstion'";
	            			var replyValue ="<aside class='comment' data-action='topic.discusstion' data-topic-id='" + entry.topic.topicId + "'><span>"+entry.topic.replyCount+"</span></aside>";
	            			if(topicContent.indexOf("sys") > -1) {
	            				topicContent = topicContent.replace('sys','');
	            				topicContent = topicContent.replace('sayed:','');
	            				userSaid = "<span class='name'> &nbsp;</span>";
	            				topicAction = "";
	            				replyValue = "";
	            			}else{
	            				topicContent = topicContent.replace(/data-action="profile.friends"/g,'');	
	            			}
							 str += "<li>"
								 + "<h2>"
							     + userSaid + "<aside class='time'>"+entry.topic.createTimeFam+"</aside>"
		                         + "</h2>"
		                         + "<div class='content'>"
		                         + "<img class='avata' src='" + entry.user.headUrl + "' data-widget='Button' data-action ='profile.friends' data-user-id='"+entry.user.id+"' />"
			    				 + "<div " + topicAction + " data-topic-id='" + entry.topic.topicId + "'><div class='detail' data-widget='TimelineContent'>"
			    				 + topicContent +"</div></div>"
		                         + "<div class='legend' >"
			    				 + replyValue
			    				 + "</div>"
			    				 + "</div>"
		                         + "</li>";
			                });
	            		 dataView.append(str,data.isHasNextPage);
	                     ctx.updateView();
	                 } else{
	                 	dataView.append('',true);
	                 }
	            },
	            "103":function() {
	                $('.error').html('User does not exist!');
					$('.error').show();
	            }
	        }, true);
	    });
	 
    hike.registerAction('games.initPlay',function(dataset,ctx){
        $(document).trigger('startplay',{"gameId":dataset["gameId"]});
    });
    hike.registerAction('games.finishPlay',function(dataset,ctx){
        $(document).trigger('endplay',{"gameId":dataset["gameId"]});
    });
    
    hike.registerAction('games.challenge', function(dataset, ctx){
        var gameId=dataset["gameId"];
      //validate game
        var valiUrl = "game/"+gameId+"/status";
        ctx.get(valiUrl,null,{
                	"0":function(){
                		  var challengeType=dataset["challengeType"];
                	        var requestId=dataset["requestId"];
                	        var requestScore=dataset["requestScore"];
                	        var challengeId = 0;
                	        var platformType = dataset["platformType"];
                	        var packageName = dataset["packageName"];
                	        var play = "android/play/"+gameId+"/"+challengeType+"/"+requestId+"/"+requestScore+"/"+challengeId+ "/new";
                	        //var play = "play/"+gameId;
                	        if(platformType == 1 && (hike.callNative('AppManager', 'isAppInstalled', packageName) != 'true')){
                	        	ctx.forward('games.openDetail', {
          		            	  gameId : gameId
          		            	});
                	        }else{
                		        ctx.get(play,{
                		           "challengeType":challengeType,
                		            "requestId":requestId,
                		            "requestScore":requestScore,
                		            "challengeId":challengeId
                		        },{
                		        	"0":function(data){
                		            	if(platformType == 1){
                		            		hike.callNative('AppManager', 'startForCompete', challengeId, packageName);
                		            	}else{
                			            	hike.callNative('AppManager', 'startURL', data.playUrl+"?challengeId="+challengeId);
                		            	}
                		            	ctx.forward('notify.clearNewsfeedCache');
                		            	 //  clean the basicInfoCache  cache  and  persist it  
                			            var url = "andriodprofile";
                			            var cache = ctx.getCache();
                			            ctx.get(url, null, function(data) {
                		    			cache.clear("basicInfoCache");
                						cache.persist("basicInfoCache",data);
                					    })
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
                	},"1009":function(data) {
                	ctx.openView('game/game-remove',data);
                	}
                });
    });
    
    hike.registerAction('games.play', function(dataset, ctx){
		var packageName = dataset.packageName;
		var gameId = dataset.gameId;
		//validate game
		var valiUrl = "game/"+gameId+"/status";
		ctx.get(valiUrl,null,{
		        	"0":function(){
		        		hike.callNative('AppManager', 'start', packageName);
		                ctx.get("play/" + gameId + "/new", null, {
		                    "0":function(data){
		                    	if(ctx.getViewName()=="game/game-mygames"){
		                    		ctx.getCache().persist("myGameList", null);
		                    		ctx.reload();
		                    	}
		                    	if(ctx.getViewName()=="game/game-detail"){
		                    		ctx.reload();
		                    	}
		                    	if(data.firstPlay){
		                    		ctx.forward('notify.clearNewsfeedCache');
		                    	}
		                    //  clean the basicInfoCache  cache  and  persist it  
		        	            var url = "andriodprofile";
		        	            var cache = ctx.getCache();
		        	            ctx.get(url, null, function(data) {
		            			cache.clear("basicInfoCache");
		        				cache.persist("basicInfoCache",data);
		        			    })
		                    },
		                    "1":function(){},
		                    "101":function(){},"301":function(){}
		                });
		        	},"1009":function(data) {
		        	 ctx.openView('game/game-remove',data);
		        	}
		 });
	}); 
    
    hike.registerAction('games.htmlplay', function(dataset, ctx){	
		var gameId=dataset["gameId"];
		//validate game
		var valiUrl = "game/"+gameId+"/status";
		var isZone = hike.domain.SOURCE; 
		var urlPath = "";
		ctx.get(valiUrl,null,{
		        	"0":function(){
		        		 var play = "play/" + gameId + "/new";
		        	        ctx.get(play,null,{
		        	            "0":function(data){
		        	            	if("zone" != isZone){
		        	            		urlPath = '?source=ghike';
		        	            	}
		        	            	hike.callNative('AppManager', 'startURL', data.playUrl + urlPath);
		        	            	if(ctx.getViewName()=="game/game-mygames"){
		        	            		ctx.getCache().persist("myGameList", null);
		        	             		ctx.reload();
		        	             	 }
		        	            	if(ctx.getViewName()=="game/game-detail"){
			                    		ctx.reload();
			                    	}
		        	            	if(data.firstPlay){
			                    		ctx.forward('notify.clearNewsfeedCache');
			                    	}
		        	            //  clean the basicInfoCache  cache  and  persist it  
			        	            var url = "andriodprofile";
			        	            var cache = ctx.getCache();
			        	            ctx.get(url, null, function(data) {
			            			cache.clear("basicInfoCache");
			        				cache.persist("basicInfoCache",data);
			        			    })
		        	            },
		        	            "1":function(){},
		        	            "101":function(){},
		        	            "301":function(){}
		        	        });
		        	},"1009":function(data) {
		        	 ctx.openView('game/game-remove',data);
		        	}
		 });
	});
    
	window.onBeginDownload = function(evt){
		if(evt.packageName != gamePackage)
			return;
		$("#downloadGame").addClass('disabled');
		$("#downloadGame").data('action','');
	};
	window.onDownloadProgress = function(evt){
		if(evt.packageName != gamePackage)
			return;
		$(".download_action").show();
    	$(".loading span").text(evt.progress + '%');
    	$(".loading span").css('width',evt.progress + '%');
    }
	window.onFinishedDownload = function(evt){
		hike.invokeAction('games.installApk', evt);
		if(evt.packageName != gamePackage)
			return;
    	$("#downloadGame").removeClass('disabled');
		$("#downloadGame").data('action','games.installApk');
    	$("#downloadGame").val('Install');
    	$(".download_action").hide();
	};

	window.onStartInstall = function(){};
	window.onFinishedInstall = function(evt){
		if(evt.packageName != gamePackage)
			return;
    	$("#downloadGame").data('action','games.play');
    	$("#downloadGame").val('Play');
    	$(".download_action").hide();
	};
	window.onDownloadError = function(evt){
    	$("#downloadGame").removeClass('disabled');
    	$("#downloadGame").data('action','games.gameinstall');
		$(".download_action").hide();
		hike.ui.showError(evt.errorMsg || 'Network Problem!');
	};
	hike.registerAction('games.gameinstall', function(dataset, ctx){
		var downloadurl = (dataset['downloadurl'].indexOf('http://') == 0 || dataset['downloadurl'].indexOf('market://') == 0)
			?dataset['downloadurl']
			:hike.domain.URL_STATIC_IMG + '/dl/' + dataset['downloadurl'];
		var gameId = dataset['gameId'];
		var packageName = dataset['packageName'];
		if(downloadurl.indexOf('market://') != 0 ){
			hike.callNative('AppManager', 'download', dataset['gameTitle'], dataset['packageName'], downloadurl);
		}else{
			hike.callNative('AppManager', 'downloadFromMarket', dataset['packageName'], downloadurl);
		}
	});
    
    hike.registerAction('games.installApk', function(dataset, ctx){
		hike.callNative('AppManager', 'install', dataset['gameTitle'], dataset['packageName'], dataset['downloadurl']);
    });

    hike.registerAction('games.quitGame', function(dataset, ctx){
    	hike.backward();
    });
	
	hike.registerAction('games.browseGames', function(dataset, ctx){
            ctx.openView('game/categories',{});
	});

	hike.registerAction('games.leaderboard', function(dataset, ctx){
        	ctx.openView('game/leaderboard',dataset);
	});

	hike.registerAction('games.getLeaderboardList', function(dataView, ctx){
        var boardurl="";
        var gameId="";
        var type=dataView["boardType"];
        if(type=="specific-leaderboard")
        {
            gameId=dataView["gameId"];
            boardurl=gameId+"/leaderboard";
        }else if(type=="specific-topplayers")
        {
            gameId=dataView["gameId"];
            boardurl=gameId+"/topplayers";
        }
        dataView.preload();
        var packageName = dataView.packageName;
        var downloadUrl = dataView.downloadurl;
        var gameId = dataView.gameId;
        var title = dataView.gameTitle;
        var platformType = dataView.platformType;
        var temp = "", action = "";
    	if(platformType == 1) {		
    		if(hike.callNative('AppManager', 'isAppInstalled', packageName) == 'true'){
    			temp = "data-challenge-id ='0' data-platform-type ='1'";
    			action = 'games.challenge';
    		}else{
    			action = 'games.pupdownload';
    		}
    	}else{
    		temp = "data-challenge-id ='0' data-platform-type ='0'";
    		action = 'games.challenge';
    	}
        ctx.get(boardurl, {
            "page":dataView["curPage"],
            "pageSize":dataView["pageSize"],
            'lastRank':dataView["lastRank"],
            'type':dataView["boardType"],
            'ts':new Date().getTime()
        }, {
            "0":function(data) {
                if (data.list != null && data.list.length > 0) {
                    var len = data.list.length;
                    var boardStr = "";
                    var lastRank = 0;
                    var isInstall;
                    for (var i = 0; i < len; i++) {
                        var board = data.list[i];                                             
                        boardStr +="<li data-widget='Button'><div class='wrapper' data-action='profile.friends' data-user-id='"+board.userId+"'>";
                        if(data.userId==board.userId)
                        {
                        	board.nickName=" <span class='hint'>(You are here)</span>";
                        }
                         boardStr += ("<div class='rank'>" + board.rank+ ".</div>"+
                            "<img src='"+board.headUrl+"' />"+
                            "<div class='info'><div class='name'>" + board.nickName + "</div><div class='mutual'>" + board.score + " Points</div></div>");
                        if(data.userId != board.userId && data.userId != 0 && data.score < board.score)
                        {
                           boardStr+="<div class='btninvite'><input class='bluebtn' type='button' "+temp+" data-game-id='" +gameId+"'"+
                               "data-action='"+action+"' data-package-name='"+packageName+"'  data-challenge-type='1' data-request-id='"+
                               board.userId+"' data-request-score='"+board.score+"' value='Challenge' /></div>";
                        }
                        boardStr+="</div></li>";
                        lastRank = board.rank;
                    }
                    dataView.append(boardStr,data.hasNextPage);
                    dataView["lastRank"] = lastRank;
                }
                else
                {
                    dataView.setHint("No record yet.");
                }
            },
            "103":function() {
                alert("User does not exist");
            }
        }, true);
	});
	
	hike.registerAction('games.gamelist', function(dataset, ctx){
        	ctx.openView('game/gamelist',dataset);
	});
	
	//*** The data page render encapsulated into a separate method 
	var tempGameList = function(lists){
    	var strs = "";
    		$.each(lists,function(entryIndex,entry){
				var gameBasic = entry.gameBasic;
				var playedTime = entry.playedTime;
				var topPlayers = entry.topPlayers;
				var flag = (gameBasic.platformType===1) ? '<span class="app">App</span>' : '';
				strs += '<li data-action="games.openDetail" data-game-id="' + gameBasic.appId + '"><div class="avata">' + flag + '<img src="' + hike.domain.URL_STATIC_IMG + "/" + gameBasic.appIconCover.touchIcon + '" /></div>'
					+ '<h1>' + gameBasic.title + '</h1><span>' + playedTime + ' times played</span><div class="legend"><ul class="image_bar">';
				var strs2 = "";
				$.each(topPlayers,function(entryIndex,entry){
					strs2 += '<li data-action="profile.friends" data-user-id="' + entry.userId + '"><img src="' + entry.headUrl + '" /></li>';
				});
				strs += strs2;
				strs += '</ul></div></li>';
            });
		hike.log.info('games.all-game boardStr = '+ strs);
		return strs;
	};
	//*** The data page render encapsulated into a separate method 
	var recentlyGameList = function(data){
		var recentlyPlayList = data.recentlyPlayed;
        var recentlyPlayLen = recentlyPlayList.length;
        var boardStr = "";
        for (var i = 0; i < recentlyPlayLen; i++) {
       	 var entry = recentlyPlayList[i];
       	 if(entry['platformType']==1)
				{
       		 var action = "games.tryToPlay";
				 boardStr += "<li data-widget=\"Button\" data-game-title=\""+entry['title']+"\" data-downloadurl=\""+entry['downloadUrl']+"\" data-package-name=\""+entry['packageName']+"\" data-game-type=\""+entry['platformType']+"\" data-action=\""+action+"\" data-game-id=\""+entry['appId']+"\">"
				 +"<span class='app'>App</span>"
				 + "<img src='" + hike.domain.URL_STATIC_IMG + "/" + entry['appIconCover'].touchIcon + "'/>"
                + "<span class='name'>" + entry['title'] + "</span>"
                + (entry['score']!=-1?("<span class='pts'>"+ entry['score']+ " points</span>"):"")
                + "</li>";
				}else{
				boardStr += "<li data-widget=\"Button\" data-action=\"games.htmlplay\" data-game-id=\""+entry['appId']+"\">"
				 + "<img src='" + hike.domain.URL_STATIC_IMG + "/" + entry['appIconCover'].touchIcon + "'/>"
                + "<span class='name'>" + entry['title'] + "</span>"
                + (entry['score']!=-1?("<span class='pts'>"+ entry['score']+ " points</span>"):"")
                + "</li>";
                }
        }
		hike.log.info('games.recentlyGamelist boardStr = '+ boardStr);
		return boardStr;
	};
//-----------------------------------------------------------
	
	hike.registerAction('games.openGamelist', function(dataView, ctx){
		var url, cacheDate, gameListCache, gameListKey, str;
		var gameType=dataView["gameType"];
		var userId = 0;
		if(gameType ==="recently-played"){
			gameListKey = "recentlyPlayListKey";
			gameListCache = ctx.getCache();
			cacheData = gameListCache.get(gameListKey);
			str = "";
			url = "user/played-games";
		}else if (gameType === 'new') {
			gameListKey = "newGameListKey";
			gameListCache = ctx.getCache();
			cacheData = gameListCache.get(gameListKey);
			str = "";
			url = "android/home-new";
		}else if(gameType === 'popular') {
			gameListKey = "popularGameListKey";
			gameListCache = ctx.getCache();
			cacheData = gameListCache.get(gameListKey);
			str = "";
			url = "android/home-popular";
		}else if(gameType === "all"){
        	gameListKey = "allGameListKey";
			gameListCache = ctx.getCache();
			cacheData = gameListCache.get(gameListKey);
			str = "";
			url = "android/all-game";
        }
        var page=dataView["curPage"];
        var pageSize=dataView["pageSize"];
        var addAppPic = function(appBasic) {
			return (appBasic.platformType === 1) ? '<span class="app">App</span>' : '';
		}
		dataView.preload();
		if(url == "user/played-games"){
			var recentlyPlayUrl = "recentlyPlay/0/1";
			userId = dataView["userId"];
			var userType = dataView["userType"];
			if(userId > 0){
				recentlyPlayUrl = "recentlyPlay/"+userId+"/1";
			}
			var recentlyPlaycache = ctx.getCache();
			var recentlyPlayListKey = "recentlyPlayListKey";
			var recentlyPlaycacheData = recentlyPlaycache.get(recentlyPlayListKey)|| null;
			var dataViewContent = "";
			if(userType == true && dataView["curPage"]<2 && null != recentlyPlaycacheData && 
					recentlyPlaycacheData.recentlyPlayed != null && recentlyPlaycacheData.recentlyPlayed.length > 0){
				dataViewContent = recentlyGameList(recentlyPlaycacheData);  
				dataView.reset(dataViewContent,recentlyPlaycacheData.hasNextPage);
			   }
				ctx.get(recentlyPlayUrl, {
		            "page":page,
		            "pageSize":pageSize
		        },{"0":function(data){
		        	 var gameStr = "";
					 var gameList = data.recentlyPlayed;
					 var hasNextPage = data.hasNextPage;
					 if(gameList!=null && gameList.length>0){
						 gameStr = recentlyGameList(data); 
					 }

					//**** If the data have updated the update cache
		            if(dataView['curPage'] === 1 && gameStr != dataViewContent){
						dataView.reset(gameStr,hasNextPage);
						recentlyPlaycache.persist(recentlyPlayListKey,data);
					}else if("" == gameStr){
						dataView.setHint("No game yet.") ;
					}else{
						dataView.append(gameStr,hasNextPage);	
					}
				},
				"1":function(data){
					$('.waring').html('No games !');
					$('.waring').show();
					dataView.append('', true);
				}
				}, true);
	
		}else {
		 if(dataView["curPage"]<2 && null != cacheData && cacheData.gameList.gameVOList != null && cacheData.gameList.gameVOList.length > 0){
	        	var list = cacheData.gameList.gameVOList;
	    		str = tempGameList(list);
	 			dataView.append(str,cacheData.gameList.hasNextPage);
			   }
	        ctx.get(url, {
	            "page":dataView["curPage"],
	            "pageSize":dataView["pageSize"],
	            'ts':new Date().getTime()
	        }, {
	            "0":function(data) {
					var hasNextPage = data.gameList.hasNextPage;
					var list = data.gameList.gameVOList;
	            	if(list!=null && list.length>0){
	            		var gameStr = "";
	            		gameStr = tempGameList(list);
	         			if(dataView['curPage'] === 1 && gameStr != str){
	         				dataView.reset(gameStr,hasNextPage);
	         				gameListCache.persist(gameListKey,data);
			             }else if("" == gameStr){
						    dataView.setHint("You have no game yet.") ;
						}else{
							dataView.append(gameStr,hasNextPage);	
							ctx.updateView();
						}
					} else {
	                	dataView.append('',true);
					}
	            }
	        }, true);
		}
	  });
    
    hike.registerAction('game.gameDetail', function(dataset, ctx){
    	ctx.openView('game/game-detail',{});
    });
    
    hike.registerAction('games.openDetailForQuit', function(dataset, ctx){
    	ctx.history.pushState('home.quit', {});
    	ctx.invokeAction('games.openDetail', dataset);
    });
    
    hike.registerAction('games.playFromNewsfeed', function(dataset, ctx){
    	var gameId=dataset['gameId'];
    	var gameUrl="game/"+gameId;
    	ctx.get(gameUrl,null,{
            "0":function(data){
               var gameEntity = data.game;
               var action = "";
               var parmas = {};
               if(gameEntity.platformType==1){
            	  var action = "games.tryToPlay";
				  parmas = {	
						    "gameTitle" : gameEntity.title,
						    "downloadurl" : gameEntity.downloadUrl,
						    "packageName" : gameEntity.packageName,
						    "gameType" : gameEntity.platformType,
						    "gameId" : gameEntity.appId
				  			};
				}else{
				  action = 'games.htmlplay';
				  parmas = { "gameId" : gameEntity.appId };
                }
	    		ctx.invokeAction(action, parmas );
            },"101":function(){
                
            }
        });
    });
})();
