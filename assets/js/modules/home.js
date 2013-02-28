(function(){
	hike.registerAction('home.launch', function(state, ctx){
		var cache = ctx.getCache();
		var user = cache.get('userInfo');
		if(user != null){
			ctx.setItem('user', user);
			if(state != null && state.action != null && state.action != ''){
                ctx.forward(state.action, state.dataset);
        	}else{
                ctx.forward('home.openHomepage');
        	}
		}else{
			ctx.get('checkuser', null, function(data){
				var user = data.user;
				cache.persist('userInfo', user);
				ctx.setItem('user', user);
				if(state != null && state.action != null && state.action != ''){
                    ctx.forward(state.action, state.dataset);
            	}else{
                    ctx.forward('home.openHomepage');
            	}
			}, false);
		}
	});
	hike.registerAction('home.openHomepage', function(dataset, ctx){
		var fillPlaceHolder = function(list, amount){
			var amountNeedFill = amount - list.length;
			for(var i=0;i<amountNeedFill;i++){
				list[list.length] = null;
			}
		}
		var featuredGameListKey = "featuredGameListKey";
		var cache = ctx.getCache();
		var cacheData = cache.get(featuredGameListKey);
		var tabIndex = dataset.tabIndex;
		if(tabIndex != undefined)
			window.sessionStorage.setItem("NativeTab@home/homepage", tabIndex);
		if(cacheData){
			ctx.openView('home/homepage', cacheData);
			ctx.get("android/home",null, {
	            "0":function(data) {
					fillPlaceHolder(data.friendPlayedList); // update 12
					fillPlaceHolder(data.topGamesList);
					fillPlaceHolder(data.defGamesList);
	            	cache.persist(featuredGameListKey,data);
	            }
	        }, true);
		}else{
			ctx.get("android/home",null, {
	            "0":function(data) {
					fillPlaceHolder(data.friendPlayedList);  // update  12
					fillPlaceHolder(data.topGamesList);
					fillPlaceHolder(data.defGamesList);
					cache.persist(featuredGameListKey,data);
	            	ctx.openView('home/homepage', data);
	            }
	        }, true);
		}
	});
	
	hike.registerAction('home.helpcustompin', function(dataset, ctx){
		ctx.openView('home/sign-helpcustompin');
	});
	

	hike.registerAction('home.to-search', function(dataset, ctx){
		//TODO get news feed items from server side.
			ctx.openView('profile/search',{'total':null,'nickName':''});
	});
	
	hike.registerAction('home.openTimeLine', function(dataset, ctx){
		//TODO open timeline page.
			ctx.openView('home/social-timeline',{});
	});
	
	hike.registerAction('home.getNewsfeedData', function(dataView, ctx){
		var url = 'newsfeedFA/list';
		var timeUtil = new hike.android.TimeUtil();
		
		var cache = ctx.getCache();
		var newsfeedCache = cache.get("newsfeedCache");
		var gameListCache = cache.get("gameListCache");
		var total = cache.get("total");
		var lastLoadTime = cache.get("lastLoadTime");//offset = 4320    3 days
		var lastLoadTimeStr = timeUtil.serverTime(lastLoadTime);
		var offset = timeUtil.timeOffset(lastLoadTime);
		if(null!=newsfeedCache && ""!=newsfeedCache && dataView["curPage"]===1 && offset<4320){
			if(total<=10){
				setTimeout(function(){
					$(".game-list").html(gameListCache);
					$(".gameList").show();
					ctx.getWidget("newsfeedlist").update();
				},500);
        	}
			var isAll = cache.get("isAll");
			var newsfeedObj = $(newsfeedCache).each(function(){
				//update newsfeed time
				var time = $(this).find('aside.time').data("feed-time");
				var timeStr = timeUtil.reckonTime(time);
				$(this).find('aside.time').text(timeStr);
			});
			newsfeedCache = "";
			for(var i=0;i<newsfeedObj.length;i++){
				newsfeedCache += newsfeedObj.get(i).outerHTML;
			}
			dataView.append(newsfeedCache,isAll);
			lastLoadTimeStr = lastLoadTimeStr == "" ? "&nbsp;" : "Updated "+lastLoadTimeStr;
			setTimeout(function(){$(".lastLoadTime").html(lastLoadTimeStr);},500);
		}else{
			dataView.preload();
	        ctx.get(url, {
	            "curPage":dataView["curPage"],
	            "pSize":dataView["pageSize"],
	            'ts':new Date().getTime()
	        }, {
	            "0":function(data) {
	            	hike.log.info("get total from server= " + data.total);
	            	if(data.total<=10){
	            		var gameList =  data.gameList;
	                	var gameStr = "";
	                	if(gameList!=null && gameList.length>0){
	                		var count = 0;
	                		$.each(gameList,function(key,val){
	                			count ++;
		                		gameStr += "<li>"+
					            				"<h2><span class=\"name\" data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\">"+val.title+"</span></h2>"+
					            				"<div class=\"content\">"+
					            					"<img class=\"avata game\" data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\" src=\"" + hike.domain.URL_STATIC_IMG + "/" +val.appIconCover.touchIcon+ "\" alt=\""+val.title+"\" />"+
					            					"<div class=\"detail playgame\">"+(410+count*100)+" people are now playing <span class=\"gamename\" data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\">"+val.title+"</span>." +
					            					"</div>"+
					            					"<aside class=\"play bluebtn\" data-action=\"games.playFromNewsfeed\" data-game-id=\""+val.appId+"\">Play</aside>"+
					            				"</div>"+
					            			"</li>";
		                		if(count>=3){
		                			return false; 
		                		}
	                		});
	                	}
	            		setTimeout(function(){
	            			$(".game-list").html(gameStr);
	            			$(".gameList").show();
	            			ctx.getWidget("newsfeedlist").update();
	            			cache.persist("gameListCache", gameStr);
	            		},500);
	            	}
	            	var timeUtil = new hike.android.TimeUtil();
					var feedData = data.feedData;
					var feedHtml = "";
					$.each(feedData,function(index,feed){
						var feedType = feed.feedType;
						if(feedType == 502){//share type  set fBody
							var fBody = feed.data.fBody[0];
							fBody = fBody.replace(/\n/g,"");
							var id = $(fBody).data("subject-id");
							var fID = $(fBody).data("subject-Owner");
							
							var fName = $(fBody).find('div.share-author').text();
							fName = fName.substring(0,fName.length-1);
							var feedTime = $(fBody).find('div.share-content').data("feed-time");
							var timeStr = timeUtil.reckonTime(feedTime);
							var fWords = $(fBody).find('div.share-content').html();
							var isPostStatus = $(fBody).find('div.share-content').data("is-post-status");
							isPostStatus = isPostStatus == undefined ? false : isPostStatus;
							var shareData ={
									"id" : id,
									"fID" : fID,
									"fName" : fName,
									"time" : feedTime,
									"timeStr" : timeStr,
									"fWords" : fWords,
									"isPostStatus" : isPostStatus
							};
							var shareMentObj = ctx.getRenderTpl("9999",shareData);//sharement tpl
							feed.data.fBody[0] = shareMentObj.get(0).outerHTML;
						} 
						var newsfeedObj = ctx.getRenderTpl(feedType,{"feed" : feed});
						newsfeedObj.find(".at").removeAttr("data-action");
						feedHtml += newsfeedObj.get(0).outerHTML;
	                });
					lastLoadTime = new Date().getTime();
					cache.persist("total", data.total);
	            	if(dataView["curPage"]===1){
	            		cache.persist("newsfeedCache", feedHtml);
	            		cache.persist("isAll", data.isAll);
	            		cache.persist("lastLoadTime", lastLoadTime);
	            	}
	            	
	            	lastLoadTimeStr = timeUtil.serverTime(lastLoadTime);
	            	lastLoadTimeStr = lastLoadTimeStr == "" ? "&nbsp;" : "Updated "+lastLoadTimeStr;
	            	setTimeout(function(){$(".lastLoadTime").html(lastLoadTimeStr);},500);
	            	
	            	dataView.append(feedHtml,data.isAll);
	            },
	            "1":function(data) {
	            	var gameList =  data.gameList;
                	var gameStr = "";
                	if(gameList!=null && gameList.length>0){
                		var count = 0;
                		$.each(gameList,function(key,val){
                			count ++;
                			gameStr += "<li>"+
				            				"<h2><span class=\"name\" data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\">"+val.title+"</span></h2>"+
				            				"<div class=\"content\">"+
				            					"<img class=\"avata game\" data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\" src=\"" + hike.domain.URL_STATIC_IMG + "/" +val.appIconCover.touchIcon+ "\" alt=\""+val.title+"\" />"+
				            					"<div class=\"detail playgame\">"+(410+count*100)+" people are now playing <span class=\"gamename\" data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\">"+val.title+"</span>." +
				            					"</div>"+
				            					"<aside class=\"play bluebtn\" data-action=\"games.playFromNewsfeed\" data-game-id=\""+val.appId+"\">Play</aside>"+
				            				"</div>"+
				            			"</li>";
	                		if(count>=3){
	                			return false; 
	                		}
                		});
                	}
                	cache.persist("gameListCache", gameStr);
            		setTimeout(function(){
            			$(".game-list").html(gameStr);
            			$(".gameList").show();
            			ctx.getWidget("newsfeedlist").update();
            		},500);
	            	dataView.append("",true);
	            },
	            "103":function() {
	            	dataView.append("",true);
	            }
	        }, true);
		}
    });
	
	
	hike.registerAction('home.toReply', function(dataset, ctx){
		var defaultContent = "";
		var bodyContent = dataset['bodyContent'];
		if(bodyContent!=null && bodyContent!="" && bodyContent != undefined){
			defaultContent = bodyContent;
		}
		var params = {  "title" : "Reply",
						"buttonName" : "Send",
						"defaultContent" : defaultContent,
						"requireSubject": false};
		
		 window.prompt('ContentEditor', hike.util.encodeJson(params));
		
		 window.onEditCompleted = function(data){
			var commentDesc = data.content;
			if(commentDesc != undefined){
				var tempDesc = commentDesc.replace(/(^\s*)(\s*$)/g,'');
				if(tempDesc.length==0){
					hike.ui.showError("Reply cannot be empty.");
					return ; 
				}
				
				var contentPatrn=/^[a-zA-Z0-9_,\s,\W]{1,140}$/;
				if (!contentPatrn.test(commentDesc)) {
					if(commentDesc.length>0){
						hike.ui.showError("Reply contains illegal characters!");
					}else{
						hike.ui.showError("Reply cannot be empty.");
					}
					return false ;
				}
				
				var patrn = new RegExp('\\@\\w+\\s', "g");
				var startValue = commentDesc.match(patrn);
				var atlist = "";
				if(null!=startValue && startValue.length>0){
					for(var a=0;a<startValue.length;a++){
						atlist += startValue[a];
						if(a<startValue.length-1){
							atlist += "$_$";
						}
					}
				}
				
				var commentType = dataset['commentType'];
				var subjectId = dataset['subjectId'];
				var subjectOwner = dataset['subjectOwner'];
				var subjectOwnerName = dataset['subjectOwnerName'];
				var replyTargetId = dataset['replyTargetId'];
				replyTargetId = replyTargetId == undefined ? "" : replyTargetId;
				var replyTargetOwner = dataset['replyTargetOwner'];
				replyTargetOwner = replyTargetOwner == undefined ? "" : replyTargetOwner;
				
				var params = {
						'feedId' : subjectId,
						'subjectOwner' : subjectOwner,
						'subjectOwnerName' : subjectOwnerName,
						'replyTargetId' : replyTargetId,
						'replyTargetOwner' : replyTargetOwner,
						'commentDesc' : commentDesc,
						'atlist' : atlist,
						'commentType' : commentType
						};
				ctx.post("newsfeed/reply",params,{
		            "0":function(data) {
		            	hike.ui.showError("Send successfully!");
		            	var cache = ctx.getCache();
		            	hike.log.info('clear newsfeedCache begin');
		        		cache.clear("isAll");
		            	cache.clear("newsfeedCache");
		            	cache.clear("activitiesCache");
		        		cache.clear("activitiesIsAll");
		            	hike.log.info('clear newsfeedCache end');
		            	ctx.reload();
		            },
		            "1":function() {
		            	hike.ui.showError("Sent failed.");
		            },
					"451" : function(data) {
						hike.ui.showError("Reply cannot be empty.");
			        },
					"454" : function(data) {
						hike.ui.showError("Reply contains illegal characters!");
			        }
		        }, false)
			 }
		 }
    });
	
	
	/*activites-detail*/
    hike.registerAction('home.activities', function(dataset, ctx){
		var subjectId = dataset["subjectId"];
		var subjectOwner = dataset["subjectOwner"];
    	var params = {
				'feedId' : subjectId,
				'subjectOwner' : subjectOwner
				};
    	ctx.get("newsfeedFA/getNewsfeedById",params,function(data){
    		var timeUtil = new hike.android.TimeUtil();
			var feedData = data.feedData;
			var feedHtml = "";
			$.each(feedData,function(index,feed){
				var feedType = feed.feedType;
				if(feedType == 502){//share type  set fBody
					var fBody = feed.data.fBody[0];
					fBody = fBody.replace(/\n/g,"");
					var id = $(fBody).data("subject-id");
					var fID = $(fBody).data("subject-Owner");
					var fName = $(fBody).find('div.share-author').text();
					fName = fName.substring(0,fName.length-1);
					var feedTime = $(fBody).find('div.share-content').data("feed-time");
					var timeStr = timeUtil.reckonTime(feedTime);
					var fWords = $(fBody).find('div.share-content').html();
					var isPostStatus = $(fBody).find('div.share-content').data("is-post-status");
					isPostStatus = isPostStatus == undefined ? false : isPostStatus;
					var shareData ={
							"id" : id,
							"fID" : fID,
							"fName" : fName,
							"time" : feedTime,
							"timeStr" : timeStr,
							"fWords" : fWords,
							"isPostStatus" : isPostStatus
					};
					var shareMentObj = ctx.getRenderTpl("9999",shareData);//sharement tpl
					feed.data.fBody[0] = shareMentObj.get(0).outerHTML;
				} 
				var newsfeedObj = ctx.getRenderTpl(feedType,{"feed" : feed});
				newsfeedObj.find("aside.comment").data("action","home.toReply");
				feedHtml += newsfeedObj.get(0).outerHTML;
            });
			data.feedHtml = feedHtml;
    		ctx.openView('home/activities-detail',data);
	    })	
    	
    });
	
    hike.registerAction('home.getComments', function(dataView, ctx){
    	var url,params ="";
    	url = "newsfeedFA/getComments";
    	dataView.preload();
		params = {
				"curPage":dataView["curPage"],
	            "pSize":dataView["pageSize"],
	            'ts':new Date().getTime(),
	            'feedId' : dataView["subjectId"],
				'subjectOwner' : dataView["subjectOwner"]
				};
		ctx.get(url, params,{
			"0":function(data){
				var commentsHtml = "";
				var ugcComments = data.ugcComments;
				$.each(ugcComments,function(index,ugcComment){
						var commentObj = ctx.getRenderTpl("9998",{"comment" : ugcComment});//sharement tpl
						commentsHtml += commentObj.get(0).outerHTML;
                });
				dataView.append(commentsHtml,data.isAll);
				dataView.update();
			},
			"1":function(data){
				hike.ui.showError("no data");
			}
		}, true);
	});
	
	hike.registerAction('home.friend-add', function(dataset, ctx) {
		var userId = dataset["userId"];
		var MyName = dataset["myName"];
		var userName = dataset["nickName"];

		var config = {
				title : 'Friend request',
				buttons:[{text:'Send',fn:'home.friendAdd'},{text:'Cancel'}],
				closable : true,
	  			content:'<textarea class="invitecontent" maxlength="50" >Hello!</textarea>'
			};
		hike.ui.showDialog(config);
		
		window.finishUpload = function(){
			//ctx.forward('home.reloadFriendsList');
		}		
		hike.registerAction('home.friendAdd', function(dataset, ctx){
			var contentText = $(".invitecontent").val(); 
			var url = "friend/" + userId + "/add";
			hike.ui.closeDialog();
			ctx.get(url, {content:contentText}, function(data) {
				ctx.forward('home.reloadFriendsList');
			})
		});
	});
	
	hike.registerAction('home.n_others_detail', function(dataset, ctx){
		var param ={
					pids : dataset.pids
				   };
		ctx.get("newsfeedFA/n_others",param,function(data){
			if(dataset.gameId != undefined && dataset.gameId != ""){
				data.gameId = dataset.gameId;
				data.gameName = dataset.gameName;
				data.idList = dataset.idList;
				data.inviterList = dataset.inviterList;
				data.packageName = dataset.packageName;
				data.platformType = dataset.platformType;
				ctx.openView('home/social-timeline-gamenothers', data);
			}else{
				ctx.openView('home/social-timeline-friendnothers', data);
			}
	    })	
	});
	
	
	hike.registerAction('home.refreshSuggestedFriends', function(dataset, ctx){
		var loadingpager = ctx.getWidget("SuggestedFriendsLoadingPager");
		loadingpager.turnPage();
		if(loadingpager)
			ctx.forward('home.getSuggestedmini', loadingpager);		
	});
	hike.registerAction('home.getSuggestedmini', function(loopSlider, ctx){		
		var url = "user/friend/people";
		var curPage=loopSlider["curPage"];
        var perPage=loopSlider["perPage"];
		ctx.get(url, {
            "page":curPage,
            "pagesize":perPage
        },{"0":function(data){
				var friendsListHtml = "";
				var introductionFriendList = data.introductionFriendList;
				$.each(introductionFriendList,function(entryIndex,entry){	                    
	   				friendsListHtml +="<span data-action=\"profile.friends\" data-user-id=\""+entry['id']+"\" id=\""+entry['id']+"\"><img data-stranger-id=\""+entry['id']+"\" src=\""+entry['headUrl']+"\" alt=\""+entry['nickName']+"\" /></span>";
                });
				loopSlider.append(friendsListHtml);
				/*loopSlider.append(introductionFriendList);*/
			},
			"1":function(data){
				hike.ui.showError("no data!");
			}
		}, true);
	});
	hike.registerAction('home.getSuggested', function(loopSlider, ctx){
		
		var url = "user/friend/people";
		var curPage=loopSlider["curPage"];
        var perPage=loopSlider["perPage"];
		ctx.get(url, {
            "page":curPage,
            "pagesize":perPage
        },{"0":function(data){
				var friendsListHtml = "";
				var introductionFriendList = data.introductionFriendList;
				$.each(introductionFriendList,function(entryIndex,entry){	                    
	   				friendsListHtml +="<span data-action=\"profile.friends\" data-user-id=\""+entry['id']+"\" id=\""+entry['id']+"\"><img data-stranger-id=\""+entry['id']+"\" src=\""+entry['headUrl']+"\" alt=\""+entry['nickName']+"\" /><em>"+entry['nickName']+"</em></span>";
                });
				loopSlider.append(friendsListHtml);
				/*loopSlider.append(introductionFriendList);*/
			},
			"1":function(data){
				hike.ui.showError("no data!");
			}
		}, true);
	});
	
	hike.registerAction('home.toShare', function(dataset, ctx){
		var defaultContent = "";
		var bodyContent = dataset['bodyContent'];
		if(bodyContent!=null && bodyContent!="" && bodyContent != undefined){
			defaultContent = bodyContent;
		}
		var params = {  "title" : "Share",
						"buttonName" : "Send",
						"defaultContent" : defaultContent,
						"requireSubject": false};
		
		 window.prompt('ContentEditor', hike.util.encodeJson(params));
		
		 window.onEditCompleted = function(data){
			var shareContent = data.content;
			if(shareContent != undefined){
				var tempDesc = shareContent.replace(/(^\s*)(\s*$)/g,'');
				if(tempDesc.length==0){
					hike.ui.showError("Share content cannot be empty.");
					return ; 
				}
				
				var contentPatrn=/^[a-zA-Z0-9_,\s,\W]{1,140}$/;
				if (!contentPatrn.test(shareContent)) {
					if(shareContent.length>0){
						hike.ui.showError("Share content contains illegal characters!");
					}else{
						hike.ui.showError("Share content cannot be empty.");
					}
					return false ;
				}
				
				var patrn = new RegExp('\\@\\w+\\s', "g");
				var startValue = shareContent.match(patrn);
				var atlist = "";
				if(null!=startValue && startValue.length>0){
					for(var a=0;a<startValue.length;a++){
						atlist += startValue[a];
						if(a<startValue.length-1){
							atlist += "$_$";
						}
					}
				}
				
				var commentType = dataset['commentType'];
				var subjectId = dataset['subjectId'];
				var subjectOwner = dataset['subjectOwner'];
				var subjectOwnerName = dataset['subjectOwnerName'];
				var fBodyId = dataset['fBodyId'];
				fBodyId = fBodyId == undefined ? "" : fBodyId;
				var fBodyOwner = dataset['fBodyOwner'];
				fBodyOwner = fBodyOwner == undefined ? "" : fBodyOwner;
				
				var params = {
						'feedId' : subjectId,
						'subjectOwner' : subjectOwner,
						'subjectOwnerName' : subjectOwnerName,
						'shareContent' : shareContent,
						'fBodyId' : fBodyId,
						'fBodyOwner' : fBodyOwner,
						'atlist' : atlist,
						'commentType' : commentType
						};
				ctx.post("newsfeed/share",params,{
		            "0":function(data) {
		            	hike.ui.showError("Share successfully!");
		            	var cache = ctx.getCache();
		        		cache.clear("isAll");
		            	cache.clear("newsfeedCache");
		            	cache.clear("activitiesCache");
		        		cache.clear("activitiesIsAll");
		        	//	window.location.reload();
		        		ctx.reload();
		            //	setTimeout(function(){history.go(-1);},3000);
		            },
		            "1":function() {
		            	hike.ui.showError("Share failed.");
		            },
					"451" : function(data) {
						hike.ui.showError("Share content cannot be empty.");
			        },
					"454" : function(data) {
						hike.ui.showError("Share content contains illegal characters!");
			        }
		        }, false)
			 }
		 }
	});
	
	hike.registerAction('home.at', function(dataset, ctx){
		//dataset['bodyContent'] = $('.commentDesc').val();
		var bodyContent = $('.commentDesc').val();
		ctx.setItem("bodyContent",bodyContent);
		ctx.openView('home/newsfeed-at',dataset);
	});
	
	
	hike.registerAction('home.sharesb', function(dataset, ctx){
		var nickName = dataset['nickname'];
		var maxlength = dataset['maxlength'];
		var atFriends = $('.atDesc').val();
		var patrn = new RegExp('\\@\\w+\\s', "g");
		var startValue = atFriends.match(patrn);
		if((null==startValue || startValue.length<10) && null!=nickName && atFriends.length<maxlength){
			var placeholder="Who/What do you want to @? Multiple choice for 10 times."
			if(atFriends.indexOf(placeholder)>=0){
				$('.atDesc').val( atFriends.replace(placeholder,""));
				$('.atDesc').css("color","#666");
				$('.atDesc').val("@"+nickName+" ");
			}else{
				$('.atDesc').val(atFriends+"@"+nickName+" ");
			}
			$('.atDesc').val($('.atDesc').val().substr(0 ,maxlength));
		}	
	});
	
	hike.registerAction('home.sharegame', function(dataset, ctx){
		var gamename = dataset['gamename'];
		var maxlength = dataset['maxlength'];
		var atFriends = $('.atDesc').val();
		var patrn = new RegExp('\\@\\w+\\s', "g");
		var startValue = atFriends.match(patrn);
		if((null==startValue || startValue.length<10) && null!=gamename && atFriends.length<maxlength){
			var placeholder="Who/What do you want to @? Multiple choice for 10 times."
			if(atFriends.indexOf(placeholder)>=0){
				$('.atDesc').val( atFriends.replace(placeholder,""));
				$('.atDesc').css("color","#666");
				$('.atDesc').val("@"+gamename+" ");
			}else{
				$('.atDesc').val(atFriends+"@"+gamename+" ");
			}
			$('.atDesc').val($('.atDesc').val().substr(0 ,maxlength));
		}	
	});
	
	hike.registerAction('home.atCounter', function(dataset, ctx){
		var element = arguments[0].element;
		setTimeout(function(){
			   var content = $(element).find(".contents");
			   var atMaxlength = dataset['atMaxlength'];
			   var maxlength = dataset["maxlength"];
			   
			   content.bind('keydown keyup focus input propertychange',function(){
				   var val = content.val();
				   var patrn = new RegExp('\\@\\w+\\s', "g");
				   var atFriends = val.match(patrn);
				   if(null!=atFriends){
					   if(atFriends.length < atMaxlength && val.length<maxlength){
						   content.removeAttr("maxlength"); 
					   }else if(atFriends.length >= atMaxlength ){
						   content.attr("maxlength",val.length); 
						   content.val(val.substr(0 ,maxlength)); 
					   }else{
						   content.attr("maxlength",maxlength); 
						   content.val(val.substr(0 ,maxlength)); 
					   }
				   }
			   });
		},1);
	});
	
	hike.registerAction('home.refreshNewsfeed', function(dataView, ctx){
        var url = 'newsfeedFA/refresh';
        
        ctx.get(url, {'ts':new Date().getTime()}, {
            "0":function(data) {
            	var cache = ctx.getCache();
            	$(".news-list").html("");
            	$(".game-list").html("");
            	if(data.total<=10){
            		var gameList =  data.gameList;
                	var gameStr = "";
                	if(gameList!=null && gameList.length>0){
                		var count = 0;
                		$.each(gameList,function(key,val){
                			count ++;
	                		gameStr += "<li>"+
				            				"<h2><span class=\"name\">"+val.title+"</span></h2>"+
				            				"<div class=\"content\">"+
				            					"<img class=\"avata game\" data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\" src=\"" + hike.domain.URL_STATIC_IMG + "/" +val.appIconCover.touchIcon+ "\" alt=\""+val.title+"\" />"+
				            					"<div class=\"detail\">"+(410+count*100)+" people are now playing <span data-action=\"games.openDetail\" data-game-id=\""+val.appId+"\">"+val.title+"</span>.</div>"+
				            				"</div>"+
				            			"</li>";
	                		if(count>=3){
	                			return false; 
	                		}
                		});
                	}
                	cache.persist("gameListCache", gameStr);
            		setTimeout(function(){
            			$(".game-list").html(gameStr);
            			$(".gameList").show();
            			ctx.getWidget("newsfeedlist").update();
            		},500);
            	}
            	var timeUtil = new hike.android.TimeUtil();
				var feedData = data.feedData;
				var feedHtml = "";
				$.each(feedData,function(index,feed){
					var feedType = feed.feedType;
					if(feedType == 502){//share type  set fBody
						var fBody = feed.data.fBody[0];
						fBody = fBody.replace(/\n/g,"");
						var id = $(fBody).data("subject-id");
						var fID = $(fBody).data("subject-Owner");
						var fName = $(fBody).find('div.share-author').text();
						fName = fName.substring(0,fName.length-1);
						var feedTime = $(fBody).find('div.share-content').data("feed-time");
						var timeStr = timeUtil.reckonTime(feedTime);
						var fWords = $(fBody).find('div.share-content').html();
						var isPostStatus = $(fBody).find('div.share-content').data("is-post-status");
						isPostStatus = isPostStatus == undefined ? false : isPostStatus;
						var shareData ={
								"id" : id,
								"fID" : fID,
								"fName" : fName,
								"time" : feedTime,
								"timeStr" : timeStr,
								"fWords" : fWords,
								"isPostStatus" : isPostStatus
						};
						var shareMentObj = ctx.getRenderTpl("9999",shareData);//sharement tpl
						feed.data.fBody[0] = shareMentObj.get(0).outerHTML;
					} 
					var newsfeedObj = ctx.getRenderTpl(feedType,{"feed" : feed});
					newsfeedObj.find(".at").removeAttr("data-action");
					feedHtml += newsfeedObj.get(0).outerHTML;
                });
            	ctx.getWidget("newsfeedlist").append(feedHtml,data.isAll);
            	ctx.getWidget("newsfeedlist").curPage=2;
            	ctx.getWidget("newsfeedlist").update();
            	var lastLoadTime = new Date().getTime();
            	var	lastLoadTimeStr = timeUtil.serverTime(lastLoadTime);
            	
            	lastLoadTimeStr = lastLoadTimeStr == "" ? "&nbsp;" : "Updated "+lastLoadTimeStr;
            	$(".lastLoadTime").html(lastLoadTimeStr);
            	
        //    	var cache = ctx.getCache();
            	cache.persist("total", data.total);
        		cache.persist("isAll", data.isAll);
        		cache.persist("newsfeedCache", feedHtml);
            	cache.persist("lastLoadTime", lastLoadTime);
            },
            "1":function(data) {
            	var cache = ctx.getCache();
            	hike.ui.showError("Already latest one.");
            	var timeUtil = new hike.android.TimeUtil();
            	var lastLoadTime = new Date().getTime();
            	var	lastLoadTimeStr = timeUtil.serverTime(lastLoadTime);
            	lastLoadTimeStr = lastLoadTimeStr == "" ? "&nbsp;" : "Updated "+lastLoadTimeStr;
            	$(".lastLoadTime").html(lastLoadTimeStr);
            	cache.persist("lastLoadTime", lastLoadTime);
            }
        }, true);
    });
	
	hike.registerAction('home.delNewsfeedDialog', function(dataset, ctx) {
		var feedId = dataset['feedId'];
		var visitType = dataset['visitType'];
		var owner = dataset['owner'];
		var config = {
				title : 'Sure to delete?',
				buttons:[{text:'Delete',fn:'home.deleteNewsfeed'},{text:'Cancel'}],
				closable : true,
	  			content:''
			};
		hike.ui.showDialog(config);
		
		hike.registerAction('home.deleteNewsfeed', function(dataset, ctx){	
			var url = "newsfeed/delete";
			hike.ui.closeDialog();
			ctx.post(url, {
							"feedId" : feedId,
							"owner"  : owner,
							"visitType" : visitType
						},{
				"0":function(data) {
					$(ctx.getWidget("newsfeedlist").element).find("li[data-newsfeed-id='newsfeed_"+feedId+"']").remove();
					
					var cache = ctx.getCache();
	            	hike.log.info('clear newsfeedCache begin');
	        		cache.clear("isAll");
	            	cache.clear("newsfeedCache");
	            	cache.clear("activitiesCache");
	        		cache.clear("activitiesIsAll");
	            	hike.log.info('clear newsfeedCache end');
					
					hike.ui.showError("Delete successfully.");
					
				},
				"903" : function() {
					hike.ui.showError("newsfeed is not exists.");
				},
				"904" : function() {
					hike.ui.showError("The newsfeed is not you created!");
				}
			})
		});	
	}); 
	
	hike.registerAction('home.getMyFriends', function(dataView, ctx){
	var url = "newsfeed/myfriends";
    ctx.post(url, {
        "curPage":dataView["curPage"],
        "pSize":dataView["pageSize"]
    },{
        "0":function(data) {
        	if(data.total<=0){
        		var boardStr = "If you want to @ friends, please add a friend first.";
            	dataView.append(boardStr,true);
        	} else if (data.list && data.list.length > 0) {
                var userNameList = data.list;
                var boardStr = "";
                $.each(userNameList,function(entryIndex,entry){ 
                	boardStr += "<li data-action='home.sharesb' data-maxlength='140' data-nickname='"+ entry +"'>"+ entry +"</li>";
                });
                dataView.append(boardStr,data.isAll);
            }else{
            	dataView.append(boardStr,true);
            }
        }
    }, true);
}, true);
	/*hike.registerAction('home.gameDownload', function(dataset, ctx){	 //alert windows
		var config = {
				title : '',
				closable : true,
				content : '<div class="android">This game need to be '+'<a href="'+hike.domain.URL_STATIC_UI+dataset['downloadUrl']+'" > download </a>'+' it frist.If you have downloaded.Please open the app to play. Thanks!'		
			};
			hike.ui.showDialog(config);
	});*/
	
	hike.registerAction('home.getAtGames', function(dataView, ctx){
		var gameType=dataView["gameType"];
		var gameUrl="game/all";
        var page=dataView["curPage"];
        var pageSize=dataView["pageSize"];
        var stopLoad=false;
		dataView.preload();
		
		ctx.get(gameUrl, {
            "page":page,
            "pageSize":pageSize
        },{"0":function(data){
        	var gameStr = "";
    		var gameList =  data.list;
        	
        	if(gameList!=null && gameList.length>0){
        		$.each(gameList,function(key,val){
           		 	gameStr += '<li data-action="home.sharegame" data-maxlength="140" data-gamename="'+val.title+'" data-game-id="'+val.appId+'">'+val.title+'</li>'
        		});
        	 }else{
        		 var gameStr = "If you want to @ games, please play a game first.";
        		 stopLoad=true;
        	 }        	         	       	   
        	 if(gameList==null || data.hasNextPage) 
 		 	{
 		 		stopLoad=true;
 		 	}
 			dataView.append(gameStr,stopLoad);
        },
		"1":function(data){
			$('.error').html('No games !');
			$('.error').show();
		}
		}, true);
	  });
	hike.registerAction('home.verifycodeControl', function(){
		setTimeout(function(){
			var control = function(e){
				$(e.target).val($(e.target).val().replace(/[^0-9.]/g,''));
			}
			$("#pincode").bind('keyup',control);
		},10);
	});
	
	hike.registerAction('home.quit', function(){
		hike.callNative('ActivityInvoker', 'finish');
	});
	
	
})();
