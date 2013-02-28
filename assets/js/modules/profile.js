(function(){
	
	hike.registerAction('profile.openMyFriendList', function(dataset, ctx){
		var userId = ctx.getItem('user').userId;
		ctx.openView('profile/my-friends', {
			userInfo : {
				id : userId
			}
		});
	});
	
	
	hike.registerAction('profile.my-badges', function(dataset, ctx) {
		var url = "profile";
		ctx.get(url, null, function(data) {
			ctx.openView('profile/my-badges',data);
		})	
	});
	
	//this action is for my friends list , not only for current user.
	hike.registerAction('profile.my-friends', function(dataset, ctx) {
		var url = "profile";
		ctx.get(url, null, function(data) {
			ctx.openView('profile/my-friends',data);
		})
	});
	
    
  //this action is for other friends list , not only for current user.
	hike.registerAction('profile.OthersFriendsLink', function(dataset, ctx) {
		var userId = dataset["userId"];
		var isMutual = dataset["isMutual"];
		ctx.openView('profile/other-friends',{'userId':userId,'isMutual':isMutual});
	});
	

    
    hike.registerAction('profile.getOthersFriends', function(dataView, ctx){
		var getBtnStr = function(item,id){
            
			var  addmsg ="add"+id ;
            var  rsendmsg ="rsend"+id ;
            
			if("NoPath"===item.relation || "Applied"===item.relation||"Apply"===item.relation){
				return  '<div class="gameinvite" id="'+addmsg+'" data-action="friend.add"  data-user-id="' + item.userId +
		        '" data-nick-name="' + item.nickName + '" data-my-name="' + item.myName + '">'+
				        '<span class="bluebtn invite">Add</span></div> '+
				        '<div class="gameinvite" id="'+rsendmsg+'" style ="display:none" >'+
					      '<span class="disablebtn">Request sent </span >'+
					    '</div>' ; 
			}else if("Friend"===item.relation){
				return   '<div class="control"> '
				        +' <span class="chat" data-widget="Button" data-action=""></span>  </div> '  ;
				//<span data-widget="Button" data-action="friend.remove" data-user-id="' + item.userId + '" data-nick-name="' + item.nickName + '">Remove</span>
			}else {
				return  '<div class="gameinvite">'+
		        '<span class="bluebtn invite weak" data-widget="Button" data-action="profile.friends" data-user-id="' + item.userId + '">View</span></div> ' ;
			}
		};	
		var url = "andriodprofile/others-friends/" + dataView["userId"];
		dataView.preload();
        ctx.get(url, {
            "page":dataView["curPage"],
            "pagesize":dataView["pageSize"]
        },{
            "0":function(data) {
                if (data.list && data.list.length > 0) {
                    var list = data.list;
                    var len = list.length;
                    
                    var boardStr = "";
                    for (var i = 0; i < len; i++) {
                        var item = list[i];
                        boardStr += '<li data-widget="Button"><div class="wrapper">'
                        	+ '<img src="' + item.headUrl + '" data-action="profile.friends" data-user-id="' + item.userId + '">'
                        	+ '<div class="info">'
                        	+ '<div class="name" data-action="profile.friends" data-user-id="'+item.userId+'">' + item.nickName+'</div>'
                        	+ '<div class="mutual" >'+item.desc+'</div>'
            			    + '</div>'
							+ getBtnStr(item,item.userId) 
							+ '</div></li>';
                    }
                    //dataView.append(boardStr,list.length<dataView["pageSize"]?true:false);
                    dataView.append(boardStr,data.hasNextPage);
                }else{
                	var isMySelf = data.isMySelf ; 
                	if(isMySelf==true){
                		dataView.setHint("You have no friend yet.") ;	
                	}else{
                		dataView.setHint("No friend yet.") ;
                	}
                	 
                }
            }
        }, true);
	}, true);
    
    hike.registerAction('profile.getOthersMutualFriends', function(dataView, ctx){
		var getBtnStr = function(item){
			return  '<div class="control"> <span class="chat" data-widget="Button" data-action=""></span> </div>';
		};
		var url = "andriodprofile/others-mutual-friends/" + dataView["userId"];
		dataView.preload();
        ctx.get(url, {
            "page":dataView["curPage"],
            "pagesize":dataView["pageSize"]
        },{
            "0":function(data) {
                if (data.list && data.list.length > 0) {
                    var list = data.list;
                    var len = list.length;
                    
                    var boardStr = "";
                    for (var i = 0; i < len; i++) {
                        var item = list[i];
                        boardStr += '<li data-widget="Button"><div class="wrapper">'
                        	+ '<img src="' + item.headUrl + '" data-action="profile.friends" data-user-id="' + item.userId + '">'
                        	+ '<div class="info">'
                        	+ '<div class="name" data-action="profile.friends" data-user-id="' + item.userId + '">' + item.nickName + '</div>'
                        	+ '<div class="mutual" >'+item.desc+'</div>'
                        	+ '</div>'
                        	+ getBtnStr(item) 
							+ '</div></li>';
                    }
					
                    //dataView.append(boardStr,list.length<dataView["pageSize"]?true:false);
                    dataView.append(boardStr,data.hasNextPage);
                }else{
                	 dataView.setHint("No mutual friend yet.") ;
                }
            }
        }, true);
	}, true);

	hike.registerAction('profile.tabActivate', function(dataset, ctx) {
		ctx.getWidget(dataset['tabGroupId']).openPage(parseInt(dataset["tabIndex"]));
	});
	
	hike.registerAction('profile.basic-info', function(dataset, ctx) {
		var url = "andriodprofile";
		var cache = ctx.getCache();
		var basicInfoCache = cache.get("basicInfoCache");
		if(null!=basicInfoCache){
			ctx.openView('profile/profile', basicInfoCache);
			ctx.get(url, {
				"version":"v1"
			}, function(data) {
				if(null!=data){
					cache.persist("basicInfoCache",data);
				}
			}, true);
		}else{
			ctx.get(url, {
				"version":"v1"
			}, function(data) {
				cache.persist("basicInfoCache",data);
				ctx.openView('profile/profile', data);
			})
		}
	});
	
	hike.registerAction('profile.friends', function(dataset, ctx) {
		var userId = dataset["userId"];
		var url = "andriodprofile/" + userId +"/info";
		ctx.get(url,  {
			"version":"v1"
		}, function(data) {
			if(data.isMyProfile){
				ctx.openView('profile/profile', data);
			}else{
				ctx.openView('profile/profile-user', data);
			}
		});	
	});
	
	hike.registerAction('profile.feedback', function(dataset, ctx) {
		ctx.get('feedback', null, {
			"101" : function(data) {
				ctx.forward('home.launch');
			},
			"0" : function(data) {
				ctx.openView('profile/feedback', data);
			}
		})
	});
	hike.registerAction('profile.feedbackSubmit', function(dataset, ctx) {
		$('.error').hide();
		var gameId = $(".feedback_fbType").val();
		var title = $(".topicsubject").val();
		var content = $(".contents").val();
		if(content.length == 0){
			$('.error').html('content cannot be empty!');
			$('.error').show();
		}		
		ctx.post("feedbacksubmit", {
			"gameId" : gameId,
			"title" : title,
			"content" : content
		},{
			"450" : function() {
				$('.error').html('title cannot be empty!');
				$('.error').show();
			},
			"451" : function() {
				$('.error').html('content cannot be empty!');
				$('.error').show();
			},
			"452" : function() {
				$('.error').html('title should be less than 30 letters.');
				$('.error').show();
			},
			"453" : function() {
				$('.error').html('content should be less than 500 letters.');
				$('.error').show();
			},
			"0" : function() {
				 hike.backward();
			}
		})
	});
	hike.registerAction('profile.personalinfo', function(dataset, ctx) {
		var url = "personal";
		ctx.get(url, null, function(data) {
			ctx.openView('profile/personalinfo', data);
		})
	});
	hike.registerAction('profile.dateControl', function(){
		setTimeout(function(){
			var control = function(e){
				$(e.target).val($(e.target).val().replace(/[^0-9.]/g,''));
			}
			$(".personalinfo_month").bind('keyup',control);
			$(".personalinfo_day").bind('keyup',control);
			$(".personalinfo_year").bind('keyup',control);
		},10);
	});
	hike.registerAction('profile.personalinfoSubmit', function(dataset, ctx) {
		var gender ;
		var nickName = $(".personalinfo_nickName").val().replace(/(^\s*)(\s*$)/g,'');
		var firstName = $(".personalinfo_firstName").val().replace(/(^\s*)(\s*$)/g,'');
		var lastName = $(".personalinfo_lastName").val().replace(/(^\s*)(\s*$)/g,'');
		var gender1 = $("input[name=gender]");
		if(gender1[0].checked){
			gender = gender1[0].value;
		}else{
			gender = gender1[1].value;
		}
		var day = $(".personalinfo_day").val();
		var month = $(".personalinfo_month").val();
		var year = $(".personalinfo_year").val();
 
		var birth = day+'/'+month+'/'+year;
		$('.nickerror').hide();
		$('.firsterror').hide();
		$('.lasterror').hide();
		$('.birtherror').hide();
		var namePatrn=/^[a-zA-Z0-9_,\s]{3,50}$/;
		var birthPatrn = /^([0-3]{1}[0-9]{1}|[1-9]{0,1})\/([0-1]{1}[0-9]{1}|[0-9]{0,1})\/[0-9]{4}$/

		if (!namePatrn.test(nickName)) {
			if(nickName.length<3){
				$('.nickerror').html("Nickname cannot be less than 3 letters.");
			}else if(nickName.length>15){
				$('.nickerror').html("Nickname cannot be more than 15 letters.");
			}else{
				$('.nickerror').html("Nickname contains illegal characters!");
			}
			$('.nickerror').show();
			hike.pageScroller.scrollToElement($('.personalinfo_nickName').get(0),1000);
			$('.personalinfo_nickName')[0].focus();
			return false;
		}
		
		if(nickName.substring(0,1)=="_"||nickName.substring(nickName.length-1,nickName.length)=="_"){
			$('.nickerror').html("The first or last letter can't be underline.");
			$('.nickerror').show();
			hike.pageScroller.scrollToElement($('.personalinfo_nickName').get(0),1000);
			$('.personalinfo_nickName')[0].focus();
			return false;
		}
		
		if (firstName != undefined &&firstName.length>0&& !namePatrn.test(firstName)) {
			if(firstName.length > 0 && firstName.length<3){
				$('.firsterror').html("fullName cannot be less than 3 letters.");				
			}else if(firstName.length>30){
				$('.firsterror').html("fullName cannot be more than 20 letters.");
			}else{
				$('.firsterror').html("fullName contains illegal characters!");
			}			
			$('.firsterror').show();
			hike.pageScroller.scrollToElement($('.personalinfo_firstName').get(0),1000);
			$('.personalinfo_firstName')[0].focus();
			return false;
		}
		
		if(firstName.substring(0,1)=="_"||firstName.substring(firstName.length-1,firstName.length)=="_"){
			$('.nickerror').html("The first or last letter can't be underline.");
			$('.nickerror').show();
			hike.pageScroller.scrollToElement($('.personalinfo_firstName').get(0),1000);
			$('.personalinfo_firstName')[0].focus();
			return false;
		}
		
		if (lastName != undefined &&lastName.length>0&&!namePatrn.test(lastName)) {
			if(lastName.length > 0 && lastName.length<3){
				$('.lasterror').html("fullName cannot be less than 3 letters.");				
			}else if(lastName.length>30){
				$('.lasterror').html("fullName cannot be more than 20 letters.");
			}else{
				$('.lasterror').html("fullName contains illegal characters!");
			}
			$('.lasterror').show();
			hike.pageScroller.scrollToElement($('.personalinfo_lastName').get(0),1000);
			$('.personalinfo_lastName')[0].focus();
			return false;
		}	
		if(birth != '//' && !birthPatrn.test(birth)){
			$('.birtherror').html("Date of Birth contains illegal characters!");
			$('.birtherror').show();
			hike.pageScroller.scrollToElement($('.personalinfo_month').get(0),1000);
			$('.personalinfo_month')[0].focus();
			return false;
		}
		ctx.post("personal", {
			'nickName' : nickName,
			'firstName' : firstName,
			'lastName' : lastName,
			'gender' : gender,
			'day' : day,
			'month' : month,
			'year' : year
		}, {
			"401" : function() {
				$('.nickerror').html('Nickname can not be empty!');
				$('.nickerror').show();
				$('.personalinfo_nickName').get(0).scrollIntoView();
				hike.pageScroller.scrollToElement($('.personalinfo_nickName').get(0),1000);
				$('.personalinfo_nickName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
				
			},
			"402" : function() {
				$('.nickerror').html('Nickname should be more than 3 letters!');
				$('.nickerror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_nickName').get(0),1000);
				$('.personalinfo_nickName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"403" : function() {
				$('.firsterror').html('First name contains illegal characters!');
				$('.firsterror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_firstName').get(0),1000);
				$('.personalinfo_firstName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"404" : function() {
				$('.lasterror').html('Last name contains illegal characters!');
				$('.lasterror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_lastName').get(0),1000);
				$('.personalinfo_lastName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"405" : function() {
				$('.birtherror').html('Date of Birth contains illegal characters!');
				$('.birtherror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_month').get(0),1000);
				$('.personalinfo_month')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"406" : function() {
				$('.nickerror').html('Nickname contains illegal characters!');
				$('.nickerror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_nickName').get(0),1000);
				$('.personalinfo_nickName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"407" : function() {
				$('.nickerror').html('Nickname has been used!');
				$('.nickerror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_nickName').get(0),1000);
				$('.personalinfo_nickName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"410" : function() {
				$('.nickerror').html('Nickname should not be more than 30 letters!');
				$('.nickerror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_nickName').get(0),1000);
				$('.personalinfo_nickName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"411" : function() {
				$('.firsterror').html('First name should not be more than 30 letters!');
				$('.firsterror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_firstName').get(0),1000);
				$('.personalinfo_firstName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"412" : function() {
				$('.lasterror').html('Last name should not be more than 30 letters!');
				$('.lasterror').show();
				hike.pageScroller.scrollToElement($('.personalinfo_lastName').get(0),1000);
				$('.personalinfo_lastName')[0].focus();
				//hike.ui.focus($('.nickerror').get(0));
			},
			"0" : function() {
				//clear basicInfo Cache
				var cache = ctx.getCache()
				cache.clear("basicInfoCache");
				cache.clear("userInfo");
				//clear newsfeed cache if change nickname.
				cache.clear("activitiesCache");
        		cache.clear("activitiesIsAll");
        		cache.clear("activitiesLastLoadTime");
				
				ctx.get("androidpersonal", null, function(data) {  
					ctx.getCache().persist("settingCache", data);
					ctx.setItem("userSetting",data.userSetting);
				});
				ctx.get('checkuser', null, function(data){
					var user = data.user;
					cache.persist('userInfo', user);
					ctx.setItem('user', user);
				});
				 hike.backward();
			}
		});
	});
	
	hike.registerAction('profile.uploadAvatar', function(dataset, ctx){
		hike.callNative('ImageUploader', 'uploadAvatar');
		window.onAvatarUploaded = function(url){
			var cache = ctx.getCache();
			var basicInfoCache = cache.get("basicInfoCache");
			basicInfoCache.headUrl= url;
			basicInfoCache.userInfo.headUrl = url;
			cache.persist("basicInfoCache",basicInfoCache);
			var userInfoCache = cache.get("userInfo");
			userInfoCache.headUrl=url;
			cache.persist("userInfo",userInfoCache);
			cache.clear("activitiesCache");
			cache.clear("newsfeedCache");
			ctx.reload();
		};
	});
	
	hike.registerAction('profile.uploadCover', function(dataset, ctx){
		hike.callNative('ImageUploader', 'uploadCover');
		window.onCoverUploaded = function(url){
			var cache = ctx.getCache();
			var basicInfoCache = cache.get("basicInfoCache");
			basicInfoCache.userInfo.coverUrl = url;
			cache.persist("basicInfoCache",basicInfoCache);
			var userInfoCache = cache.get("userInfo");
			userInfoCache.coverUrl = url;
			cache.persist("userInfo",userInfoCache);
			//cache.clear("activitiesCache");
			//cache.clear("newsfeedCache");
			ctx.reload();
		};
	});
	
	
	
	
	hike.registerAction('profile.getFriends', function(dataView, ctx){
		var userId= dataView["userId"] ;
		var url =""  ;  
		var nickname =$("#name").val();
		var name =dataView['nickName'];
		
		if(nickname!=""&&dataView!= undefined && dataView['nickName'] != undefined){	
			url = "profile/friend/search";
		}else{
			nickname ="" ; 
		    url = "profile/"+userId+ "/getFriends";  
		}
		
		var getBtnStr = function(item){
			if("NoPath"===item.relation || "Applied"===item.relation){
				return '<div class="operation"><input type="button" data-widget="Button" data-action="friend.invite" data-user-id="' + item.userId + '" data-nick-name="' + item.nickName + '" data-my-name="' + item.myName + '" value="Add" /></div>';
			}
			return '<div class="operation"><input type="button" data-widget="Button" data-action="profile.friends" data-user-id="' + item.userId + '" value="View" /></div>';
		};
		
		dataView.preload();
        ctx.get(url, {
            "page":dataView["curPage"],
            "pagesize":dataView["pageSize"],
        	"nickName":nickname,
			"userId":userId
        },{
            "0":function(data) {
            	var boardStr ="";
                if (data.friends != undefined && data.friends != null && data.friends.length > 0) {
				    var list = data.friends;
                    var len = list.length;
                    for (var i = 0; i < len; i++) {
                        var item = list[i];
                        boardStr += '<li data-widget="Button" data-action="profile.friends" data-user-id="'+item.userId+'">'
                        	+'<div class="wrapper"><img src="' + item.headUrl
                        	+ '" /><div class="info"><div class="name">' + item.nickName
                        	+ '</div><div class="mutual">' + item.mutualFriendCount
                        	+ ' mutual friends</div></div>'
                        	+ getBtnStr(item)
                        	+ '</div></li>';
                    }
					
                    dataView.append(boardStr,list.length<dataView["pageSize"]?true:false);
                }else{
                	dataView.append(boardStr,true);
                }
            }
        }, true);
	});
	
	
	hike.registerAction('profile.getVisitors', function(dataView, ctx){
		var userId= dataView["userId"] ;
		var url = "profile/"+userId+ "/getVisitors";  
		
		var getBtnStr = function(item,userItem,myName){
			var  relation =item.relation;
			if("NoPath"===relation || "Applied"===relation){
				return '<div class="operation"><input type="button" value="Add" data-widget="Button" data-action="friend.invite" data-user-id="' + userItem.id + '" data-nick-name="' + userItem.nickName + '" data-my-name="' + myName + '" /></div>';
			}else if("Apply"===relation){
				return '<div class="operation"><input type="button" value="Request Sent" data-widget="Button" disabled class="weak"/></div>';	
			}else if("Friend"===relation){
			    return '<div class="operation"><input type="button" value="View" data-widget="Button" data-action="profile.friends" data-user-id="' + userItem.id + '" /></div>';
			}else{
				return '' ; 
			}
		};
		dataView.preload();
		
		hike.log.info("visitors cache begin");
		var cache = ctx.getCache();
		var visitorsCache = cache.get("visitorsCache");
		var boardStr ="";
		if(dataView['curPage']===1 && null != visitorsCache && undefined != visitorsCache){
			 hike.log.info('go visitorsCache = '+ visitorsCache);
			 var list = visitorsCache.visitors;
		     var pagedVistors = visitorsCache.pagedVistors;
		     var hasNextPage = visitorsCache.hasNextPage ;
		     var total  =visitorsCache.total ; 
		     var myName  = visitorsCache.hostUser.nickName
	         var len = list.length;
	         for (var i = 0; i < len; i++) {
	             var item = list[i];
	             var pagedVistorsItem = pagedVistors[i];
	             
	             boardStr += '<li data-widget="Button" data-action="profile.friends" data-user-id="'+item.id+'">'
	             	+ '<div class="wrapper"><img src="'+item.headUrl+'" />'
	             	+ '<div class="info">'
	             	+ '<div class="name">' + item.nickName+ '</div>'
	             	//+ '<div class="mutual">' + pagedVistorsItem.mutualFriendCount+ ' mutual friends</div>'
	             	+ '<div class="time">' +item.visitTime+'</div></div>'
	             	+ getBtnStr(pagedVistorsItem,item,myName)
	             	+ '</div></li>';
	         }
	         dataView.append(boardStr,hasNextPage);
	         hike.log.info('update visitorsCache begin');
	         ctx.get(url, {
		            "page":dataView["curPage"],
		            "pagesize":dataView["pageSize"],
		        },{
		            "0":function(data) {
		                if (data.visitors != undefined && data.visitors != null && data.visitors.length > 0) {
		                    cache.persist("visitorsCache",data);
		                    hike.log.info('update visitorsCache end');
		                }
		            }
		     }, true);
	         
	         hike.log.info('go visitorsCache end ');
		}else{
			hike.log.info('get new visitors from server = '+ visitorsCache);
			ctx.get(url, {
	            "page":dataView["curPage"],
	            "pagesize":dataView["pageSize"],
	        },{
	            "0":function(data) {
	                if (data.visitors != undefined && data.visitors != null && data.visitors.length > 0) {
					    var list = data.visitors;
					    var pagedVistors = data.pagedVistors;
					    var hasNextPage = data.hasNextPage ;
					    var total  =data.total ; 
					    var myName  = data.hostUser.nickName
	                    var len = list.length;
	                    for (var i = 0; i < len; i++) {
	                        var item = list[i];
	                        var pagedVistorsItem = pagedVistors[i];
	                        
	                        boardStr += '<li data-widget="Button" data-action="profile.friends" data-user-id="'+item.id+'">'
	                        	+ '<div class="wrapper"><img src="'+item.headUrl+'" />'
	                        	+ '<div class="info">'
	                        	+ '<div class="name">' + item.nickName+ '</div>'
	                        	//+ '<div class="mutual">' + pagedVistorsItem.mutualFriendCount+ ' mutual friends</div>'
	                        	+ '<div class="time">' +item.visitTime+'</div></div>'
	                        	+ getBtnStr(pagedVistorsItem,item,myName)
	                        	+ '</div></li>';
	                    }
	                    dataView.append(boardStr,hasNextPage);
	                    if(dataView['curPage']===1 || null == visitorsCache){
	                    	cache.persist("visitorsCache",data);
	                    }
	                }else{
	                	dataView.append(boardStr,true);
	                }
	            }
	        }, true);
			
		}
	});
	
	/*hike.registerAction('profile.getRecentlyPlayed', function(dataView, ctx){
		//var url = "profile/" + dataView["userId"] + "/getRecentlyPlayed";
		var url = "recentlyPlay/" + dataView["userId"] + "/1";
		var userId = dataView["userId"];
		var loginId = dataView["loginId"];
		var recentlyPlaycache = ctx.getCache();
		var recentlyPlayListKey = "recentlyPlayListKey";
		var recentlyPlaycacheData = recentlyPlaycache.get(recentlyPlayListKey);
		dataView.preload(); 
		if(null != recentlyPlaycacheData && loginId == userId){
			 if (recentlyPlaycacheData.recentlyPlayed != null && recentlyPlaycacheData.recentlyPlayed.length > 0) {
                 var recentlyPlayList = recentlyPlaycacheData.recentlyPlayed;
                 var recentlyPlayLen = recentlyPlayList.length;
                 var boardStr = "";
                 for (var i = 0; i < recentlyPlayLen; i++) {
                     var item = recentlyPlayList[i];
                     var scorestr = "" ; 
                     if(item.score != -1){
                     	scorestr =  item.score + "points " 
                     }
                     boardStr += '<li data-widget="Button" data-action="games.openDetail"  data-game-id = "'+item.gameId+'" ><div class="wrapper">'
                     	+ '<img src="' + hike.domain.URL_STATIC_IMG + '/' + item.appIconCover.touchIcon + '" />'
                     	+ '<div class="info"><div class="name">' + item.title + '</div><div class="mutual">' + scorestr + '</div></div>'
                     	+ '</div></li>';
                 }
                 
                 dataView.append(boardStr,recentlyPlaycacheData.hasNextPage);
                 ctx.get(url, null,{"0":function(data){
             	 recentlyPlaycache.persist(recentlyPlayListKey,data);
             	 var playCachelist = recentlyPlaycache.get(recentlyPlayListKey).recentlyPlayed;
             	 var l = playCachelist.length;
             	 var gameStr = "";
	             	 for (var x = 0; x < l; x++) {
	                	 var item = playCachelist[x];
	                	 var scorestr = "" ; 
	                        if(item.score>0){
	                        	scorestr =  item.score + "points " 
	                        }
						 gameStr += '<li data-widget="Button" data-action="games.openDetail" data-game-title="'+item.title+'" data-downloadurl="'+item.downloadUrl+'" data-game-id = "'+item.gameId+'" data-package-name="'+item.packageName+'" data-game-type="'+item.platformType+'"><div class="wrapper">'
                        	+ '<img src="' + hike.domain.URL_STATIC_IMG + '/' + item.appIconCover.touchIcon + '" />'
                        	+ '<div class="info"><div class="name">' + item.title + '</div><div class="mutual">' + scorestr + '</div></div>'
                        	+ '</div></li>';
	                 }
		            hike.callNative('NativeUI', 'setRecentlyPlayed', hike.util.encodeJson(playCachelist));
		            dataView.reset(gameStr,true); 
	  		        }
	  				}, true);
                 
             }
		}else{
	        ctx.get(url, {
	            "page":dataView["curPage"],
	            "pagesize":dataView["pageSize"]
	        },{
	            "0":function(data) {
	                if (data.recentlyPlayed != null && data.recentlyPlayed.length > 0) {
	                    var list = data.recentlyPlayed;
	                    var len = list.length;
	                    var boardStr = "";
	                    for (var i = 0; i < len; i++) {
	                        var item = list[i];
	                        var scorestr = "" ; 
	                        if(item.score != -1){
	                        	scorestr =  item.score + "points " 
	                        }
	                        boardStr +=  '<li data-widget="Button" data-action="games.openDetail" data-game-title="'+item.title+'" data-downloadurl="'+item.downloadUrl+'" data-game-id = "'+item.gameId+'" data-package-name="'+item.packageName+'" data-game-type="'+item.platformType+'"><div class="wrapper">'
	                        	+ '<img src="' + hike.domain.URL_STATIC_IMG + '/' + item.appIconCover.touchIcon + '" />'
	                        	+ '<div class="info"><div class="name">' + item.title + '</div><div class="mutual">' + scorestr + '</div></div>'
	                        	+ '</div></li>';
	                    }
	                    if(dataView['curPage'] === 1 || null == recentlyPlaycacheData){
	                    	recentlyPlaycache.persist(recentlyPlayListKey,data);
	                    }
	                    dataView.append(boardStr,data.hasNextPage);
	                }else{
	                	var cache = ctx.getCache();
	            		var featuredGameListKey = "featuredGameListKey";
	            		var cacheData = cache.get(featuredGameListKey);
	                	var list = cacheData.featureGameList;
	                    var len = list.length;
	                    var boardStr = "";
	                    for (var i = 0; i < len; i++) {
	                        var item = list[i];
	                        boardStr += '<li data-widget="Button" data-action="games.play"  data-game-id = "'+item.appId+'" ><div class="wrapper">'
	                        	+ '<img src="' + hike.domain.URL_STATIC_IMG + '/' + item.appIconCover.touchIcon + '" />'
	                        	+ '<div class="info"><div class="name">' + item.title + '</div></div>'
	                        	+ '</div></li>';
	                    }
	                    boardStr = '<div class="itemtitle">Featured Game</div>'+boardStr
	                    dataView.append(boardStr,hasNextPage);  
	                }
	            }
	        }, true);
		}
	});*/
	
	hike.registerAction('profile.getNewsfeeds', function(dataView, ctx){
		var isMyProfile = dataView["userType"];
		hike.log.info("activities cache begin");
		var cache = ctx.getCache();
		var activitiesCache = cache.get("activitiesCache");
		var activitiesLastLoadTime = cache.get("activitiesLastLoadTime");//offset = 4320    3 days
		var timeUtil = new hike.android.TimeUtil();
		var offset = timeUtil.timeOffset(activitiesLastLoadTime);
		var curPage = dataView["curPage"];
		if(isMyProfile == "true"  && null!=activitiesCache && curPage===1 && offset<4320){
			hike.log.info("go activities cache");
			var activitiesIsAll = cache.get("activitiesIsAll");
			
			hike.log.info('used activitiesCache');
			var newsfeedObj = $(activitiesCache).each(function(){
				//update newsfeed time
				var time = $(this).find('aside.time').data("feed-time");
				var timeStr = timeUtil.reckonTime(time);
				$(this).find('aside.time').text(timeStr);
			});
			activitiesCache = "";
			for(var i=0;i<newsfeedObj.length;i++){
				activitiesCache += newsfeedObj.get(i).outerHTML;
			}
			dataView.append(activitiesCache,activitiesIsAll);
		}
		
		
		//update activities data
			if(curPage===1 && isMyProfile == "true" && null!=activitiesCache){
				dataView["curPage"] = curPage;
			}
			var url = "newsfeedFA/" + dataView["userId"] + "/getNewsfeeds";
			dataView.preload();  
	        ctx.get(url, {
	            "page":dataView["curPage"],
	            "pagesize":dataView["pageSize"]
	        },{
	            "0":function(data) {
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
            		if(curPage===1 && isMyProfile == "true"){
            			if(null!=activitiesCache){
            				dataView.reset(feedHtml,data.isAll);
            			}
            		}else{
            			dataView.append(feedHtml,data.isAll);
            		}
            		cache.persist("activitiesCache", feedHtml);
            		cache.persist("activitiesIsAll", data.isAll);
            		cache.persist("activitiesLastLoadTime", new Date().getTime());
	            }
	        },true);
	}, true);
	
	
	
	
	hike.registerAction('profile.userBadges', function(dataSet, ctx){
		var userId = dataSet["userId"]
		ctx.openView('profile/my-badges',{'userId':userId,});
	});
	hike.registerAction('profile.getUserBadges', function(dataView, ctx){
		var userId = dataView["userId"]
		
		var url = "profile/"+userId+"/getBadges"
        ctx.get(url, {    
        },{
            "0":function(data) {
                if (data.badges && data.badges.length > 0) {
                    var list = data.badges;
                    var len = list.length;
                    var boardStr = "";
                    for (var i = 0; i < len; i++) {
                        var item = list[i];
                        boardStr += '<li><div class="badgepic">'
                        	+ '<img src="'+hike.domain.URL_STATIC_UI+'/img/touch/' + item.pic + '" alt= "" >'
                        	+ '</div>'
                        	+ '<div class="info"><span class="name">' + item.name + '</span>'
            				+ '<span class="des">Desctiption:<em>'+ item.description +'</em></span>'
            			    + '</div>'
							+ '</li>';
                    }
                    dataView.append(boardStr,true);
                }else{
                	dataView.append("",true);
                }
            }
        }, true);
	}, true);
	
	hike.registerAction('profile.openOthersFriends', function(dataSet, ctx){
		var url = "profile/others-friends-count/" + dataSet["userId"];
        ctx.get(url, {},{
            "0":function(data) {
				var tabActive = ('Y'===dataSet['isMutual']?1:0);
                ctx.openView('profile/other-friends',{
					'userId':dataSet['userId'],
					'tabActive':tabActive,
					'friendCount':data.friendCount,
					'mutualFriendCount':data.mutualFriendCount===null?0:data.mutualFriendCount
				});
            }
        });
	}, true);
	
	
	hike.registerAction('profile.activies', function(dataset, ctx) {
		var userId = dataset["userId"];
		var url = "profile/" + userId + "/activies";
		ctx.get(url, null, function(data) {
			ctx.openView('profile/activies', data);
		})
	});
	
	
	hike.registerAction('profile.setting', function(dataset, ctx) {
		var cache = ctx.getCache();
		var settingCache = cache.get("settingCache");
		var url = "androidpersonal"; 
		var isZone = hike.domain.SOURCE;  
		if(settingCache!=null){
			if(isZone=='hike'){
				ctx.forward('profile.paymentCenter',settingCache);
			}else{
				ctx.openView('profile/settings', settingCache);	
			}
			ctx.get(url, null, function(data) {
				if(null!=data){
					cache.persist("settingCache", data);
				}
			}, true);
		}else{
			ctx.get(url, null, function(data) {
				cache.persist("settingCache", data);
				if(isZone=='hike'){
					ctx.forward('profile.paymentCenter',data);			
				}else{
			    ctx.openView('profile/settings', data);
			    }
			})
		}
	});
	

	hike.registerAction('profile.paymentCenter', function(dataset, ctx){
		//ctx.setItem('paymentType','forSNS');
		var cache = ctx.getCache();
		var paymentCache = cache.get("paymentCache");
		var detailUrl="payment:recharge";    
		if(null != paymentCache){
			for(var name in paymentCache){
				dataset[name]=paymentCache[name];
			}
			hike.log.info("profile.paymentCache");
			ctx.openView('profile/hikesettings', dataset);
			
			ctx.get(detailUrl,null,{
                "200":function(data){
                	$(".tokenInput").data("jsp_token",data.jsp_token);
                	hike.log.info("update paymentCache jsp_token="+data.jsp_token);
                	hike.log.info("update paymentCache jsp_token="+$(".tokenInput").data('jsp_token'));
                	data.rechargeType = 'SNS';
                	cache.persist("paymentCache", data);
                	
                	if(data.user.nickName != paymentCache.user.nickName && paymentCache.user.nickName != null && undefined != $(".name").text()){
                		$(".name").text(data.user.nickName);
                	}
                	if(data.ac.hikeCoin != paymentCache.ac.hikeCoin && paymentCache.ac.hikeCoin != null && undefined != $(".level").text()){
                		var coinDes = data.ac.hikeCoin > 1? "Coins" : "Coin";
                		$(".level").text(data.ac.hikeCoin + " " + coinDes);
					}              	
                },
                "202":function(data){
                }
            });
		}else{
		     ctx.get(detailUrl,null,{
	                "200":function(data){
	                	for(var name in data){
	        				dataset[name]=data[name];
	        			}
	                	data.rechargeType = 'SNS';
	                	hike.log.info("profile.payment");
	                	ctx.openView('profile/hikesettings', dataset);
	                	cache.persist("paymentCache", data);
	                },
	                "202":function(data){
	                }
	            });
		}
	});	
	
	
	
	
	hike.registerAction('profile.defaultsetting', function(dataset, ctx) {
		var url = "notify/default"; 
		ctx.post(url, null, function(data) {  // 锟矫碉拷 setting 锟斤拷锟斤拷
			//ctx.openView('profile/settings', data);
			ctx.forward('profile.paymentCenter',data);
		})
	});
	
	hike.registerAction('profile.changeLogintype', function(dataset, ctx) {
		setTimeout(function(){
			var control = function(e){
				var val=$("input[name=logintype]:checked").val();
				if(val==1){
					$(".setcustom").html(
						'<div><div class="des">Set your PIN here, only letters and numbers 6 - 16</div>'+
						'<p class="customtxt"><input class="pinnoenter" type="password" placeholder="Enter your custom PIN"  required maxlength="16"  value="" /></p>'+
						'<p class="customtxt"><input class="pinnoverify" type="password" placeholder="Verify your custom PIN" required maxlength="16" value="" /></p></div>'
					);
				}else{
					$(".setcustom").html("");
				}
			}
			$("input[name=logintype]").bind('change',control);
				
		},10);			
	});
	
	hike.registerAction('profile.changeCustompin', function(dataset, ctx) {
		if($(ctx.element).data('ispin') == '1'){
			$(ctx.element).removeClass('selectyes');
			$(ctx.element).addClass('selectno');
			$(ctx.element).text('no');
			$(".setcustom").html("");
			$(ctx.element).data('ispin','0');
		}else{
			$(ctx.element).removeClass('selectno');
			$(ctx.element).addClass('selectyes');
			$(ctx.element).text('yes');
			$(".setcustom").html(
					'<div class="des">Set your PIN here, only letters and numbers 6 - 16</div>'+
					'<p class="customtxt"><input class="pinnoenter" type="password" placeholder="Enter your custom PIN"  required maxlength="16"  value="" /></p>'+
					'<p class="customtxt"><input class="pinnoverify" type="password" placeholder="Verify your custom PIN" required maxlength="16" value="" /></p>'
			);
			$(ctx.element).data('ispin','1');
		}
	});
	
	hike.registerAction('profile.settingSubmit', function(dataset, ctx) {
		
		var  friendsRequestPhone=ctx.getWidget("friendsRequestPhone").getValue();
		var  friendsRequestMail=ctx.getWidget("friendsRequestMail").getValue() ;    
		var  friendsAcceptancePhone=ctx.getWidget("friendsAcceptancePhone").getValue() ; 
		var  friendsAcceptanceMail=ctx.getWidget("friendsAcceptanceMail").getValue()  ; 
		
		var  gameCompetitionPhone=ctx.getWidget("gameCompetitionPhone").getValue() ;   
		var  gameCompetitionMail=ctx.getWidget("gameCompetitionMail").getValue()  ;   
		var  gameInvitationPhone=ctx.getWidget("gameInvitationPhone").getValue()  ;  
		var  gameInvitationMail=ctx.getWidget("gameInvitationMail").getValue()  ;  
		
		var  gameResultsPhone=ctx.getWidget("gameResultsPhone").getValue()   ;  
		var  gameResultsMail=ctx.getWidget("gameResultsMail").getValue()    ;   
		var  systemMessagePhone= ctx.getWidget("systemMessagePhone").getValue() ;   
		var  systemMessageMail=ctx.getWidget("systemMessageMail").getValue()    ;   
		
		var  personalInfo=$("input[name='pType']:checked").val(); 
		var  activities=$("input[name=aType]:checked").val(); 
		var  visitors=$("input[name=vType]:checked").val(); 
		var  recentlyPlayed=$("input[name=rType]:checked").val(); 
		var  forums=$("input[name=fType]:checked").val(); 
		
	    var  isPin=$("input[name=logintype]:checked").val();
	    
	    if(isPin==1){
	    	var  pinnoenter= $(".pinnoenter").val();
	    	var  pinnoverify= $(".pinnoverify").val();
	    	
	    //  add  the check of  pinnoenter and pinnoverify , only be char and number   
	    	var namePatrn=/^[a-zA-Z0-9,\s]+$/;
			if (!namePatrn.test(pinnoenter)) {
				hike.ui.showError("Custom PIN contains illegal letters!") ;
	    		return ;
			}
			if (!namePatrn.test(pinnoverify)) {
				hike.ui.showError("Custom PIN contains illegal letters!") ;
	    		return ;
			}
	    	if(pinnoenter.length==0||pinnoverify.length==0){
	    		hike.ui.showError("Pin number can't be empty!") ;
	    		return ;
	    	}else if(pinnoenter.length<6||pinnoverify.length<6){
	    		hike.ui.showError("Pin number should more than 6 char") ;
	    		return ;
	    	}
	    	if(pinnoenter!=pinnoverify){
	    		hike.ui.showError("Custom PINs differ!") ;
	    		$(".pinnoenter").val("");
	    		$(".pinnoverify").val("");
	    		return ;
	    	}
	    	
	    }else{
	    	 $(".pinnoenter").val("");
			 $(".pinnoverify").val("");	
	    }
		ctx.post("notify/submit", {
				'friendsRequestPhone' : friendsRequestPhone,
				'friendsRequestMail' : friendsRequestMail,
				'friendsAcceptancePhone' : friendsAcceptancePhone,
				'friendsAcceptanceMail' : friendsAcceptanceMail,
				'gameCompetitionPhone' : gameCompetitionPhone,
				'gameCompetitionMail' : gameCompetitionMail,
				'gameInvitationPhone' : gameInvitationPhone,
				'gameInvitationMail' : gameInvitationMail,
				'gameResultsPhone' : gameResultsPhone,
				'gameResultsMail' : gameResultsMail,
				'systemMessagePhone' : systemMessagePhone,
				'systemMessageMail' : systemMessageMail,
				'personalInfo' : personalInfo,
				'activities' : activities,
				'visitors' : visitors,
				'recentlyPlayed' : recentlyPlayed,
				'forums' : forums ,
				'isPin' : isPin,
				'pinNo' : pinnoenter
		}, {
				"0" : function() {
					 hike.backward();
					var url = "notify"; 
					ctx.get(url, null, function(data) {  
						ctx.getCache().persist("settingCache", data);
						ctx.setItem("userSetting",data.userSetting);
					})
				}
			});
	
	});
	
	
	
	hike.registerAction('profile.to-search-results', function(dataset, ctx){
		$('.searchMsg').hide();
		$('.searchMsg').html("");
		//TODO get news feed items from server side.
		var nickName = $(".searchKeyword").val();
		if(nickName==null||nickName==undefined){
			nickName = '';
		}
		
		var nickNamePatrn=/^[a-zA-Z0-9,\s][a-zA-Z0-9_,\s]{3,50}[a-zA-Z0-9,\s]$/;
		if (!nickNamePatrn.test(nickName)) {
			if(nickName.length>3){
				$('.searchMsg').html("Nickname contains illegal characters!");
			}else{
				$('.searchMsg').html("Nickname cannot be less than 3 letters.");
			}
			$('.searchMsg').show();
			return false ;
		}
		
		var searchType = 'standad';
		ctx.invokeAction('profile.do-search', {
			nickName : nickName,
			searchType : searchType
		});
	});
	
	
	hike.registerAction('profile.do-standad-search', function(datadset, ctx) {
		$('.resultsMsg').hide();
		$('.resultsMsg').html("");
		var dataview = ctx.getWidget("searchView");
		dataview.reset("",true);
		dataview.preload();
		var nickName = $(".searchKeyword").val();
		dataview['nickName'] = nickName;
		dataview["searchType"] = 'standad';
		dataview["curPage"] = 1;
		dataview["pageSize"] = 10; 
		
		var url,parame ="";
		parame = {
				"curPage":dataview["curPage"],
	            "pSize":dataview["pageSize"],
	            'ts':new Date().getTime(),
				'nickName' : nickName 
				};
		url = "search/user/standad";
		
		ctx.get(url,parame,{
			"0" : function(data){
					var friendsListHtml = "";
					var users = data.users;
					var descMap = data.descMap;
					var total = data.total;
					
					$.each(users,function(entryIndex,entry){  
		                var buttonHtml = "";
		                if(entry['weight']!=10){
		                	buttonHtml ="<div class=\"btninvite\" id=\"add"+entry['id']+"\" data-action=\"friend.add\" data-user-id=\""+entry['id']+"\" data-my-Name=\""+data.myName+"\" data-nick-Name=\""+entry['nickName']+"\">" +
				                			"<a href=\"javascript:void(0)\" class=\"bluebtn invite\">Add</a>" +
					                	"</div>"+
										 "<div class=\"btninvite\" style =\"display:none\" id=\"rsend"+entry['id']+"\">"+
										 	"<span class=\"disablebtn\">Request sent </span >"+
										 "</div>";
		                }else if(entry['weight']==10){
		                	buttonHtml =  "<div class=\"btninvite\" data-action=\"profile.friends\" data-user-Id=\""+entry['id']+"\"><a href=\"javascript:void(0)\" class=\"bluebtn invite\" >View</a></div>";
		                }
		                
		                
		   				friendsListHtml += "<li data-widget=\"Button\" data-widget=\"Button\" data-action=\"profile.friends\" data-user-id=\""+entry['id']+"\">"+
											    "<div class=\"wrapper\">"+
											        "<img src=\""+entry['headUrl']+"\" />"+
											        "<div class=\"info\">"+
											        	"<div class=\"name\">"+entry['nickName']+"</div>"+
											              "<div class=\"mutual\">"+descMap[entry['id']]+"</div>"+
											         "</div></div>"+buttonHtml+
										    "</li>";
		            });
					var	totalHtml = "<span class=\"number\">"+ total +"</span><span class=\"fontcolor\">People found.</span>";
					dataview.reset(friendsListHtml,data.isAll);
					setTimeout(function(){$(".showresult").html(totalHtml);},500);
			},
			"401" : function(data) {
				$('.resultsMsg').html("Search box cannot be less than 3 letters.");
				$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
				$('.resultsMsg').show();
				dataview.reset("",true);
	        },
	        "402" : function(data) {
	        	$('.resultsMsg').html("Search box cannot be less than 3 letters.");
	        	$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
	            $('.resultsMsg').show();
	            dataview.reset("",true);
	        },
	        "406" : function(data) {
	        	$('.resultsMsg').html("Search box contains illegal characters!");
	        	$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
	            $('.resultsMsg').show();
	            dataview.reset("",true);
	        }
		});
	});
	
	
	hike.registerAction('profile.getSearchResult', function(dataView, ctx) {
		var nickName = dataView["nickName"];
		if(nickName==null||nickName==undefined){
			nickName = '';
		}
		var firstName = $("#firstName").val();
		firstName = firstName==undefined ? '':firstName;
		var lastName = $("#lastName").val();
		lastName = lastName==undefined ? '':lastName;
		var gender = dataView["gender"]; 
		gender = gender==undefined ? '':gender;
		var minAge = dataView["minAge"];
		minAge = minAge==undefined ? '':minAge;
		var maxAge = dataView["maxAge"];
		maxAge = maxAge==undefined ? '':maxAge;
		var searchType = dataView["searchType"];
		var url,parame ="";
		if(searchType=="standad"){
			parame = {
					"curPage":dataView["curPage"],
		            "pSize":dataView["pageSize"],
		            'ts':new Date().getTime(),
					'nickName' : nickName 
					};
			url = "search/user/standad";
		}else if(searchType=="advanced"){
			parame = {
					"curPage":dataView["curPage"],
		            "pSize":dataView["pageSize"],
		            'ts':new Date().getTime(),
					'nickName' : nickName ,
					'firstName' : firstName ,
					'lastName' : lastName ,
					'gender' : gender ,
					'minAge' : minAge ,
					'maxAge' : maxAge 
					};
			url = "search/user/advanced";
		}else {
			return;
		}
		dataView.preload();
		ctx.get(url,parame,{
			"0" : function(data){
					var friendsListHtml = "";
					var users = data.users;
					var descMap = data.descMap;
					var total = data.total;
					
					$.each(users,function(entryIndex,entry){  
						var buttonHtml = "";
		                if(entry['weight']!=10){
		                	buttonHtml ="<div class=\"btninvite\" id=\"add"+entry['id']+"\" data-action=\"friend.add\" data-user-id=\""+entry['id']+"\" data-my-Name=\""+data.myName+"\" data-nick-Name=\""+entry['nickName']+"\">" +
				                			"<a href=\"javascript:void(0)\" class=\"bluebtn invite\">Add</a>" +
					                	"</div>"+
					                	"<div class=\"btninvite\" style =\"display:none\" id=\"rsend"+entry['id']+"\">"+
										 	"<span class=\"disablebtn\">Request sent </span >"+
										 "</div>";
		                }else if(entry['weight']==10){
		                	buttonHtml =  "<div class=\"btninvite\" data-action=\"profile.friends\" data-user-Id=\""+entry['id']+"\"><a href=\"javascript:void(0)\" class=\"bluebtn invite\" >View</a></div>";
		                }
		                
		                
		   				friendsListHtml += "<li data-widget=\"Button\" data-action=\"profile.friends\" data-user-id=\""+entry['id']+"\">"+
											    "<div class=\"wrapper\">"+
											        "<img src=\""+entry['headUrl']+"\" />"+
											        "<div class=\"info\">"+
											        	"<div class=\"name\">"+entry['nickName']+"</div>"+
											              "<div class=\"mutual\">"+descMap[entry['id']]+"</div>"+
											         "</div></div>"+ buttonHtml+
										    "</li>";
		            });
					var	totalHtml = "<span class=\"number\">"+ total +"</span><span class=\"fontcolor\">People found.</span>";
					dataView.append(friendsListHtml,data.isAll);
					$(".showresult").html(totalHtml);
			},
			"401" : function(data) {
				$('.resultsMsg').html("Nickname cannot be less than 3 letters.");
				$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
				$('.resultsMsg').show();
	            dataView.append("",true);
	        },
	        "402" : function(data) {
	        	$('.resultsMsg').html("Nickname cannot be less than 3 letters.");
	        	$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
	            $('.resultsMsg').show();
	            dataView.append("",true);
	        },
			"403" : function(data) {
				$('.resultsMsg').html("First name contains illegal characters!");
				$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
	            $('.resultsMsg').show();
	            dataView.append("",true);
	        },
	        "404" : function(data) {
	        	$('.resultsMsg').html("Last name contains illegal characters!");
	        	$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
	            $('.resultsMsg').show();
	            dataView.append("",true);
	        },
	        "406" : function(data) {
	        	$('.resultsMsg').html("Nickname contains illegal characters!");
	        	$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
	            $('.resultsMsg').show();
	            dataView.append("",true);
	        },
	        "408" : function(data) {
	        	$('.resultsMsg').html("Age contains illegal characters!");
	        	$(".showresult").html("<span class=\"number\">0</span><span class=\"fontcolor\">People found.</span>");
	            $('.resultsMsg').show();
	            dataView.append("",true);
	        }
		}, true);
	});
	
	
	
	hike.registerAction('profile.to-advanced-search', function(dataset, ctx) {
		ctx.openView('profile/advanced-search', {});
	});
	
	hike.registerAction('profile.advanced-search', function(dataset, ctx) {
		$('.advancedMsg').hide();
		$('.advancedMsg').html("");
		var searchType = "advanced";
		var nickName = $("#nickName").val();
		var firstName = $("#firstName").val();
		var lastName = $("#lastName").val();
		var gender = ""; 
		if($("input[name='gender']")[0].checked){
			gender = 0;
		}else if($("input[name='gender']")[1].checked){
			gender = 1;
		}
		
		var minAge = $("#ageFrom").val();
		var maxAge = $("#ageTo").val();

		var nickNamePatrn=/^[a-zA-Z0-9,\s][a-zA-Z0-9_,\s]{3,20}[a-zA-Z0-9,\s]$/;
		if (!nickNamePatrn.test(nickName)) {
			if(nickName.length>3){
				$('.advancedMsg').html("Nickname contains illegal characters!");
			}else{
				$('.advancedMsg').html("Nickname cannot be less than 3 letters.");
			}
			$('.advancedMsg').show();
			return false ;
		}
			
		var firstNamePatrn=/^[a-zA-Z]{0,30}$/;
		if (!firstNamePatrn.test(firstName)) {
			$('.advancedMsg').html("First name contains illegal characters!");
            $('.advancedMsg').show();
			return false ;
		}
		
		var lastPatrn=/^[a-zA-Z]{0,30}$/;
		if (!lastPatrn.test(lastName)) {
			$('.advancedMsg').html("Last name contains illegal characters!");
			$('.advancedMsg').show();
			return false ;
		}
		
		if(!(""===minAge && ""===maxAge)){
			var minAgePatrn = /^[1-9][0-9]{0,1}$/;
			var maxAgePatrn = /^[2-9]\d?|100$/;
			if (!minAgePatrn.test(minAge)){
				$('.advancedMsg').html("Age contains illegal characters!");
				$('.advancedMsg').show();
				return false ;
			}
			
			if (!maxAgePatrn.test(maxAge)){
				$('.advancedMsg').html("Age contains illegal characters!");
				$('.advancedMsg').show();
				return false ;
			}
		}
		
		
		ctx.invokeAction('profile.do-search', {
			'nickName' : nickName ,
			'firstName' : firstName ,
			'lastName' : lastName ,
			'gender' : gender ,
			'minAge' : minAge ,
			'maxAge' : maxAge ,
			'searchType' : searchType
		});
	});
	
	
	hike.registerAction('profile.do-search', function(dataset, ctx){
		ctx.openView('profile/search-results', dataset);
	});
	
	hike.registerAction('profile.levelrules', function(dataset, ctx) {
		ctx.openView('profile/levelrules', {});
	});
	hike.registerAction('profile.badgerules', function(dataset, ctx) {
		ctx.openView('profile/badgerules', {});
	});
	
	
	hike.registerAction('profile.friendsearch', function(dataset, ctx) {
		var dataview = ctx.getWidget("firendsview");
		var nickName = $("#nickName").val();
		$("#name").val(nickName);
		dataview['nickName'] = nickName;
		var userId= dataview["userId"] ;
		var getBtnStr = function(item){
			if("NoPath"===item.relation || "Applied"===item.relation){
				return '<div class="operation"><input type="button" data-widget="Button" data-action="friend.invite" data-user-id="' + item.userId + '" data-nick-name="' + item.nickName + '" data-my-name="' + item.myName + '" value="Add" /></div>';
			}
			return '<div class="operation"><input type="button" data-widget="Button" data-action="profile.friends" data-user-id="' + item.userId + '" value="View" /></div>';
		};
			 ctx.get('profile/friend/search', {
		            "page":dataview["curPage"],
		            "pagesize":dataview["pageSize"],
		        	"nickName":nickName,
					"userId":userId
		        },{
		            "0":function(data) {
		            	var boardStr ="";
		                if (data.friends != undefined && data.friends != null && data.friends.length > 0) {
						    var list = data.friends;
		                    var len = list.length;
		                    for (var i = 0; i < len; i++) {
		                        var item = list[i];
		                        boardStr += '<li data-widget="Button" data-action="profile.friends" data-user-id="'+item.userId+'"><div class="wrapper"><img src="' + item.headUrl
		                        	+ '" /><div class="info"><div class="name">' + item.nickName
		                        	+ '</div><div class="mutual">' + item.mutualFriendCount
		                        	+ ' mutual friends</div></div>'
		                        	+ getBtnStr(item)
		                        	+ '</div></li>';
		                    }
		                    dataview.reset(boardStr,list.length<dataview["pageSize"]?true:false);
		                }else{
		                	dataview.reset('',true);
		                }
		            }
		        });
		});
	
	hike.registerAction('profile.allfriends', function(dataset, ctx) {
		if(dataset['activeIndex'] === 2){
			$("#nickName").val('');
			ctx.invokeAction('profile.getFriends', ctx.getWidget("firendsview"));
		}
	});
	/*
	 * search tab 
	 * */
	hike.registerAction('profile.searchTab', function(dataset, ctx) {
		var tabValue = $(".searchchoosetype").val();
		if(dataset['fieldValue'] === tabValue){
			return;
		}else{
			if(tabValue == 1){
				$(".searchfriends").removeClass("checkedtab");
				$(".searchgames").addClass("checkedtab");
			}else if(tabValue == -1){
				$(".searchgames").removeClass("checkedtab");
				$(".searchfriends").addClass("checkedtab");				
			}
			$(".searchchoosetype").val(-1*tabValue);
		}
	});
	
	hike.registerAction('profile.toPostPage', function(dataset, ctx) {
		var defaultContent = "Add status here";
		var bodyContent = ctx.getItem("bodyContent");
		if(bodyContent!=null && bodyContent!="" && bodyContent != undefined){
			defaultContent = bodyContent;
		}
		var postValue = dataset["postValue"] ;
		
		if(postValue==null || postValue=="" || postValue == undefined){
			postValue ="" ; 
		}
		var params = {  "title" : "Post Status",
						"buttonName" : "Send",
						"defaultContent" : defaultContent,
						"contentVal" :  postValue,
						"requireSubject": false
					 };
		
		window.prompt('ContentEditor', hike.util.encodeJson(params));
		
		window.onEditCompleted = function(data){
			var contents = data.content; 
			if(contents != undefined){
				hike.log.info("contents = " +contents);
				
				var tempContents = contents.replace(/(^\s*)(\s*$)/g,'');
				if(tempContents.length==0){
					hike.ui.showError("Content cannot be empty.");
					return ; 
				}
				
				var url = "status/send";
				var patrn = new RegExp('\\@\\w+\\s', "g");
				var startValue = contents.match(patrn);
				var atlist = "";
				if(null != startValue && startValue.length > 0){
					for(var a=0;a<startValue.length;a++){
						atlist += startValue[a];
						if(a<startValue.length-1){
							atlist += "$_$";
						}
					}
				}
				var parame = {
						"contents" : contents,
						"atlist" : atlist
				};
				ctx.setItem("bodyContent",undefined);
				ctx.post(url,parame,{
					"0" : function(data){
						//successfully 
						hike.ui.showError("Send successfully!");
						var cache = ctx.getCache();
		            	hike.log.info('clear newsfeedCache begin');
		        		cache.clear("isAll");
		            	cache.clear("newsfeedCache");
		            	cache.clear("activitiesCache");
		            	cache.clear("activitiesIsAll");
		        		cache.clear("basicInfoCache");
		            	hike.log.info('clear newsfeedCache end');
		            	ctx.reload();
					},
					"1" : function(data) {
						hike.ui.showError("Send failed!");
			        },
					"451" : function(data) {
						hike.ui.showError("Status cannot be empty.");
			        },
					"454" : function(data) {
						hike.ui.showError("Status contains illegal characters!");
			        }
				},false);
			}
		}
	});
	
	/*hike.registerAction('profile.post', function(dataset, ctx) {
	var tempContents = $(".contents").val().replace(/(^\s*)(\s*$)/g,'');
	if(tempContents.length==0){
		hike.ui.showError("Content cannot be empty.");
		return ; 
	}
	
	var contents = $(".contents").val();
	var contentPatrn=/^[a-zA-Z0-9_,\s,\W]{1,140}$/;
	if (!contentPatrn.test(contents)) {
		if(contents.length>0){
			hike.ui.showError("Content contains illegal characters!");
		}else{
			hike.ui.showError("Content cannot be empty!");
		}
		return false ;
	}
	
	var url = "status/send";
	var patrn = new RegExp('\\@\\w+\\s', "g");
	var startValue = contents.match(patrn);
	var atlist = "";
	if(null!=startValue && startValue.length>0){
		for(var a=0;a<startValue.length;a++){
			atlist += startValue[a];
			if(a<startValue.length-1){
				atlist += "$_$";
			}
		}
	}
	var parame = {
			"contents" : contents,
			"atlist" : atlist
	};
	ctx.setItem("bodyContent",undefined);
	ctx.post(url,parame,{
		"0" : function(data){
			//successfully
			hike.ui.showError("Send successfully!");
			var cache = ctx.getCache();
        	hike.log.info('clear newsfeedCache begin');
    		cache.clear("isAll");
        	cache.clear("newsfeedCache");
        	cache.clear("activitiesCache");
    		cache.clear("activitiesIsAll");
        	hike.log.info('clear newsfeedCache end');
			setTimeout(function(){ hike.backward();},3000);
		},
		"1" : function(data) {
			hike.ui.showError("Send failed!");
        },
		"451" : function(data) {
			hike.ui.showError("Status cannot be empty.");
        },
		"454" : function(data) {
			hike.ui.showError("Status contains illegal characters!");
        }
	}, false);
});*/
	
	
	hike.registerAction('profile.onProfilePageOpened', function(){
		setTimeout(function(){
			if(hike.util.UserAgent.ios){
				$('span.change-avata').css({
					'display' : 'none'
				});
			}
		});
	});
	
	hike.registerAction('profile.contact', function(){
		hike.callNative('ActivityInvoker', 'startContactActivity')	
	});
	
})();
