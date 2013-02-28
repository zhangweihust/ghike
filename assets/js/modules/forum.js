(function(){
	
	 /*forum-home*/
    hike.registerAction('forum.home', function(dataset, ctx){
    	ctx.get('community/home', {}, {
			"0": function(data) {
				ctx.openView('forum/forum-home',data);
			}
		})
    });
    /*forum*/
    hike.registerAction('forum.toforum', function(dataset, ctx){
        var forumId = dataset["forumId"];
    	var detailUrl="community/forum/toforum";
        ctx.get(detailUrl,{"forumId":forumId},function(data){
        	ctx.openView("forum/forum-certain",data);
        });
	});
        
    /*forum-topicdetail*/
    hike.registerAction('forum.topicdetail', function(dataView, ctx){
        var url = "community/forum/showTopicList";
        var forumId = dataView["forumId"];
        dataView.preload();
        ctx.get(url, {
        	"forumId":forumId,
            "page":dataView["curPage"],
            "pageSize":dataView["pageSize"],
            'ts':new Date().getTime()
        }, {
            "0":function(data) {
            	if (data.topicList != null && data.topicList.length > 0) {
                    var len = data.topicList.length;
                    var boardStr = "";
                    for (var i = 0; i < len; i++) {
                        var topic = data.topicList[i];
                        var user = data.userList[i];
                        var topicListreplyTime =  data.topicListreplyTime[i];
                        boardStr +="<li data-action='topic.discusstion' data-topic-id='"+topic.topicId+"'>"+
        				"<aside class='person'>"+
        				"<img src='"+user.headUrl+"' alt='"+user.nickName+"' />"+
        					"<span class='name'>" + user.nickName + "</span>"+
        				"</aside>"+
        				"<div class='content' data-widget='TimelineContent'>"+ topic.subject +
        				"</div>"+
        				"<div class='details'><span class='discuss'>"+ topic.replyCount +"</span>"+
        				"<span class='duration'>"+ topicListreplyTime +"</span></div>"+
        			"</li>";                      
                        
                    }
                    dataView.append(boardStr,data.isHasNextPage);
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
    
    hike.registerAction('forum.subscribe', function(dataset, ctx){
    	var url = "community/forum/subscribe_forum";
        var forumId = dataset["forumId"];
        ctx.get(url,{"forumId":forumId},function(data){
        	$("#subscribe").val("Subscribed");
            $("#subscribe").data('action','');
        	//var config = {content:'You have subscribed to this forum!',buttons: [{text:''},{text:'Cancel'}]};
        	//hike.ui.showDialog(config);
        });
	});
    hike.registerAction('forum.deletepop', function(dataset, ctx){
		var config = {title:'Sure to delete ?',buttons: [{text:'Delete'},{text:'Cancel'}]};
		hike.ui.showDialog(config);
	});
    
  ////
    /*forum-all*/
    hike.registerAction('forum.allforumlist', function(dataset, ctx){
    	ctx.openView('forum/forum-all',{});
    });
    /*forum-mypost*/
    hike.registerAction('forum.mypost', function(dataset, ctx){
    	var  myforumButton = ctx.getWidget("myforumButton");
    	var  mypostButton = ctx.getWidget("mypostButton");
    	//   modify  myforum's  class not be  "active" 
    	$(myforumButton.element).removeClass("active");
    	$(mypostButton.element).addClass("active");
    	var  myforum=ctx.getWidget("myforum");
    	var  mypost = ctx.getWidget("mypost");
    	$(myforum.element).hide()
    	$(mypost.element).show()
    	//  display  myforum's  selection 
    });
    /*forum.mypostredirect*/
    hike.registerAction('forum.mypostredirect', function(dataset, ctx){
    	var id = dataset["id"];
    	var type = dataset["type"];
    	var url="community/jump-post";
    	ctx.get(url, {"id":id,"type":type}, {
			"0": function(data) {
				ctx.forward("topic.discusstion",{'topicId':data.topicId});
			},
			"903": function(data) {
				ctx.openView('forum/topic-blank',{});
			}
		})
	});
    hike.registerAction('forum.mypostsdel', function(dataset, ctx){
    	var id = dataset["id"];
    	var type = dataset["type"];
    	if(type==1){
    		ctx.forward("topic.deltopic",{'topicId':id,'topicType':2});
    	}else{
    		ctx.forward("topic.delreply",{'replyId':id,'replyType':2});
    	}
    	
	});
    /*forum-myforums*/
    hike.registerAction('forum.myforums', function(dataset, ctx){
    	ctx.openView('forum/forum-mypost',{});
    });
    
    /*forum-myforums*/
    hike.registerAction('forum.myforum', function(dataset, ctx){
    	var  myforumButton = ctx.getWidget("myforumButton");
    	var  mypostButton = ctx.getWidget("mypostButton");
    	//   modify  myforum's  class not be  "active" 
    	$(myforumButton.element).addClass("active");
    	$(mypostButton.element).removeClass("active");
    	var  myforum=ctx.getWidget("myforum");
    	var  mypost = ctx.getWidget("mypost");
    	$(myforum.element).show()
    	$(mypost.element).hide()
    	
    });
    
    hike.registerAction('forum.myforumpop', function(dataset, ctx){
    	var forumId = dataset["forumId"];
    	var config = {title:'Sure to unsubscribe ?',buttons: [{text:'Unsubscribe',fn:'forum.myforumunsubscribe'},{text:'Cancel'}]};
		hike.ui.showDialog(config);
	    hike.registerAction('forum.myforumunsubscribe', function(dataset, ctx){
			var url = "community/forum/un_subscribe_forum";
			hike.ui.closeDialog();
			ctx.get(url, {"forumId":forumId}, {
				"0": function(data) {
					ctx.forward('forum.myforums');
				}
			})
		});
    });
    
    hike.registerAction('forum.userforums', function(dataView, ctx){
    	var userId = dataView["userId"]
        var url = "community/forum/"+userId;
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
                        var topicCount = data.topicCountMap[item.forumId];	
						var appBasic = data.appBasicMap[item.forumId];
						var imgurl = appBasic['appIconCover'] == null ? '' : appBasic['appIconCover'].touchIcon;
                        str += "<li data-forum-id ='"+item.forumId+ "' data-action='forum.toforum' data-widget='Button'>"
                        	+  "<img class='avata' src='" + hike.domain.URL_STATIC_IMG + "/"+imgurl+"' alt='"+appBasic.title+"' />"
				            +  "<div class='info'><span class='name'>"+appBasic.title+"</span><span class='cnt'>Topic: "+topicCount+"</span></div>"
				            +  "<input type='button' value='Unsubscribe' data-widget='Button' data-transition='slide'"
				            +  "data-action='forum.myforumpop' data-forum-id='"+item.forumId+"' data-board-type='specific-leaderboard' />"
			                +  "</li>";
                    }
                    dataView.append(str,data.hasNextPage);
                    ctx.updateView();
                } else{
                  	var list = data.featuredGame;
                    var len = list.length;
                    var boardStr = "";
                    for (var i = 0; i < len; i++) {
                        var item = list[i];
                        boardStr += '<li data-widget="Button" data-action="games.play"  data-game-id = "'+item.appId+'" ><div class="wrapper">'
                        	+ '<img src="' + hike.domain.URL_STATIC_IMG + '/' + item.appIconCover.touchIcon + '" />'
                        	+ '<div class="info"><span class="name">' + item.title + '</span></div>'
                        	+ '</div></li>';
                    }
                    boardStr = '<div class="itemtitle">Featured Game</div>'+boardStr
                    dataView.append(boardStr,data.hasNextPage);
                }
            },
            "103":function() {
                $('.error').html('User does not exist!');
				$('.error').show();
            }
        }, true);
    });
    
    
    /*forum-allforums-data*/
    hike.registerAction('forum.allforumsscroll', function(dataView, ctx){
        var url = "community/forum";
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
                        var topicCount = data.topicCountMap[item];	
						var appBasic = data.appBasicMap[item];
						var imgurl = appBasic['appIconCover'] == null ? '' : appBasic['appIconCover'].touchIcon;
                        str += "<li data-forum-id ='"+item+ "' data-action='forum.toforum'>"
				            + "<img class='avata' src='" + hike.domain.URL_STATIC_IMG + "/"+imgurl+"' alt='"+appBasic.title+"' data-forum-id ='"+item+ "' data-action='forum.toforum' />"
				            + "<div class='info'><span class='name'>"+appBasic.title+"</span>"
				            + "<span class='cnt'>Topic: "+topicCount+"</span></div>"
			                + "</li>";
                    }
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
    /*forum-myforums-data*/
    hike.registerAction('forum.myforumsscroll', function(dataView, ctx){
        var url = "community/my-forum";
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
                        var topicCount = data.topicCountMap[item];	
						var appBasic = data.appBasicMap[item];
						var imgurl = appBasic['appIconCover'] == null ? '' : appBasic['appIconCover'].touchIcon;
                        str += "<li data-forum-id ='"+item+ "' data-action='forum.toforum' data-widget='Button'>"
                        	+ "<img class='avata' src='" + hike.domain.URL_STATIC_IMG + "/"+imgurl+"' alt='"+appBasic.title+"' />"
				            + "<div class='info'><span class='name'>"+appBasic.title+"</span><span class='cnt'>Topic: "+topicCount+"</span></div>"
				            + "<input type='button' value='Unsubscribe' data-widget='Button' data-transition='slide'"
				            + "data-action='forum.myforumpop' data-forum-id='"+item+"' data-board-type='specific-leaderboard' />"
			                + "</li>";
                    }
                    dataView.append(str,data.isHasNextPage);
                    ctx.updateView();
                } else{
                	dataView.append('<li>No record.</li>',true);
                }
            },
            "103":function() {
                $('.error').html('User does not exist!');
				$('.error').show();
            }
        }, true);
    });
    /*forum-allposts-data*/
    hike.registerAction('forum.mypostsscroll', function(dataView, ctx){
        var url = "community/my-post";
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
                        var info = data.list[i];
                        var numshow = "";
                        if(info.type == 1){
                        	numshow =info.count;
                        }
                        str += "<li data-action='forum.mypostredirect' data-id='"+info.id+"' data-type='"+info.type+"'>"
                        	+"<img class='avata' src='" + hike.domain.URL_STATIC_IMG + "/"+info.touchIcon+"' alt='"+info.appName+"'/>"
				            +"<div class='detail'  data-widget='TimelineContent' ><span class='name'>"+info.appName+"</span> : "+info.message+"</div>"
				            + "<div class='legend'><span class='duration'>"+info.createTime+"</span>"
				            +"<span class='delete' data-action='forum.mypostsdel' data-id='"+info.id+"' data-type='"+info.type+"'>Delete</span>"
				            +"<span class='comments'>"+numshow+"</span></div>"
				            +"<div class='clear'></div>"
				            + "</li>";
                    }
                    dataView.append(str,data.isHasNextPage);
                    ctx.updateView();
                } else{
                	dataView.append('<li>No record.</li>',true);
                }
            },
            "103":function() {
                $('.error').html('User does not exist!');
				$('.error').show();
            }
        }, true);
    });
    //////////
	
})();
