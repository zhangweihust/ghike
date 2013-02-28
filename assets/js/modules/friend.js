(function(){
	

	hike.registerAction('friend.invite', function(dataset, ctx) {
		var userId = dataset["userId"];
		var MyName = dataset["myName"];
		var userName = dataset["nickName"];

		//hike.callNative("NativeUI", "requestDialog", 'Friend Request', 'Cancel', 'Send', true, 'Hello!', false); 

		hike.callNative("NativeUI", "requestDialog", {
			title : 'Friend Request',
			editable : true,
			content : 'Hello!',
			confirmButton : 'Send',
			cancelButton : 'Cancel'
		}); 
		
		window.onDialogConfirm  = function(message){
			hike.log.info("UtilDialog message = " + message);
				var contentText = message;
				var url = "friend/" + userId + "/add";
				hike.ui.closeDialog();
				ctx.get(url, {content:contentText}, {
					
					"0": function(data) {
						$(".invitefriend").html("<span class=\"disablebtn\" data-widget=\"Button\" >Request Sent</span>");
					   //ctx.reload();
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
		};
		window.onDialogCancel = function(){};
	});
	
	
	hike.registerAction('friend.remove', function(dataset, ctx) {
		var userId = dataset["userId"];
		//alert(MyName+"---"+userName) ; 
		var config = {
				title : 'Remove this friend?',
				buttons:[{text:'OK',fn:'friend.removefriend'},{text:'Cancel'}],
				closable : true
			};
		hike.ui.showDialog(config);
	
		hike.registerAction('friend.removefriend', function(dataset, ctx){
			var url = "friend/" + userId + "/remove";
			hike.ui.closeDialog();
			ctx.get(url, null, function(data) {
				ctx.forward('profile.my-friends',{
					'userId':userId
				});
			});

		});
		
	});
	hike.registerAction('friend.add', function(dataset, ctx) {
		var userId = dataset["userId"];
		var MyName = dataset["myName"];
		var userName = dataset["nickName"];
		userName = userName.replace("<b>","").replace("</b>","");

		//hike.callNative("NativeUI", "requestDialog", 'Friend Request', 'Cancel', 'Send', true, 'Hello!', false); 
		hike.callNative("NativeUI", "requestDialog", {
			title : 'Friend Request',
			editable : true,
			content : 'Hello!',
			confirmButton : 'Send',
			cancelButton : 'Cancel'
		}); 
		window.onDialogConfirm  = function(message){
			hike.log.info("UtilDialog message = " + message);
				var contentText = message;
				var url = "friend/" + userId + "/add";
				hike.ui.closeDialog();
				ctx.get(url, {content:contentText}, {
					
					"0": function(data) {
					  $("#add"+userId).hide();
					  $("#rsend"+userId).show();
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
						hike.ui.showError("Invite request has been sent.");
						return;
					}
				})
		};
		window.onDialogCancel = function(){};
	});

	
	hike.registerAction('friend.pelple', function(dataset, ctx) {
			ctx.openView('profile/peopleyoumayknow', null);
	});
	
	
	
	hike.registerAction('friend.mutualfriends', function(dataset, ctx) {
		var userId = dataset["userId"];
		var page   = dataset.page ;
		var pagesize   = dataset.pagesize ;
		var url = "user/" + userId + "/mutualfriends";
		ctx.get(url, {page:page,pagesize:pagesize,nickName:searchNickName}, function(data) {
			ctx.openView('user/mutualfriends', data);
		})
	});

	
	hike.registerAction('friend.peopleyouMayknow', function(dataView, ctx){
		var isMyProfile = dataView["userType"];
		var cache = ctx.getCache();
		var recommendCache = cache.get("recommendCache");
		var url = "user/friend/people";
		if(isMyProfile && null!=recommendCache && dataView["curPage"]===1){
			hike.log.info('used recommendCache');
			var friendsListHtml = "";
			
			var introductionFriendList = recommendCache.introductionFriendList;
			var recommendTypeMap = recommendCache.recommendType;
			var mutualFriends = recommendCache.mutualFriends;
			var mutualGames = recommendCache.mutualGames;
			
			var hostNickName = recommendCache.hostNickName ;
			$.each(introductionFriendList,function(entryIndex,entry){  
                var recommendType = recommendTypeMap[entry['id']];
                var mutualFriend = mutualFriends[entry['id']];
                var mutualGame = mutualGames[entry['id']];
                var  addmsg ="add"+entry['id'] ;
                var  rsendmsg ="rsend"+entry['id'] ;
                var detailContent = "";
                if(recommendType==1){
                	detailContent = mutualFriend+" mutual friends";
                }else if(recommendType==2){
                	detailContent = "Playing "+mutualGame;
                }else{
                	detailContent = "Online now" ; 
                }
        		friendsListHtml += "<li data-widget=\"Button\" data-action=\"profile.friends\" data-user-id=\""+entry['id']+"\" id=\""+entry['id']+"\">"+
				"<div class=\"wrapper\">"+
					"<img alt=\""+entry['nickName']+"\" data-stranger-id=\""+entry['id']+"\" src=\""+entry['headUrl']+"\"/>"+
					"<div class=\"info\">"+
						"<div class=\"name\" >"+entry['nickName']+"</div>"+
						"<div class=\"mutual\" data-stranger-id=\""+entry['id']+"\">"+detailContent+"</div>"+
					"</div>"+
				"</div>"+
				"<div class=\"btninvite\" id=\""+addmsg+"\" data-action=\"friend.add\" data-user-id=\"" + entry['id']+ "\" data-nick-name =\""+entry['nickName']+"\" data-my-name  =\""+hostNickName+"\" >"+
				 	"<span class='bluebtn  invite'>Add</span>"+
				"</div>"+
				"<div class=\"btninvite\" style =\"display:none\" id=\""+rsendmsg+"\">"+
			      	"<span class=\"disablebtn\">Request sent </span >"+
			    "</div>"+
				"</li>";
               
            });  
		    
			dataView.append(friendsListHtml,recommendCache.isAll);
		}else{	
			dataView.preload();
			hike.log.info('go recommend server');
				ctx.get(url, {
		            "page":dataView["curPage"],
		            "pagesize":dataView["pageSize"]
		        },{"0":function(data){
						var friendsListHtml = "";
					
						var introductionFriendList = data.introductionFriendList;
						var recommendTypeMap = data.recommendType;
						var mutualFriends = data.mutualFriends;
						var mutualGames = data.mutualGames;
						
						var hostNickName = data.hostNickName ;
						
						$.each(introductionFriendList,function(entryIndex,entry){  
		                    var recommendType = recommendTypeMap[entry['id']];
		                    var mutualFriend = mutualFriends[entry['id']];
		                    var mutualGame = mutualGames[entry['id']];
		                    
		                    var  addmsg ="add"+entry['id'] ;
		                    var  rsendmsg ="rsend"+entry['id'] ;
		                   
		                    var detailContent = "";
		                    if(recommendType==1){
		                    	detailContent = mutualFriend+" mutual friends";
		                    }else if(recommendType==2){
		                    	detailContent = "Playing "+mutualGame;
		                    }else{
		                    	detailContent = "Online now" ; 
		                    }
		                    
			   				friendsListHtml += "<li data-widget=\"Button\" data-action=\"profile.friends\" data-user-id=\""+entry['id']+"\" id=\""+entry['id']+"\">"+
													"<div class=\"wrapper\">"+
														"<img alt=\""+entry['nickName']+"\" data-stranger-id=\""+entry['id']+"\" src=\""+entry['headUrl']+"\"/>"+
														"<div class=\"info\">"+
															"<div class=\"name\" >"+entry['nickName']+"</div>"+
															"<div class=\"mutual\" data-stranger-id=\""+entry['id']+"\">"+detailContent+"</div>"+
														"</div>"+
													"</div>"+
													"<div class=\"btninvite\" id=\""+addmsg+"\" data-action=\"friend.add\" data-user-id=\"" + entry['id']+ "\" data-nick-name =\""+entry['nickName']+"\" data-my-name  =\""+hostNickName+"\" >"+
													 	"<span class='bluebtn  invite'>Add</span>"+
													"</div>"+
													"<div class=\"btninvite\" style =\"display:none\" id=\""+rsendmsg+"\">"+
												      	"<span class=\"disablebtn\">Request sent </span >"+
												    "</div>"+
												"</li>";
			   			 
		                });  
						if(dataView['curPage']===1 || null == recommendCache){
							cache.persist("recommendCache",data);
							hike.log.info("persist recommend cache end");
						}
						dataView.append(friendsListHtml,data.isAll);
					},
					"1":function(data){
						dataView.append("",true);
					}
				}, true);
			}
	});
		
		
		

})();
