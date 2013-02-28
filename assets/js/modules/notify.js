(function(){
	
	//start timeout to load notifications automatically.
	var loadNotify = function(){
		var timestamp = Date.parse(new Date()); 
		var notifyUrl = hike.domain.URL_SNS_TOUCH + "/android/notify/getAllNotify?timestamp="+timestamp;
		$.ajax({
			method : 'get',
			url : notifyUrl,
			data :  {
	            "page":1,
	            "pageSize":10
	        },
			success : function(data){
					var notify = hike.util.decodeJson(data);
					var ctx = hike.getContext();
					var cache = ctx.getCache();
					cache.persist("allFriendNotifyList",notify.allFriendNotifyList);
					cache.persist("allGameNotifyList",notify.allGameNotifyList);
	            	cache.persist("payNotificationList",notify.payNotificationList);
	            	cache.persist("allNotifyCount",notify.allNotifyCount);
	            	cache.persist("friendsNotifyIds", notify.allFriendNotifyList.notifyIds);
	            	cache.persist("challengeNotifyIds", notify.allGameNotifyList.notifyIds);
	            	cache.persist("time", notify.time);
	            	hike.log.info('time:'+notify.time);
	            	hike.callNative('NativeUI', 'setNotifyCount', notify.allNotifyCount.friendNotifyUnreadCount, notify.allNotifyCount.gameNotifyUnreadCount, notify.allNotifyCount.messageNotifyUnreadCount,notify.allNotifyCount.total);
	            	//reload notification data
	            	var viewName = ctx.getViewName();
	            	if(viewName == "notify/list-notify"){
	            		ctx.reload();
	            	}
	            	window.setTimeout(loadNotify, 3 * 60 * 1000);
			},
			error : function(){
				hike.log.error('Reload notification error');
				window.setTimeout(loadNotify, 3 * 60 * 1000);
			}
		});
	};
	
	loadNotify();
	
	hike.registerAction('notify.friend', function(dataView, ctx){
		var cache = ctx.getCache();
		var cacheData = cache.get("allNotifyCount");
		var url = "android/notify/home";
		if(cacheData){
			hike.callNative('NativeUI', 'setNotifyCount',cacheData.friendNotifyUnreadCount,cacheData.gameNotifyUnreadCount,cacheData.messageNotifyUnreadCount,cacheData.total);
			ctx.openView('notify/list-notify', cacheData);
		}else{
			ctx.get(url,null,{
	            "0":function(data){
	            	hike.callNative('NativeUI', 'setNotifyCount',data.friendNotifyUnreadCount,data.gameNotifyUnreadCount,data.messageNotifyUnreadCount,data.total);
	            	ctx.openView('notify/list-notify', data);
	            }
	        }, true);
	  }
	});

	hike.registerAction('notify.allFriendNotify', function(dataView, ctx){
		var cache = ctx.getCache();
		var cacheData = cache.get("allFriendNotifyList");
		var url = "android/notify/friend";
		if(cacheData!=null&&dataView["curPage"]==1){  
	          if (cacheData.list != null && cacheData.list.length > 0) {
                  var len = cacheData.list.length;
                  var userMap = cacheData.userMap;
                  var RequsetMutualFriendMap = cacheData.RequsetMutualFriendMap;                        
                  var RequsetMutualGameMap = cacheData.RequsetMutualGameMap;
                  var sendContentMap = cacheData.sendContentMap;
                  var acceptanceMutualFriendMap = cacheData.acceptanceMutualFriendMap;
                  var acceptanceUserMap = cacheData.acceptanceUserMap;
                  var str = "";
                  
                  for (var i = 0; i < len; i++) {
                      var item = cacheData.list[i];
                      var headUrl = "";
  					  var nickName = "";
  					  var mutualShow = "";
  					  var displayAccept = "";
  					  var displayLater = "";
  					  var acceptanceUser = acceptanceUserMap[item.id];
                      if(item.type == 100){
	                        var applicantUser = userMap[item.appId];						
							var RequestMutualFriendNum = RequsetMutualFriendMap[item.appId];
							var RequestMutualGameNum = RequsetMutualGameMap[item.appId];						
							var sendContent=sendContentMap[item.appId];
							if(applicantUser != null){
								headUrl = applicantUser.headUrl;
	        					nickName = applicantUser.nickName;
							}

							if(RequestMutualFriendNum > 1){
								mutualShow = RequestMutualFriendNum + " common friends";
							}
							if(RequestMutualFriendNum == 1){
								mutualShow = RequestMutualFriendNum + " common friend";
							}
							if(RequestMutualFriendNum == 0 && RequestMutualGameNum > 1){
								mutualShow = RequestMutualGameNum + " common games";
							}
							if(RequestMutualFriendNum == 0 && RequestMutualGameNum == 1){
								mutualShow = RequestMutualGameNum + " common game";
							}
                      }else{
                          var acceptanceMutualFriendNum = acceptanceMutualFriendMap[item.id];
	      			      if(acceptanceUser != null){
	      						headUrl = acceptanceUser.headUrl;
	          					nickName = acceptanceUser.nickName;
	      					}
                      }
                      
                      if(item.type == 100){
                    	  if(item.status == 0){
                    		  displayAccept ="<div class='play' data-widget='Button' name='accept"+item.appId+"' data-action='notify.friend.requests.accept' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn bluebtn'>Accept</span></div>";
                    		  displayLater = "<div class='del' data-widget='Button' name='later"+item.appId+"' data-action='notify.friend.requests.ignore' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
                    	  }else if(item.status==1) {
                    		  displayAccept ="<div class='play' data-widget='Button' name='accept"+item.appId+"' data-action='notify.friend.requests.accept' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn bluebtn'>Accept</span></div>";
                    		  displayLater = "<div class='del' data-widget='Button' name='later"+item.appId+"' data-action='notify.friend.requests.ignore' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
                    	  }else if(item.status == 2){
                    		  displayAccept = "<div class='ignored' name='accept"+item.appId+"'><span class='btn disablebtn'>Accepted</span></div>";
                    	  }else if(item.status == 3){
                    		  displayAccept = "<div class='ignored' name='accept"+item.appId+"'><span class='btn disablebtn'>Ignored</span></div>";
                    	  }
                      }
					
						var li = "";
						if(item.type == 100){
							if(item.status == 0){
								li = "<li class='unread' name='unread"+item.appId+"' data-widget='Button' data-action='notify.friend.detail' data-user-id='"+item.appId+"'>";
							}else{
								li = "<li data-widget='Button' data-action='notify.friend.detail' data-user-id='"+item.appId+"'>";
							}
							str += li
								+ "<img class='avata' src='"+headUrl+"'/>"
								+ "<h1>"+nickName+"</h1>"
								+ "<h2>"+ mutualShow+"</h2>"
								+ "<h3>"+ sendContent+"</h3>"
	                            + displayAccept
	                            + displayLater
	                            + "</li>";
						}else{
							hike.log.info('acceptanceStatus'+item.status);
							if(item.status == 0){
								li = "<li class='unread' data-widget='Button' data-action='notify.acceptance.read' data-user-id='"+acceptanceUser.id+"'>";
							}
							else{
								li = "<li data-widget='Button' data-action='notify.acceptance.read' data-user-id='"+acceptanceUser.id+"'>";
							}
							
							str += li
							+ "<img class='avata' alt='"+nickName+"' src='"+headUrl+"' />"
							+ "<h1>"+nickName+"</h1>"
							+ "<h2>has accepted your request</h2>"
	                    	+ "</li>";
						}
                  }
                  dataView.append(str,cacheData.hasNextPage);
                  ctx.updateView();
              } else{
              	dataView.append('',true);
              	dataView.setHint("No record yet.");
              }
	          ctx.forward('notify.updateAllFriendNotifyCache');
              ctx.forward('notify.updateCountCache');
		}   
		else{
        dataView.preload();
        ctx.get(url, {
            "page":dataView["curPage"],
            "pageSize":dataView["pageSize"],
            'ts':new Date().getTime()
        }, {
            "0":function(data) {
            	hike.log.info('friend request normal flow');
                if (data.list != null && data.list.length > 0) {
                    var len = data.list.length;
                    var userMap = data.userMap;
                    var RequsetMutualFriendMap = data.RequsetMutualFriendMap;                        
                    var RequsetMutualGameMap = data.RequsetMutualGameMap;
                    var sendContentMap = data.sendContentMap;
                    var acceptanceMutualFriendMap = data.acceptanceMutualFriendMap;
                    var acceptanceUserMap = data.acceptanceUserMap;
                    var str = "";
                    
                    for (var i = 0; i < len; i++) {
	                    var item = data.list[i];
	                    var headUrl = "";
						var nickName = "";
						var mutualShow = "";
						var displayAccept = "";
						var displayLater = "";
						var acceptanceUser = acceptanceUserMap[item.id];
                        if(item.type == 100){
  	                        var applicantUser = userMap[item.appId];						
  							var RequestMutualFriendNum = RequsetMutualFriendMap[item.appId];
  							var RequestMutualGameNum = RequsetMutualGameMap[item.appId];						
  							var sendContent=sendContentMap[item.appId];
  							if(applicantUser != null){
  								headUrl = applicantUser.headUrl;
  	        					nickName = applicantUser.nickName;
  							}

  							if(RequestMutualFriendNum > 1){
  								mutualShow = RequestMutualFriendNum + " common friends";
  							}
  							if(RequestMutualFriendNum == 1){
  								mutualShow = RequestMutualFriendNum + " common friend";
  							}
  							if(RequestMutualFriendNum == 0 && RequestMutualGameNum > 1){
  								mutualShow = RequestMutualGameNum + " common games";
  							}
  							if(RequestMutualFriendNum == 0 && RequestMutualGameNum == 1){
  								mutualShow = RequestMutualGameNum + " common game";
  							}
                        }else{
                            var acceptanceMutualFriendNum = acceptanceMutualFriendMap[item.id];
  	      			      	if(acceptanceUser != null){
  	      						headUrl = acceptanceUser.headUrl;
  	          					nickName = acceptanceUser.nickName;
  	      					}
                        }
                        
                    	if(item.status == 0&&item.type == 100){
							displayAccept ="<div class='play' data-widget='Button' name='accept"+item.appId+"' data-action='notify.friend.requests.accept' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn bluebtn'>Accept</span></div>";
							displayLater = "<div class='del' data-widget='Button' name='later"+item.appId+"' data-action='notify.friend.requests.ignore' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
                    	}else if(item.status==1&&item.type == 100){
							displayAccept ="<div class='play' data-widget='Button' name='accept"+item.appId+"' data-action='notify.friend.requests.accept' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn bluebtn'>Accept</span></div>";
							displayLater = "<div class='del' data-widget='Button' name='later"+item.appId+"' data-action='notify.friend.requests.ignore' data-applicant='"+item.appId+"' data-accepter='"+item.receiverId+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
						}else if(item.status == 2&&item.type == 100){
							displayAccept = "<div class='ignored' name='accept"+item.appId+"'><span class='btn disablebtn'>Accepted</span></div>";
						}else if(item.status == 3&&item.type == 100){
							displayAccept = "<div class='ignored' name='accept"+item.appId+"'><span class='btn disablebtn'>Ignored</span></div>";
						}
  					
  						var li = "";
  						if(item.type == 100){
  							if(item.status == 0){
  								li = "<li class='unread' name='unread"+item.appId+"' data-widget='Button' data-action='notify.friend.detail' data-user-id='"+item.appId+"'>";
  							}
  							else{
  								li = "<li data-widget='Button' data-action='notify.friend.detail' data-user-id='"+item.appId+"'>";
  							}
  							str += li
  								+ "<img class='avata' src='"+headUrl+"'/>"
  								+ "<h1>"+nickName+"</h1>"
  								+ "<h2>"+ mutualShow+"</h2>"
  								+ "<h3>"+ sendContent+"</h3>"
  	                            + displayAccept
  	                            + displayLater
  	                            + "</li>";
  						}else{
  							hike.log.info('acceptanceStatus'+item.status);
  							if(item.status == 0){
  								li = "<li class='unread' data-widget='Button' data-action='notify.acceptance.read' data-user-id='"+acceptanceUser.id+"'>";
  							}
  							else{
  								li = "<li data-widget='Button' data-action='notify.acceptance.read' data-user-id='"+acceptanceUser.id+"'>";
  							}
  							
  							str += li
  							+ "<img class='avata' alt='"+nickName+"' src='"+headUrl+"' />"
  							+ "<h1>"+nickName+"</h1>"
  							+ "<h2>has accepted your request</h2>"
  	                    	+ "</li>";
  						}
  						
                    }
                    dataView.append(str,data.hasNextPage);
                    ctx.updateView();
                    ctx.forward('notify.updateAllFriendNotifyCache');
                    ctx.forward('notify.updateCountCache');
                } else{
                	dataView.append('',true);
                	dataView.setHint("No record yet.");
                }
            },
            "103":function() {
                $('.error').html('User does not exist!');
				$('.error').show();
            }
        }, true);
	  }
    });

	hike.registerAction('notify.friend.detail', function(dataset, ctx){
		$(ctx.element).data('action','');
		var applicant = dataset["userId"];
		var url = "notify/friend/detail?applicant="+applicant;
		$('li[name="unread'+applicant+'"]').removeClass('unread');
		ctx.forward("profile.friends",{"userId":applicant});
		ctx.get(url,null,{
            "0":function(data){
            	hike.log.info('friend request profile cache update begin');
        		ctx.forward('notify.updateAllFriendNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
        		hike.log.info('friend request profile cache update end');
        		hike.log.info('open other profile page');
            },
            "701":function(data){
            	ctx.forward("profile.friends",{"userId":applicant});
            }
        });
	});
	
	hike.registerAction('notify.friend.requests.ignore', function(dataView, ctx){
		var applicant=dataView["applicant"];
		var accepter=dataView["accepter"];
		var cache = ctx.getCache();
		var cacheData = cache.get("allNotifyCount");
		var url = "notify/friend/ignore?applicant="+applicant+"&accepter="+accepter;
		ctx.get(url,null,{
            "0":function(data){
        		var later=$('div[name="later'+applicant+'"]');
        		var unread=$('li[name="unread'+applicant+'"]');
        		$('div[name="accept'+applicant+'"]').hide();
        		unread.removeClass('unread');
        		later.removeAttr('data-action');
        		later.removeClass('del');
        		later.addClass('ignored');
        		later.html("<span class='btn disablebtn'>Ignored</span>");
        		if(data.friendRequest.status == 0){
        			hike.log.info('friendRequestStatus='+data.friendRequest.status);
        			var friendRequestCount = parseInt(cacheData.friendNotifyUnreadCount)-parseInt('1');
        			hike.callNative('NativeUI', 'setNotifyCount',friendRequestCount,cacheData.gameNotifyUnreadCount,cacheData.messageNotifyUnreadCount,cacheData.total);
        			hike.log.info('friendRequestCount minus one'+friendRequestCount);
        		}
        		ctx.forward('notify.updateAllFriendNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
            },
            "602":function(data){
            	$('.error').html('Already no relation!');
				$('.error').show();
            },
            "1":function(data){
            	$('.error').html('Ignore fail!');
				$('.error').show();
            }
        }, true);
	});
	hike.registerAction('notify.friend.requests.accept', function(dataView, ctx){
		var applicant=dataView["applicant"];
		var accepter=dataView["accepter"];
		var cache = ctx.getCache();
		var cacheData = cache.get("allNotifyCount");
		var url = "notify/friend/accept?applicant="+applicant+"&accepter="+accepter;
		ctx.get(url,null,{
            "0":function(data){
        		var accept = $('div[name="accept'+applicant+'"]');
        		var unread=$('li[name="unread'+applicant+'"]');
        		unread.removeClass('unread');
        		accept.removeAttr('data-action');
        		accept.removeClass('play');
        		accept.addClass('ignored');
        		accept.html("<span class='btn disablebtn'>Accepted</span>");
        		$('div[name="later'+applicant+'"]').hide();
        		if(data.friendRequest.status == 0){
        			hike.log.info('friendRequestStatus='+data.friendRequest.status);
        			var friendRequestCount = parseInt(cacheData.friendNotifyUnreadCount)-parseInt('1');
        			hike.callNative('NativeUI', 'setNotifyCount',friendRequestCount,cacheData.gameNotifyUnreadCount,cacheData.messageNotifyUnreadCount,cacheData.total);
        			hike.log.info('friendRequestCount minus one'+friendRequestCount);
        		}
        		ctx.forward('notify.updateAllFriendNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
        		ctx.forward('notify.clearNewsfeedCache');
            },
            "601":function(data){
            	var friendRequestCount = data.friendRequestCount;
        		var tabGroup = ctx.getWidget('friendNotifiy');
        		tabGroup.setTitle(0, 'Requests(<em>'+friendRequestCount+'</em>)');
            	$('.error').html("Already friend!");
				$('.error').show();
            },
            "701":function(data){
            	var friendRequestCount = data.friendRequestCount;
        		var tabGroup = ctx.getWidget('friendNotifiy');
        		tabGroup.setTitle(0, 'Requests(<em>'+friendRequestCount+'</em>)');
            	$('.error').html('Friend request not exist!');
				$('.error').show();
            }
        });
	});
	
	checkTextarea = function(obj,num){
		if (obj.value.length > num) {
			obj.value=obj.value.substring(0,num);
		}
	}
	
	
	hike.registerAction('notify.friend.add', function(dataset, ctx) {
		var userId = dataset["userId"];
		var MyName = dataset["myName"];
		var userName = dataset["nickName"];
		userName = userName.replace("<b>","").replace("</b>","");

		var config = {
				title : 'Friend add',
				buttons:[{text:'Send',fn:'notify.friend.addsend'},{text:'Cancel'}],
				closable : true,
	  			content:'<textarea class="invitecontent" onkeypress="checkTextarea(this,50)" onkeydown="checkTextarea(this,50)" onkeyup="checkTextarea(this,50)" >Hello!</textarea>'
			};
		hike.ui.showDialog(config);

		window.finishUpload = function(){
			$('.error').html('Your request has been send!');
			
		}
		
		hike.registerAction('notify.friend.addsend', function(dataset, ctx){
			var url = "friend/" + userId + "/add";
			var contentText = $(".invitecontent").val(); 
			hike.ui.closeDialog();
			ctx.get(url, {
	            content:contentText
	        }, {
				"0": function(data) {
					$('input[data-user-id="' + userId + '"]').hide();
					$('input[data-user-id="span' + userId + '"]').show();
					
					//update Cache
	                 ctx.get("notify/friend/recommend", {
	     	            "page":dataView["curPage"],
	     	            "pageSize":dataView["pageSize"],
	     	            'ts':new Date().getTime()
	     	        }, {
	     	            "0":function(data) {
	     	                if (data.list != null && data.list.length > 0) {
	     	                	hike.log.info("go server to update recommend cache ");
	     	                    cache.persist("recommendCache",data);
	     	                }
	     	            },
	     	            "103":function() {
	     	            }
	     	        }, true);
				},
			    "1001": function(data) {
			    	$('.error').html("Invite user is donot exist.");
				},
				"1006": function(data) {
					$('.error').html("Can not invite yourself.");
				},
				"1007": function(data) {
					$('.error').html("Invite request has been sent.");
				}
			})
		});
	});

	hike.registerAction('notify.allGameNotify', function(dataView, ctx){
        var url = "android/notify/game";
        var cache = ctx.getCache();
        var cacheData = cache.get("allGameNotifyList");
        var timeUtil = new hike.android.TimeUtil();
        if(cacheData!=null && dataView["curPage"]==1){
        	if (cacheData.list != null && cacheData.list.length > 0) {
                var len = cacheData.list.length;
                var requestUserMap = cacheData.requestUserMap;
                var requestGameMap = cacheData.requestGameMap;
                var requestSystemTimeMap = cacheData.requestSystemTimeMap;
                var inviteUserMap = cacheData.inviteUserMap;
                var inviteGameMap = cacheData.inviteGameMap;
                var inviteSystemTimeMap = cacheData.inviteSystemTimeMap;
                var inviterIdListMap =cacheData.inviterIdListMap;
                var pIdsListMap =cacheData.pIdsListMap;
                var inviteCountMap =cacheData.inviteCountMap;
                var idListMap = cacheData.idListMap;
                var resultSystemTimeMap = cacheData.resultSystemTimeMap;
                var resultGameMap = cacheData.resultGameMap;
                var resultUserMap = cacheData.resultUserMap;
                var lenMap = cacheData.lenMap;
                var str = "";
                for (var i = 0; i < len; i++) {
                    var item = cacheData.list[i];

                    var headUrl = "";
                    var nickName = "";
                    var gameTitle = "";
                    var time = "";
                    var nameDetail = "";
                    var idString = "";
                    var inviterString = "";
                    var gameUrl = "";
                    var platformType="";
                    var packageName ="";
                    var gameResult = "";
                    var type = "";
                    var invite = "";
                    
                    if(item.type == 17){
                        if(inviteGameMap[item.appId]!= null){
                        	gameTitle = inviteGameMap[item.appId].title;
                        	gameUrl =  inviteGameMap[item.appId].appIconCover.touchIcon;
                        	platformType = inviteGameMap[item.appId].platformType;
                        	packageName = inviteGameMap[item.appId].packageName;
                        }
                        nameDetail = inviteUserMap[item.id];
                        time = timeUtil.serverTime(inviteSystemTimeMap[item.id]);
                        idString = idListMap[item.id];
                        inviterString = inviterIdListMap[item.id];
                        var leng = lenMap[item.id];
                        if(leng == 1) {
                        	invite = " invites";
                        }
                        else{
                        	invite = " invite";
                        }
                    }else if(item.type == 24){
                         time = timeUtil.serverTime(resultSystemTimeMap[item.id]);
                         gameTitle = resultGameMap[item.id].title;
                         if (item.senderScore < item.receiverScore){
                         	gameResult = "<em>Win</em>";
                         }
                         else if(item.senderScore == item.receiverScore){
                         	gameResult = "<em>Tie</em>";
                         }
                         else if(item.senderScore > item.receiverScore){
                         	gameResult = "<em>Lose</em>";
                         }
                         
                         if(item.rltType == 0){
                         	type = "challenge";
                         }
                         else if(item.rltType == 1){
                         	type = "competition";
                         }
                         var challengerUser = resultUserMap[item.appId];
                         if(challengerUser != null){
                         	headUrl = challengerUser.headUrl;
                         	nickName = challengerUser.nickName;
                         }
                    }else{
                        var senderUser = requestUserMap[item.appId];
                        time = timeUtil.serverTime(requestSystemTimeMap[item.id]);
                        if(senderUser != null){
                        	headUrl = senderUser.headUrl;
                        	nickName = senderUser.nickName;
                        }
                        var gameBasic = requestGameMap[item.gameId];
                        if(gameBasic != null){
                        	gameTitle = gameBasic.title;
                        }
                    }
                    
                    var displayPlay = "";
					var displayIgnore = "";
					if(item.type == 17){
						if(item.status == 0){
							displayPlay = "<div class='play' name='play"+item.id+"' data-action='notify.game.invite.play' data-package-name='"+packageName+"' data-notify-id= '"+item.id+"' data-app-id = '"+item.appId+"' data-id-list='"+idString+"' data-inviter-list='"+inviterString+"' data-platform-type='"+platformType+"'><span class='btn bluebtn'>Play</span></div>";
							displayIgnore = "<div class='del' name='ignore"+item.id+"' data-action='notify.game.invite.ignore' data-notify-id='"+item.id+"' data-id-list='"+idString+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
						}
						else if(item.status == 1){
							displayPlay = "<div class='play' name='play"+item.id+"' data-action='notify.game.invite.play' data-package-name='"+packageName+"' data-notify-id= '"+item.id+"' data-app-id = '"+item.appId+"' data-id-list='"+idString+"' data-inviter-list='"+inviterString+"' data-platform-type='"+platformType+"'><span class='btn bluebtn'>Play</span></div>";
							displayIgnore = "<div class='del' name='ignore"+item.id+"' data-action='notify.game.invite.ignore' data-notify-id='"+item.id+"' data-id-list='"+idString+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
						}
						else if(item.status == 3){
							displayPlay = "<div class='ignored'><span class='btn disablebtn'>Accepted</span></div>";
						}
						else if(item.status == 4){
							displayPlay = "<div class='ignored'><span class='btn disablebtn'>Ignored</span></div>";
						}
					}else if(item.type == 101){
						if(item.status == 0){
							displayPlay = "<div class='play' name='play"+
							item.id+"' data-action='notify.game.onChallenge' data-challenge-id = '"+
							item.id+"' data-challenge-type='2' data-request-id='"+
							item.appId+"' data-request-score='"+item.senderScore+"' data-game-id='"+
							item.gameId+"' data-package-name='"+gameBasic.packageName+"' data-platform-type='"+gameBasic.platformType+"'><span class='btn bluebtn'>Play</span></div>";
							displayIgnore = "<div class='del' name='ignore"+
							item.id+"' data-action='challenge.request.ignore' data-challenge-id='"+item.id+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
						}
						else if(item.status == 1){
							displayPlay = "<div class='play' name='play"+
							item.id+"' data-action='notify.game.onChallenge' data-challenge-id = '"+
							item.id+"' data-challenge-type='2' data-request-id='"+
							item.appId+"' data-request-score='"+item.senderScore+"' data-game-id='"+
							item.gameId+"' data-package-name='"+gameBasic.packageName+"' data-platform-type='"+gameBasic.platformType+"'><span class='btn bluebtn'>Play</span></div>";
							displayIgnore = "<div class='del' name='ignore"+
							item.id+"' data-action='challenge.request.ignore' data-challenge-id='"+item.id+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
						}
						else if(item.status == 2 || item.status == 4){
							displayPlay = "<div class='ignored'><span class='btn disablebtn'>Accepted</span></div>";
						}
						else if(item.status == 3){
							displayPlay = "<div class='ignored'><span class='btn disablebtn'>Ignored</span></div>";
						}
					}
					
					var li = "";
					if(item.type == 17)	{
						  var pids = pIdsListMap[item.id];
						  var inviteCount = inviteCountMap[item.id];
						  var nOthersAction = "";
						  if(item.status == 0){ // unread
							  nOthersAction = " data-action=\"notify.game.invite.opennOthersDetail\" data-app-id=\""+item.appId+"\" " +
							 			   "data-package-name=\""+packageName+"\" data-platform-type=\""+platformType+"\" data-notify-id=\""+item.id+"\" " +
							 			   "data-inviter-list=\""+inviterString+"\" data-id-list=\""+idString+"\" data-game-id=\""+item.appId+"\" " +
							 			   "data-game-name=\""+gameTitle+"\" data-pids=\""+pids+"\"";
							 
							 li = "<li class='unread' name='unread"+item.id+"'"+ nOthersAction +" >";
						  } else{ // read
							  nOthersAction = " data-action=\"home.n_others_detail\" data-game-id=\""+item.appId+"\" data-game-name=\""+gameTitle+"\" " +
								 			"data-pids=\""+pids+"\" ";
							 li = "<li " + nOthersAction + ">";
						  }
						  
						  str += li
						  + "<img data-action='notify.game.invite.openDetail' data-notify-id='"+item.id+"' data-id-list='"+idString+"' data-app-id ='"+item.appId+"' src='"+ hike.domain.URL_STATIC_IMG + "/" + gameUrl + "' alt = '' class ='avata game' />"
		                	+ "<h1><em>"
		                	+ nameDetail
		                	+ "</em>"
		                	+ invite
		                	+ " you to play "
		                	+ "<em>'"+gameTitle+"'</em>"
		                	+ "</h1>" 
		                	+ "<h2>"+time+"</h2>"
		                	+ displayPlay
		                	+ displayIgnore
		                	+"</li>";
					}else if(item.type == 24){
						  if(item.delFlag == 0){
							  li = "<li class='unread' name='unread"+item.id+"' data-widget='Button' data-action='notify.result.read' data-result-id='"+item.id+"' data-user-id='"+item.appId+"'>";
						  }
						  else{
							  li = "<li data-widget='Button' data-action='notify.game.profile.friends' data-user-id='"+item.appId+"' data-challenge-id = '"+item.id+"'>";
						  }
						  str += li
	                     	+ "<img class='avata' src='"+headUrl+"' alt='"+nickName+"' />"
	                     	+ "<h1>"+gameResult+" in "+nickName + "'s challenge<br/>"
	                     	+ gameTitle+" " + item.receiverScore + "-" + item.senderScore + "</h1>"
	                     	+"<h2>"+time+"</h2>"
	                     	+ "</li>";
					}else{
						if(item.status == 0){
							li= "<li class='unread' name='unread"+item.id+"' data-widget='Button' data-action='notify.game.profile.friends' data-user-id='"+item.appId+"' data-challenge-id = '"+item.id+"'>";
						}
						else{
							li= "<li data-widget='Button' data-action='notify.game.profile.friends' data-user-id='"+item.appId+"' data-challenge-id = '"+item.id+"'>";
						}
                        str += li
                        	+ "<img src='"+headUrl+"' class='avata' alt = ''/>"
                        	+ "<h1><em>"+nickName+"</em> challenge in "+gameTitle+"</h1>"
                         	+ "<h2>"+time+"</h2>"
                        	+ displayPlay
                        	+ displayIgnore
                        	+ "</li>";
					}
                }
                dataView.append(str,cacheData.hasNextPage);
                ctx.updateView();
            } else{
            	dataView.append('',true);
            	dataView.setHint("No record yet.");
            }
        	ctx.forward('notify.updateAllGameNotifyCache');
            ctx.forward('notify.updateCountCache');
        }
        else{
        dataView.preload();
        ctx.get(url, {
            "page":dataView["curPage"],
            "pageSize":dataView["pageSize"],
            'ts':new Date().getTime()
        }, {
            "0":function(data) {
            	if (data.list != null && data.list.length > 0) {
                    var len = data.list.length;
                    var requestUserMap = data.requestUserMap;
                    var requestGameMap = data.requestGameMap;
                    var requestSystemTimeMap = data.requestSystemTimeMap;
                    var inviteUserMap = data.inviteUserMap;
                    var inviteGameMap = data.inviteGameMap;
                    var inviteSystemTimeMap = data.inviteSystemTimeMap;
                    var inviterIdListMap =data.inviterIdListMap;
                    var pIdsListMap =data.pIdsListMap;
                    var inviteCountMap =data.inviteCountMap;
                    var idListMap = data.idListMap;
                    var resultSystemTimeMap = data.resultSystemTimeMap;
                    var resultGameMap = data.resultGameMap;
                    var resultUserMap = data.resultUserMap;
                    var lenMap = data.lenMap;
                    var str = "";
                    for (var i = 0; i < len; i++) {
                        var item = data.list[i];

                        var headUrl = "";
                        var nickName = "";
                        var gameTitle = "";
                        var time = "";
                        var nameDetail = "";
                        var idString = "";
                        var inviterString = "";
                        var gameUrl = "";
                        var platformType="";
                        var packageName = "";
                        var gameResult = "";
                        var type = "";
                        var invite = "";
                        
                        if(item.type == 17){
                            if(inviteGameMap[item.appId]!= null){
                            	gameTitle = inviteGameMap[item.appId].title;
                            	gameUrl = inviteGameMap[item.appId].appIconCover.touchIcon;
                            	platformType = inviteGameMap[item.appId].platformType;
                            	packageName = inviteGameMap[item.appId].packageName;
                            }
                            nameDetail = inviteUserMap[item.id];
                            time = timeUtil.serverTime(inviteSystemTimeMap[item.id]);
                            idString = idListMap[item.id];
                            inviterString = inviterIdListMap[item.id];
                            var leng = lenMap[item.id];
                            if(leng == 1) {
                            	invite = " invites";
                            }
                            else{
                            	invite = " invite";
                            }
                        }else if(item.type == 24){
                             time = timeUtil.serverTime(resultSystemTimeMap[item.id]);
                             gameTitle = resultGameMap[item.id].title;
                             if (item.senderScore < item.receiverScore){
                             	gameResult = "<em>Win</em>";
                             }
                             else if(item.senderScore == item.receiverScore){
                             	gameResult = "<em>Tie</em>";
                             }
                             else if(item.senderScore > item.receiverScore){
                             	gameResult = "<em>Lose</em>";
                             }
                             
                             if(item.rltType == 0){
                             	type = "challenge";
                             }
                             else if(item.rltType == 1){
                             	type = "competition";
                             }
                             var challengerUser = resultUserMap[item.appId];
                             if(challengerUser != null){
                             	headUrl = challengerUser.headUrl;
                             	nickName = challengerUser.nickName;
                             }
                        }else{
	                        var senderUser = requestUserMap[item.appId];
	                        time = timeUtil.serverTime(requestSystemTimeMap[item.id]);
	                        if(senderUser != null){
	                        	headUrl = senderUser.headUrl;
	                        	nickName = senderUser.nickName;
	                        }
	                        var gameBasic = requestGameMap[item.gameId];
	                        if(gameBasic != null){
	                        	gameTitle = gameBasic.title;
	                        }
                        }
                        
                        var displayPlay = "";
						var displayIgnore = "";
						if(item.type == 17){
							if(item.status == 0){
								displayPlay = "<div class='play' name='play"+item.id+"' data-action='notify.game.invite.play' data-package-name='"+packageName+"' data-notify-id= '"+item.id+"' data-app-id = '"+item.appId+"' data-id-list='"+idString+"' data-inviter-list='"+inviterString+"' data-platform-type='"+platformType+"'><span class='btn bluebtn'>Play</span></div>";
								displayIgnore = "<div class='del' name='ignore"+item.id+"' data-action='notify.game.invite.ignore' data-notify-id='"+item.id+"' data-id-list='"+idString+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
							}
							else if(item.status == 1){
								displayPlay = "<div class='play' name='play"+item.id+"' data-action='notify.game.invite.play' data-package-name='"+packageName+"' data-notify-id='"+item.id+"' data-app-id = '"+item.appId+"' data-id-list='"+idString+"' data-inviter-list='"+inviterString+"' data-platform-type='"+platformType+"'><span class='btn bluebtn'>Play</span></div>";
								displayIgnore = "<div class='del' name='ignore"+item.id+"' data-action='notify.game.invite.ignore' data-notify-id='"+item.id+"' data-id-list='"+idString+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
							}
							else if(item.status == 3){
								displayPlay = "<div class='ignored'><span class='btn disablebtn'>Accepted</span></div>";
							}
							else if(item.status == 4){
								displayPlay = "<div class='ignored'><span class='btn disablebtn'>Ignored</span></div>";
							}
						}else if(item.type == 101){
							if(item.status == 0){
								displayPlay = "<div class='play' name='play"+
								item.id+"' data-action='notify.game.onChallenge' data-challenge-id = '"+
								item.id+"' data-challenge-type='2' data-request-id='"+
								item.appId+"' data-request-score='"+item.senderScore+"' data-game-id='"+
								item.gameId+"' data-package-name='"+gameBasic.packageName+"' data-platform-type='"+gameBasic.platformType+"'><span class='btn graybtn'>Play</span></div>";
								displayIgnore = "<div class='del' name='ignore"+
								item.id+"' data-action='challenge.request.ignore' data-challenge-id='"+item.id+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
							}
							else if(item.status == 1){
								displayPlay = "<div class='play' name='play"+
								item.id+"' data-action='notify.game.onChallenge' data-challenge-id = '"+
								item.id+"' data-challenge-type='2' data-request-id='"+
								item.appId+"' data-request-score='"+item.senderScore+"' data-game-id='"+
								item.gameId+"' data-package-name='"+gameBasic.packageName+"' data-platform-type='"+gameBasic.platformType+"'><span class='btn bluebtn'>Play</span></div>";
								displayIgnore = "<div class='del' name='ignore"+
								item.id+"' data-action='challenge.request.ignore' data-challenge-id='"+item.id+"'><span class='btn graybtn'><img src='file:///android_asset/img/notify-delbtn.png'/></span></div>";
							}
							else if(item.status == 2 || item.status == 4){
								displayPlay = "<div class='ignored'><span class='btn disablebtn'>Accepted</span></div>";
							}
							else if(item.status == 3){
								displayPlay = "<div class='ignored'><span class='btn disablebtn'>Ignored</span></div>";
							}
						}
						
						var li = "";
						if(item.type == 17)	{ //game invite
							  var pids = pIdsListMap[item.id];
							  var inviteCount = inviteCountMap[item.id];
							  var nOthersAction = "";
							  if(item.status == 0){ // unread
								  nOthersAction = " data-action=\"notify.game.invite.opennOthersDetail\" data-app-id=\""+item.appId+"\" " +
								 			   "data-package-name=\""+packageName+"\" data-platform-type=\""+platformType+"\" data-notify-id=\""+item.id+"\" " +
								 			   "data-inviter-list=\""+inviterString+"\" data-id-list=\""+idString+"\" data-game-id=\""+item.appId+"\" " +
								 			   "data-game-name=\""+gameTitle+"\" data-pids=\""+pids+"\"";
								 
								 li = "<li class='unread' name='unread"+item.id+"'"+ nOthersAction +" >";
							  } else{ // read
								  nOthersAction = " data-action=\"home.n_others_detail\" data-game-id=\""+item.appId+"\" data-game-name=\""+gameTitle+"\" " +
									 			"data-pids=\""+pids+"\" ";
								 li = "<li " + nOthersAction + ">";
							  }
							  str += li
			                	+ "<img data-action='notify.game.invite.openDetail' data-notify-id='"+item.id+"' data-id-list='"+idString+"' data-app-id ='"+item.appId+"' src='"+ hike.domain.URL_STATIC_IMG + "/" + gameUrl + "' alt = '' class ='avata game' />"
			                	+ "<h1><em>"
			                	+ nameDetail
			                	+ "</em>"
			                	+ invite
			                	+ " you to play "
			                	+ "<em>'"+gameTitle+"'</em>"
			                	+ "</h1>" 
			                	+ "<h2>"+time+"</h2>"
			                	+ displayPlay
			                	+ displayIgnore
			                	+"</li>";
						}else if(item.type == 24){  // challenge result
							  if(item.delFlag == 0){
								  li = "<li class='unread' name='unread"+item.id+"' data-widget='Button' data-action='notify.result.read' data-result-id='"+item.id+"' data-user-id='"+item.appId+"'>";
							  }
							  else{
								  li = "<li data-widget='Button' data-action='notify.game.profile.friends' data-user-id='"+item.appId+"' data-challenge-id = '"+item.id+"'>";
							  }
							  str += li
		                     	+ "<img class='avata' src='"+headUrl+"' alt='"+nickName+"' />"
		                     	+ "<h1>"+gameResult+" in "+nickName + "'s challenge<br/>"
		                     	+ gameTitle+" " + item.receiverScore + "-" + item.senderScore + "</h1>"
		                     	+"<h2>"+time+"</h2>"
		                     	+ "</li>";
						}else{
							if(item.status == 0){
								li= "<li class='unread' name='unread"+item.id+"' data-widget='Button' data-action='notify.game.profile.friends' data-user-id='"+item.appId+"' data-challenge-id = '"+item.id+"'>";
							}
							else{
								li= "<li data-widget='Button' data-action='notify.game.profile.friends' data-user-id='"+item.appId+"' data-challenge-id = '"+item.id+"'>";
							}
	                        str += li
	                        	+ "<img src='"+headUrl+"' class='avata' alt = ''/>"
	                        	+ "<h1><em>"+nickName+"</em> challenge in "+gameTitle+"</h1>"
	                         	+ "<h2>"+time+"</h2>"
	                        	+ displayPlay
	                        	+ displayIgnore
	                        	+ "</li>";
						}
                    }
                    dataView.append(str,data.hasNextPage);
                    ctx.updateView();
                    ctx.forward('notify.updateAllGameNotifyCache');
                    ctx.forward('notify.updateCountCache');
                } else{
                	dataView.append('',true);
                	dataView.setHint("No record yet.");
                }
            },
            "103":function() {
                $('.error').html('User does not exist!');
				$('.error').show();
            }
        }, true);
        };
    });

	hike.registerAction('notify.game.invite.play', function(dataView, ctx){
		var appId = dataView["appId"];
		
		//validate game
		var valiUrl = "game/"+appId+"/status";
		ctx.get(valiUrl,null,{
		        	"0":function(){
		        		var idString = dataView["idList"];
		        		var inviterString = dataView["inviterList"];
		        		var notifyId = dataView["notifyId"];
		        		var packageName = dataView["packageName"];
		        		var platformType = dataView["platformType"];
		        		var url = "notify/game/invite/play";
		        		if(platformType == 1 && (hike.callNative('AppManager', 'isAppInstalled', packageName) != 'true')){
		        	    	var detailUrl="android/notify/invite/play";
		        	    	$('li[name="unread'+notifyId+'"]').removeClass('unread');
		        	        ctx.get(detailUrl,{
		        	        	"idString":idString
		        	        },{
		        	        	"0":function(data){
		        		            data.animation='fade';
		        		            hike.log.info('game invite play cache update begin');
		        		        	ctx.forward('notify.updateAllGameNotifyCache');
		        		            ctx.forward('notify.updateAllNotifyCountCache');
		        		            hike.log.info('game invite play cache update end');
		        		            ctx.forward("games.openDetail",{'gameId':appId});
		        		            hike.log.info('open game detail page end');
		        	          }
		        	        });
		                }else{
		                	if(platformType == 1){
		                		var playbtn = $('div[name="play'+notifyId+'"]');
		        		    	playbtn.removeAttr('data-action');
		        		    	playbtn.removeClass('play');
		        		    	playbtn.addClass('ignored');
		        		    	playbtn.html("<span class='btn disablebtn'>Accepted</span>");
		        				$('div[name="ignore'+notifyId+'"]').hide();
		        				$('li[name="unread'+notifyId+'"]').removeClass('unread');
		        				ctx.get(url,{
		        					   "idString":idString,
		        			           "inviterString":inviterString,
		        			           "appId":appId
		        				},{
		        		            "0":function(data){
		        		            	hike.log.info('game invite android play cache update begin');
		        		        		ctx.forward('notify.updateAllGameNotifyCache');
		        		        		ctx.forward('notify.updateAllNotifyCountCache');
		        		        		hike.log.info('game invite android play cache update end');
		        		        		ctx.forward('games.play',{'packageName':packageName,'gameId':appId});
		        		        		hike.log.info('open game page end');
		        		            }
		        		        });
		                		
		                	}
		                	else{
		        		    	var playbtn = $('div[name="play'+notifyId+'"]');
		        		    	playbtn.removeAttr('data-action');
		        		    	playbtn.removeClass('play');
		        		    	playbtn.addClass('ignored');
		        		    	playbtn.html("<span class='btn disablebtn'>Accepted</span>");
		        				$('div[name="ignore'+notifyId+'"]').hide();
		        				$('li[name="unread'+notifyId+'"]').removeClass('unread');
		        				ctx.get(url,{
		        					   "idString":idString,
		        			           "inviterString":inviterString,
		        			           "appId":appId
		        				},{
		        		            "0":function(data){
		        		            	hike.log.info('game invite html play cache update begin');
		        		        		ctx.forward('notify.updateAllGameNotifyCache');
		        		        		ctx.forward('notify.updateAllNotifyCountCache');
		        		        		hike.log.info('game invite html play cache update end');
		        		        		ctx.forward("games.htmlplay",{'gameId':appId});
		        		        		hike.log.info('open game page end');
		        		            }
		        		        });
		                    }
		                }
		        	},"1009":function(data){
		                 ctx.openView('game/game-remove',data);
		        	}
		        });
	});
	
	
	hike.registerAction('notify.game.invite.playForOthers', function(dataView, ctx){
		var idString = dataView["idList"];
		var inviterString = dataView["inviterList"];
		var appId = dataView["appId"];
		var packageName = dataView["packageName"];
		var platformType = dataView["platformType"];
		var url = "notify/game/invite/play";
		if(platformType == 1 && (hike.callNative('AppManager', 'isAppInstalled', packageName) != 'true')){
	    	var detailUrl="android/notify/invite/play";
	        ctx.get(detailUrl,{
	        	"idString":idString
	        },{
	        	"0":function(data){
		            data.animation='fade';
		        	ctx.forward('notify.updateAllGameNotifyCache');
		            ctx.forward('notify.updateAllNotifyCountCache');
		            ctx.forward("games.openDetail",{'gameId':appId});
	          }
	        });
        }else{
        	if(platformType == 1){
				ctx.get(url,{
					   "idString":idString,
			           "inviterString":inviterString,
			           "appId":appId
				},{
		            "0":function(data){
		        		ctx.forward('notify.updateAllGameNotifyCache');
		        		ctx.forward('notify.updateAllNotifyCountCache');
		        		ctx.forward('games.play',{'packageName':packageName,'gameId':appId});
		            }
		        });
        		
        	}
        	else{
				ctx.get(url,{
					   "idString":idString,
			           "inviterString":inviterString,
			           "appId":appId
				},{
		            "0":function(data){
		        		ctx.forward('notify.updateAllGameNotifyCache');
		        		ctx.forward('notify.updateAllNotifyCountCache');
		        		ctx.forward("games.htmlplay",{'gameId':appId});
		            }
		        });
            }
        }
	});
	
	
	hike.registerAction('notify.game.invite.openDetail', function(dataset, ctx){
		$(ctx.element).data('action','');
		var idString = dataset["idList"];
		var appId = dataset["appId"];
		var notifyId = dataset["notifyId"];
		var url = "notify/game/invite/openDetail";
		$('li[name="unread'+notifyId+'"]').removeClass('unread');
		ctx.forward("games.openDetail",{'gameId':appId});
		ctx.get(url,{
			   "idString":idString,
	           "appId":appId
		},{
            "0":function(data){
        		ctx.forward('notify.updateAllGameNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
            }
        });
	});
	
	
	hike.registerAction('notify.game.invite.opennOthersDetail', function(dataset, ctx){
		$(ctx.element).data('action','');
		var idString = dataset["idList"];
		var appId = dataset["gameId"];
		var notifyId = dataset["notifyId"];
		var url = "notify/game/invite/openDetail";
		$('li[name="unread'+notifyId+'"]').removeClass('unread');
		
		ctx.get(url,{
			   "idString":idString,
	           "appId":appId
		},{
            "0":function(data){
        		ctx.forward('notify.updateAllGameNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
        		ctx.forward('home.n_others_detail',dataset);
            }
        });
	});
	
	hike.registerAction('notify.game.invite.detail', function(dataView, ctx){
		var idString = dataView["idList"];
		var inviterString = dataView["inviterList"];
		var appId = dataView["appId"];
		var notifyId = dataView["notifyId"];
		var url = "notify/game/invite/detail";
		ctx.get(url,{
			   "inviterString":inviterString,
			   "idString":idString,
	           "appId":appId,
	           "notifyId":notifyId
		},{
            "0":function(data){
        		ctx.forward('notify.updateAllGameNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
            	ctx.openView('notify/detail-gameinvite',data);
            }
        });
	});
	
	hike.registerAction('notify.game.profile.friends', function(dataset, ctx){
		$(ctx.element).data('action','');
		var senderId = dataset["userId"];
		var challengeId = dataset["challengeId"];
		var url = "notify/game/profile/friends";
		$('li[name="unread'+challengeId+'"]').removeClass('unread');
		ctx.forward('profile.friends',{'userId':senderId});
		ctx.get(url,{
	           "challengeId":challengeId
		},{
            "0":function(data){
        		ctx.forward('notify.updateAllGameNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
            	hike.log.info('open other profile page');
            }
        },{"1":function(data){}}
		);
	});
	
	hike.registerAction('notify.result.read', function(dataView, ctx){
		
		var senderId = dataView["userId"];
		var resultId = dataView["resultId"];
		var url = "challenge/result/read";
		ctx.forward('profile.friends',dataView);
		ctx.get(url,{
	           "resultId":resultId
		},{
            "0":function(data){
            	$('li[name="unread'+resultId+'"]').removeClass('unread');
        		ctx.forward('notify.updateAllGameNotifyCache');
        		ctx.forward('notify.updateAllNotifyCountCache');
            }
        });
	});
	
	hike.registerAction('notify.acceptance.read', function(dataView, ctx){
		$(ctx.element).data('action','');
		var senderId = dataView["userId"];
		ctx.forward('notify.updateAllFriendNotifyCache');
		ctx.forward('notify.updateAllNotifyCountCache');
		ctx.forward("profile.friends",{"userId":senderId});
       
	});

	hike.registerAction('notify.game.invite.ignore', function(dataView, ctx) {
		var idString = dataView["idList"];
		var notifyId = dataView["notifyId"];
		var cache = ctx.getCache();
		var cacheData = cache.get("allNotifyCount");
		var url = "notify/game/invite/ignore";
		ctx.get(url,{
			   "idString":idString
		},{
         "0":function(data){
     		var ignore = $('div[name="ignore'+notifyId+'"]');
    		ignore.removeAttr('data-action');
    		ignore.removeClass('del');
    		ignore.addClass('ignored');
    		ignore.html("<span class='btn disablebtn'>Ignored</span>");
    		$('div[name="play'+notifyId+'"]').hide();
    		$('li[name="unread'+notifyId+'"]').removeClass('unread');
    		if(data.gameInviteNotify.status == 0){
    			var gameNotifyCount = parseInt(cacheData.gameNotifyUnreadCount)-parseInt('1');
    			hike.callNative('NativeUI', 'setNotifyCount',cacheData.friendNotifyUnreadCount,gameNotifyCount,cacheData.messageNotifyUnreadCount,cacheData.total);
    		}
    		ctx.forward('notify.updateAllGameNotifyCache');
    		ctx.forward('notify.updateAllNotifyCountCache');
         }
     }, true);
	});

	hike.registerAction('notify.game.invite.back', function(dataset, ctx) {
		hike.backward();
	});
	
	hike.registerAction('notify.system.payment', function(dataView, ctx){
        var url = "notify/system/payment";
        var cache = ctx.getCache();
        var cacheData = cache.get("payNotificationList");
        if(	cacheData!=null&&dataView["curPage"]==1){
        	hike.log.info('payment notify cache begin');
        	if (cacheData.list != null && cacheData.list.length > 0) {
                var len = cacheData.list.length;
                var str = "";
                for (var i = 0; i < len; i++) {
                    var item = cacheData.list[i];
                    
                    var li = "";
                    var message = "";
					if(item.status == 0 && item.type == 1){
						li = "<li class='unread' data-widget='Button' data-action='notify.recharge' data-notify-id='"+item.id+"'>";
					}
					else if(item.status == 0 && item.type == 2){
						li = "<li class='unread' data-widget='Button' data-action='notify.consume' data-notify-id='"+item.id+"'>";
					}
					else if(item.status == 1 && item.type == 1){
						li = "<li data-widget='Button' data-action='notify.recharge' data-notify-id='"+item.id+"'>";
					}
					else if(item.status == 1 && item.type == 2){
						li = "<li data-widget='Button' data-action='notify.consume' data-notify-id='"+item.id+"'>";
					}
						
					//Recharge..........type=1
					if(item.type == 1){
				        message = "<h1>Successful Recharge!<br/>"
						        + "Amount: "+item.coins+"</h1>"
						        + "<h2>"+item.rechargeTime+"</h2>";
					}
					//Consume..........type=2
					if(item.type == 2){
						message = "<h1>Successful Consume!<br/>"
						        + "Game: "+item.gameName+"</h1>"
						        + "<h2>"+item.chargeTime+"</h2>";
					}

					str +=li
						+ message
                        + "</li>";
                }
                hike.log.info('payment notify cache end');
                dataView.append(str,cacheData.hasNextPage);
                ctx.updateView();
            } else{
            	dataView.append('',true);
            	dataView.setHint("No record yet.");
            }
        }
        else{
        dataView.preload();
        ctx.get(url, {
            "page":dataView["curPage"],
            "pageSize":dataView["pageSize"],
            'ts':new Date().getTime()
        }, {
            "0":function(data) {
                if (data.list != null && data.list.length > 0) {
                    var len = data.list.length;
                    var str = "";
                    for (var i = 0; i < len; i++) {
                        var item = data.list[i];
                        
                        var li = "";
                        var message = "";
                    	if(item.status == 0 && item.type == 1){
    						li = "<li class='unread' data-widget='Button' data-action='notify.recharge' data-notify-id='"+item.id+"'>";
    					}
    					else if(item.status == 0 && item.type == 2){
    						li = "<li class='unread' data-widget='Button' data-action='notify.consume' data-notify-id='"+item.id+"'>";
    					}
    					else if(item.status != 0 && item.type == 1){
    						li = "<li data-widget='Button' data-action='notify.recharge' data-notify-id='"+item.id+"'>";
    					}
    					else if(item.status != 0 && item.type == 2){
    						li = "<li data-widget='Button' data-action='notify.consume' data-notify-id='"+item.id+"'>";
    					}
						
                    	//Recharge..........type=1
    					if(item.type == 1){
    				        message = "<h1>Successful Recharge!<br/>"
    						        + "Amount: "+item.coins+"</h1>"
    						        + "<h2>"+item.rechargeTime+"</h2>";
    					}
    					//Consume..........type=2
    					if(item.type == 2){
    						message = "<h1>Successful Consume!<br/>"
    						        + "Game: "+item.gameName+"</h1>"
    						        + "<h2>"+item.chargeTime+"</h2>";
    					}

						str +=li
							+ message
                            + "</li>";
                    }
                    dataView.append(str,data.hasNextPage);
                    ctx.updateView();
                } else{
                	dataView.append('',true);
                	dataView.setHint("No record yet.");
                }
            },
            "103":function() {
                $('.error').html('User does not exist!');
				$('.error').show();
            }
        }, true);
        };
    });
	
	hike.registerAction('notify.recharge', function(dataView, ctx){
		var notifyId = dataView["notifyId"];
		var url = "notify/system/detail?notifyId="+notifyId;
		ctx.get(url,null,{
            "0":function(data){
            	ctx.forward('notify.updatePaymentMessageCache');
            	ctx.forward('notify.updateAllNotifyCountCache');
            	ctx.openView('notify/detail-systemrecharge', data);
            }
        });
	});
	hike.registerAction('notify.consume', function(dataView, ctx){
		var notifyId = dataView["notifyId"];
		var url = "notify/system/detail?notifyId="+notifyId;
		ctx.get(url,null,{
            "0":function(data){
            	ctx.forward('notify.updatePaymentMessageCache');
            	ctx.forward('notify.updateAllNotifyCountCache');
            	ctx.openView('notify/detail-systemconsume', data);
            }
        });
	});
	
	hike.registerAction('notify.updatePaymentMessageCache', function(dataView, ctx){
		 var cache = ctx.getCache();
		 var url = "notify/system/payment";
		 ctx.get(url, {
           "page":dataView["curPage"],
           "pageSize":dataView["pageSize"],
           'ts':new Date().getTime()
       }, {
           "0":function(data) {
         	  if (data.list != null && data.list.length > 0) {
         		  hike.log.info('update payment message cache begin');
         		  cache.persist("payNotificationList", data);
         		  hike.log.info('update payment message cache end');
         	  }
          }
       },true);
	});
	
	hike.registerAction('notify.updateAllFriendNotifyCache', function(dataView, ctx){
		var cache = ctx.getCache();
		var url = "android/notify/friend";
		ctx.get(url,null,{
	            "0":function(data){
	            	hike.log.info('update all friend notify cache begin');
	            	cache.persist("allFriendNotifyList",data);
	            	hike.log.info('update all friend notify cache end');
	        }
		 },true);
	});
	
	hike.registerAction('notify.updateAllGameNotifyCache', function(dataView, ctx){
		var cache = ctx.getCache();
		var url = "android/notify/game";
		ctx.get(url,null,{
	            "0":function(data){
	            	hike.log.info('update all game notify cache begin');
	            	cache.persist("allGameNotifyList",data);
	            	hike.log.info('update all game notify cache end');
	        }
		 },true);
	});
	
	hike.registerAction('notify.updateAllNotifyCountCache', function(dataView, ctx){
		var cache = ctx.getCache();
		var url = "android/notify/home";
		ctx.get(url,null,{
	            "0":function(data){
	            	hike.log.info('update all notify count cache begin');
	            	cache.persist("allNotifyCount",data);
	            	hike.log.info('update all notify count cache end');
	            	hike.log.info('call NativeUI method begin');
	            	hike.callNative('NativeUI', 'setNotifyCount',data.friendNotifyUnreadCount,data.gameNotifyUnreadCount,data.messageNotifyUnreadCount,data.total);
	            	hike.log.info('call NativeUI method end');
	        }
		 },true);
	});
	
	hike.registerAction('notify.updateCountCache', function(dataView, ctx){
		var cache = ctx.getCache();
		var url = "android/notify/home";
		ctx.get(url,null,{
	            "0":function(data){
	            	hike.log.info('update all notify count cache begin');
	            	cache.persist("allNotifyCount",data);
	            	hike.log.info('update all notify count cache end');
	        }
		 },true);
	});
	
	hike.registerAction('notify.clearAllGameNotifyCache', function(dataView, ctx){
		var cache = ctx.getCache();
	    cache.clear("allGameNotifyList");
	});
	
	hike.registerAction('notify.clearAllNotifyCountCache', function(dataView, ctx){
		var cache = ctx.getCache();
	    cache.clear("allNotifyCount");
	});
	
	hike.registerAction('notify.clearNewsfeedCache', function(dataView, ctx){
		var cache = ctx.getCache();
		cache.clear("isAll");
    	cache.clear("newsfeedCache");
    	cache.clear("activitiesCache");
		cache.clear("activitiesIsAll");
	});
	
	hike.registerAction('notify.game.onChallenge', function(dataset, ctx){
        var gameId=dataset["gameId"];
        var challengeType=dataset["challengeType"];
        var requestId=dataset["requestId"];
        var requestScore=dataset["requestScore"];
        var challengeId = dataset["challengeId"];
        var platformType = dataset["platformType"];
        var packageName = dataset["packageName"];
        
        var isAppInstalled = true;
        if(platformType == 1 && (hike.callNative('AppManager', 'isAppInstalled', packageName) != 'true')){
        	$('li[name="unread'+challengeId+'"]').removeClass('unread');
        	isAppInstalled = false;
        }else{
          	var playbtn = $('div[name="play'+challengeId+'"]');
      		var unread = $('li[name="unread'+challengeId+'"]');
      		unread.removeClass('unread');
      		playbtn.removeAttr('data-action');
      		playbtn.removeClass('play');
      		playbtn.addClass('ignored');
      		playbtn.html("<span class='btn disablebtn'>Accepted</span>");
      		$('div[name="ignore'+challengeId+'"]').hide();
        }
      //validate game
        var valiUrl = "game/"+gameId+"/status";
        ctx.get(valiUrl,null,{
                 "0":function(){
                      //var play="play/"+gameId;
                      var play = "android/play/"+gameId+"/"+challengeType+"/"+requestId+"/"+requestScore+"/"+challengeId+ "/new";
              		 if(platformType == 1 && !isAppInstalled ){
              	    	var detailUrl="notify/game/profile/friends";
              	        ctx.get(detailUrl,{
              	        	 "challengeId":challengeId
              	        },function(data){
              	            data.animation='fade';
              	            ctx.forward('notify.updateAllGameNotifyCache');
              	            ctx.forward('notify.updateAllNotifyCountCache');
              	            ctx.forward("games.openDetail",{'gameId':gameId});
              	        });
                      }else if(platformType == 1 && isAppInstalled ){
                  		
                      	ctx.get(play,{
                	           "challengeType":challengeType,
                	            "requestId":requestId,
                	            "requestScore":requestScore,
                	            "challengeId":challengeId
                	        },{
                	            "0":function(data){
                	            	ctx.forward('notify.updateAllGameNotifyCache');
                	            	ctx.forward('notify.updateAllNotifyCountCache');
                      	            ctx.forward('notify.clearNewsfeedCache');
//                	            	ctx.forward('notify.clearAllGameNotifyCache');
//                	            	ctx.forward('notify.clearAllNotifyCountCache');
                	            	hike.callNative('AppManager', 'startForCompete', challengeId, packageName);
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
                      }else{
              	        ctx.get(play,{
              	           "challengeType":challengeType,
              	            "requestId":requestId,
              	            "requestScore":requestScore,
              	            "challengeId":challengeId
              	        },{
              	            "0":function(data){
              	            	 ctx.forward('notify.updateAllGameNotifyCache');
                  	             ctx.forward('notify.updateAllNotifyCountCache');
//              		             ctx.forward('notify.clearAllGameNotifyCache');
//              		             ctx.forward('notify.clearAllNotifyCountCache');
              		             hike.log.info("notify.game.onChallenge data.playUrl = " + data.playUrl);
              		             hike.log.info("notify.game.onChallenge challengeId = " + challengeId);
              		             hike.callNative('AppManager', 'startURL', data.playUrl+"?challengeId="+challengeId);
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
                 },"1009":function(data){
                         ctx.openView('game/game-remove',data);
                 }
        }); 
    });
	
    hike.registerAction('notify.notifyListTabChange', function(obj, ctx){
    	var cache = ctx.getCache();
    	var friendsNotifyIds = cache.get("friendsNotifyIds");
    	var challengeNotifyIds = cache.get("challengeNotifyIds");
    	hike.log.info("friendsNotifyIds = " + friendsNotifyIds);
    	hike.log.info("challengeNotifyIds = " + challengeNotifyIds);
    	var url="android/notify/setNotificationRead";
    	if(friendsNotifyIds!="" && friendsNotifyIds !=null && obj.index ==0 ){
    		ctx.get(url,{"notifyIds":friendsNotifyIds,"type" : obj.index},{
    			"0":function(data){
    				var newNotifyIds = cache.get("friendsNotifyIds");
    				if(notifyIds == newNotifyIds){
    					hike.log.info("clear friendsNotifyIds" );
    					cache.clear("friendsNotifyIds");
    			        ctx.forward('notify.updateAllFriendNotifyCache');
    		            ctx.forward('notify.updateCountCache');
    				}
    			},
    			"1":function(){}
    		},true);
    	}else if(challengeNotifyIds!="" && challengeNotifyIds !=null && obj.index ==1 ){
    		ctx.get(url,{"notifyIds":challengeNotifyIds,"notifyType" : obj.index},{
    			"0":function(data){
    				var newNotifyIds = cache.get("challengeNotifyIds");
    				if(notifyIds == newNotifyIds){
    					hike.log.info("clear challengeNotifyIds" );
    					cache.clear("challengeNotifyIds");
    					ctx.forward('notify.updateAllGameNotifyCache');
     		            ctx.forward('notify.updateCountCache');
    				}
    			},
    			"1":function(){}
    		},true);
    	}
	});
    
})();
