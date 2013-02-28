(function(){
	hike.registerAction('topic.topiclogin', function(dataset, ctx) {
		var params = {  "title" : "New Topic",
						"buttonName" : "Post",
						"defaultContent" : "",
						"gameTitle" : dataset['gameTitle'],
						"requireSubject": false};
		 window.prompt('ContentEditor', hike.util.encodeJson(params));
			
		 window.onEditCompleted = function(data){
			var subject = data.subject;
			var content = data.content;
			if(content!=undefined){
				if(subject.length==0){
					hike.ui.showError('Subject can not be empty!');
					return ; 
				}
				if(content.length==0){
					hike.ui.showError('Content can not be empty!');
					return ; 
				}
				var url = "community/topic/add";
				var forumId = dataset['forumId'];
				var gameId = dataset['gameId'];
				ctx.post(url, {
					"forumId":forumId,
					"subject":subject,
					"content":content
						},{
				   "0": function(data) {
					   hike.ui.showError('Send successfully!');
					   ctx.reload();
				    },
					"901" : function() {
						hike.ui.showError('Subject is too long !');
					},
					"902" : function() {
						hike.ui.showError('Content is too long !');
					}
				},false);
			 }
		 }
	});
	
	hike.registerAction('topic.discusstion', function(dataset, ctx) {
		var topicId = dataset['topicId'];
		var url = "community/topic/" + topicId +"/discusstion";
		ctx.get(url, null,{
		    "0": function(data) {
				ctx.openView('forum/forum-topicdetail', data);
			},
			"903" : function() {
				$('.error').html('topic is not exists.');
				$('.error').show();
			}
		})
	});	
		

	hike.registerAction('topic.replylist', function(dataView, ctx){
		var topicId = dataView['topicId'];
		var url = "community/topic/" + topicId +"/reply";
		var page   =  dataView["curPage"];
		var pagesize   = dataView["pageSize"] ;
		dataView.preload();
		
		ctx.get(url, {
	      "page":page,
	      "pagesize":pagesize
		},{
        "0":function(data) {
        	 var boardStr ="";
        	 if (data.replyList != undefined && data.replyList != null && data.replyList.length > 0) {
				 var replyList = data.replyList;
				 var replyUser = data.replyuser;
                 var len = replyList.length;
                 var topic = data.topic ; 
                 var topicuser =data.topicuser ;
                 var user   = data.user ; 
                 for (var i = 0; i < len; i++) {
                	 var itemReply = replyList[i];
                     var itemUser = replyUser[i];
                     var userid = itemUser.id;
                     var strhtml ="" ; 
                     var strReplyTo ="" ; 
                     if(userid==user.id){
                     	strhtml = '<span class="delete" data-reply-type="1" data-topic-id ="'+topic.topicId+'"  data-reply-id="'+itemReply.replyId+'" data-action="topic.delreply">delete</span>' ; 
                     }else{
                     	strhtml = "" ;
                     } 
                     var replyTo = itemReply.replyTo ; 
                     if(replyTo===null||replyTo==0){
                    	 strReplyTo = ":"
                     }
                     boardStr += '<li>'
                    	+'<div class="content">'
                      	+'<img class="avata" src="' + itemUser.headUrl+ '" data-action ="profile.friends" data-user-id="' + itemUser.id+ '" />'
                      	+'<div class="detail" data-widget="TimelineContent"> <span class="name">' + itemUser.nickName +strReplyTo+'</span>'
                      	+ itemReply.contentStyle
                      	+ '</div>'
                      	+ '<div class="legend">'
                      	+ '<aside class="time">'+ itemReply.createTimereckon+'</aside>'
						+ '<aside class="comment" data-topic-id="'+topic.topicId+'" data-reply-to="'+itemUser.id+'" data-reply-name="'+ itemUser.nickName+'"  data-action="topic.replylink"></aside>'
                        + '</div>'
                        + '</div>'
                      	+ '</li>' ;
                 }
                 dataView.append(boardStr,data.isHasNextPage);
                 ctx.updateView();
             }else{
             	dataView.append(boardStr,true);
             }
        }
    }, true);
	});	
	
	
	hike.registerAction('topic.deltopic', function(dataset, ctx) {
		var forumId = dataset['forumId'];
		var topicId = dataset['topicId'];
		var topicType = dataset['topicType'];
		var config = {
				title : 'Sure to delete?',
				buttons:[{text:'Delete',fn:'topic.delete'},{text:'Cancel'}],
				closable : true,
	  			content:''
			};
		hike.ui.showDialog(config);
		
	hike.registerAction('topic.delete', function(dataset, ctx){	
		var url = "community/topic/" + topicId +"/delete";
		hike.ui.closeDialog();
		ctx.post(url, null,{
			"0":function(data) {
				if(topicType==1){
					ctx.forward('topic.back')
				}else if(topicType==2){
					ctx.forward('forum.mypost');
				}else{
					ctx.forward('topic.back')
				}
			},
			"903" : function() {
				$('.error').html('topic is not exists.');
				$('.error').show();
			},
			"904" : function() {
				$('.error').html('The topic is not you created! ');
				$('.error').show();
			}
			
			
		})
	});	
	
	}); 
	 
	
	hike.registerAction('topic.back', function(dataset, ctx){
		 hike.backward();
	});
	  
	
	hike.registerAction('topic.replylink', function(dataset, ctx) {
		var topicId= dataset['topicId'];
		var replyTo= dataset['replyTo'];
		var replyName=dataset['replyName'];
		var topicName=dataset['topicName'];
		var viewName ="" ; 
		if(replyTo.length>0){
			viewName  ="Reply to  "+replyName+" :"	
			replyName =" to "+replyName+" :"	
		}else{
			viewName  ="Reply  to  "+topicName+" :"		
		}
		
		var params = {  "title" : "Reply",
						"buttonName" : "Send",
						"defaultContent" : viewName,
						"requireSubject": false};
		window.prompt('ContentEditor', hike.util.encodeJson(params));
		
		window.onEditCompleted = function(data){
			var content = data.content;
			if(content!=undefined){
				var contentTemp = content.replace(/(^\s*)(\s*$)/g,'');
				if(contentTemp.length==0){
					hike.ui.showError('Content can not be empty!');
					return ; 
				}
				var url = "community/reply/add";
				var content = replyName + content; 
				ctx.post(url, {
					"topicId":topicId,
					"replyTo":replyTo,
					"content":content,
					"contentTemp":contentTemp
				},{
					   "0": function(data) {
						   hike.ui.showError('Send successfully!');
						   ctx.reload();
						//   setTimeout(function(){ctx.forward('topic.back');},3000);
					    },
						"901" : function() {
							hike.ui.showError('Subject is too long !');
						},
						"902" : function() {
							hike.ui.showError('Content is too long !');
						}
				},false);
			}
		}
	});	
	
	
//	hike.registerAction('topic.reply', function(dataset, ctx) {
//		var topicId = dataset['topicId'];
//		var replyName = dataset['replyName'];
//		var replyTo = $(".tosomeone").val();  
//		var contentTemp = $(".replycontent").val().replace(/(^\s*)(\s*$)/g,'');
//		var content = replyName + $(".replycontent").val(); 
//		if(contentTemp.length==0){
//			hike.ui.showError('Content can not be empty!');
//			return ; 
//		}
//		var url = "community/reply/add";
//		ctx.post(url, {
//			"topicId":topicId,
//			"replyTo":replyTo,
//			"content":content,
//			"contentTemp":contentTemp
//		},{
//			   "0": function(data) {
//				   hike.ui.showError('Sent successfully!');
//				   setTimeout(function(){ctx.forward('topic.back');},3000);
//			    },
//				"901" : function() {
//					hike.ui.showError('Subject is too long !');
//				},
//				"902" : function() {
//					hike.ui.showError('Content is too long !');
//				}
//		},false);
//	});	
	
	hike.registerAction('topic.delreply', function(dataset, ctx) {
		var topicId = dataset['topicId'];
		var replyId = dataset['replyId'];
		var replyType = dataset['replyType'];
		var config = {
				title : 'Sure to delete?',
				buttons:[{text:'Delete',fn:'reply.delete'},{text:'Cancel'}],
				closable : true,
	  			content:''
			};
	hike.ui.showDialog(config);
	
   hike.registerAction('reply.delete', function(dataset, ctx){	
		var url = "community/reply/" + replyId +"/delete";
		hike.ui.closeDialog();
		ctx.post(url, null,{
			"0":function(data) {
				if(replyType==1){
					var replylist =ctx.getWidget("replylist");
					$(replylist.element).find("li[data-reply-id='"+replyId+"']").remove();
					hike.ui.showError("Delete successfully.");
					
					var  countNum = $(".replycount").data('replycount-id');
					countNum = countNum -1 ;				
					var html = '<span class="cnt" data-replycount-id="'+countNum+'" >Reply: '+countNum+'</span>';
					$(".replycount").html("");
					$(".replycount").html(html);
					
					//ctx.forward('topic.back');
				}else if(replyType==2){
					ctx.forward('forum.mypost');
				}else{
					ctx.forward('topic.back');
				}
			}
		})
	});	
   
	}); 
	var focusStart=function(obj){
		if (obj.createTextRange) {
	        var range = obj.createTextRange();
	        range.collapse(true);
	        range.moveEnd('character', 0);
	        range.moveStart('character', 0);
	        range.select();
	    } else if (obj.setSelectionRange) {
	    	obj.focus();
	    	obj.setSelectionRange(0, 0);
	    }
	};
	hike.registerAction('topic.topicreply', function(dataset, ctx){
		var element = arguments[0].element;
		setTimeout(function(){
			   var subject = $(element).find('.topicsubject');
			   var content = $(element).find(".contents");
			   var countwdiget = $(element).find(".count");
			   var count = $(element).find(".count .maxnum");
			   var countNum = $(element).find(".count .num");
			   var maxlength = content.attr("maxlength");
			   if(subject.length ===1 && content.length ===1){
				   count.html(subject.attr("maxlength"));
			   };			   
			   var countSubjectNum = function(){
				   countwdiget.show();
				   count.html(subject.attr("maxlength"));
				   
				   countNum.html(subject.val().length);
			   };			   
			   var countContentNum = function(){
				   var val = content.val();
				   
				   if(val.length < maxlength){
					   content.removeAttr("maxlength"); 
				   }else{
					   content.attr("maxlength",maxlength); 
				   }
				   countwdiget.show();
				   count.html(maxlength);
				   if(val.length > maxlength) {
					   content.val(content.val().substr(0 ,maxlength));
				   }				   
				   countNum.html(content.val().length);
			   };
			   
			   var CountSubevt = document.createEvent("UIEvents");
			   CountSubevt.initEvent("countSubjectEvents", true, true);
			   subject.bind("countSubjectEvents",countSubjectNum);
			   
			   var CountContevt = document.createEvent("UIEvents");
			   CountContevt.initEvent("countContentEvents", true, true);
			   content.bind("countContentEvents",countContentNum);
			   
			   ctx.setItem("CountContevt",CountContevt);
			   ctx.setItem("CountSubevt",CountSubevt);
			   
			   subject.bind('keydown keyup focus input propertychange',function(){				   
				   $(this).trigger(CountSubevt);
			   });
			   content.bind('keydown keyup focus input propertychange',function(){
				   $(this).trigger(CountContevt);
			   });
		},1);
	});
	
})();
