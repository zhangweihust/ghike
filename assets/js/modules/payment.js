var secs = 120;
var t=null;

(function(){
	hike.registerAction('payment.paymentCenter', function(dataset, ctx){
		//ctx.setItem('paymentType','forSNS');
		var cache = ctx.getCache();
		var paymentCache = cache.get("paymentCache");
		var detailUrl="payment:recharge";    
		if(null != paymentCache){
			hike.log.info("used paymentCache");
			ctx.openView('payment/payment', paymentCache);
			ctx.get(detailUrl,null,{
                "200":function(data){
                    $(".tokenInput").data("jsp_token",data.jsp_token);
                    hike.log.info("update paymentCache jsp_token="+data.jsp_token);
                    hike.log.info("update paymentCache jsp_token="+$(".tokenInput").data.jsp_token);
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
				hike.log.info("get new payment data");
		     ctx.get(detailUrl,null,{
	                "200":function(data){
	                	data.rechargeType = 'SNS';
	                	ctx.openView('payment/payment', data);
	                	cache.persist("paymentCache", data);
	                },
	                "202":function(data){
	                }
	            });
		}
	});	
	
	hike.registerAction('payment.paymentGameCenter', function(dataset, ctx){
		ctx.setItem('paymentType','forGame');
		var cache = ctx.getCache();
		var paymentGameCache = cache.get("paymentGameCache");
		
		var isNative = dataset.isNative;
		var transactionId = dataset.transactionId;
		var returnUrl=dataset.returnUrl; 
		var detailUrl="payment:recharge";    
		
		if(null != paymentGameCache){
			 ctx.openView('payment/payment', paymentGameCache);
			
			 ctx.get(detailUrl,{
		        	"isNative":isNative,
		       		"transactionId":transactionId,
		       		"returnUrl":returnUrl   
		           },{
		               "200":function(data){
			            	data.rechargeType = 'Game';
			            	cache.persist("paymentGameCache", data);
		                	
		                	if(data.user.nickName != paymentGameCache.user.nickName && paymentGameCache.user.nickName != null && undefined != $(".name").text()){
		                		$(".name").text(data.user.nickName);
		                	}
		                	if(data.ac.hikeCoin != paymentGameCache.ac.hikeCoin && paymentGameCache.ac.hikeCoin != null && undefined != $(".level").text()){
		                		var coinDes = data.ac.hikeCoin > 1? "Coins" : "Coin";
		                		$(".level").text(data.ac.hikeCoin + " " + coinDes);
							}
		               },
		               "202":function(data){
		               	
		               }
		      });
		}else{
			 ctx.get(detailUrl,{
		        	"isNative":isNative,
		       		"transactionId":transactionId,
		       		"returnUrl":returnUrl   
		           },{
		               "200":function(data)
		               {
		            	data.rechargeType = 'Game';
		               	ctx.openView('payment/payment', data);
		               	cache.persist("paymentGameCache", data);
		               },
		               "202":function(data){
		               	
		               }
		     });
		}
	});
	
	hike.registerAction('payment.rechargerecord', function(dataView, ctx){
		var url = "payment:recharge/record/";
		
		 dataView.preload();  
	        ctx.get(url, {
	            "curPage":dataView["curPage"],
	            "pSize":8
	        },{
	        	"200":function(data) {
	                if (data.lstRecord != null && data.total > 0) {
	                    var list = data.lstRecord;
	                    var len = list.length;
	                    
	                    var boardStr = "";
	                    for (var i = 0; i < len; i++) {
	                        var pay = list[i];
	                        var coinUnit = "";
	                        var rupeeUnit  = "";
	                        if(pay.amount == 1){
	                        	coinUnit = "Coin";
	                        	rupeeUnit = "Rupee";
	                        }
	                        else{
	                        	coinUnit = "Coins";
	                        	rupeeUnit = "Rupees";
	                        }
	                        
	                        if(pay.status == 'actived'){
	                        boardStr += '<li><dl><dt>Order number: <em class="info">'
	                        	+  pay.payBillId + '</em></dt><dt>Amount: <em class="hint">' + pay.hikeCoin + ' ' + coinUnit + '</em></dt><dd>Actual Pay: <em class="info">'
	                        	+  pay.amount + ' ' +rupeeUnit+'</em></dd><dd><time date-time="2012-7-14T20:08">'
	                            + pay.updateTimeStrEn + '</time><div class="bk0"></div></dd></dl></li>';
	                        }
	                        else{
	                        	 boardStr += '<li><dl><dt>Order number: <em class="info">'
	 	                        	+  pay.payBillId + '</em></dt><dt>Amount: <em class="hint">' + pay.hikeCoin + ' ' + coinUnit + '</em></dt><dd>Actual Pay: <em class="info">Failed</em></dd><dd><time date-time="2012-7-14T20:08">'
	 	                            + pay.updateTimeStrEn + '</time><div class="bk0"></div></dd></dl></li>';
	                        }
	                    }
						
	                    dataView.append(boardStr,data.hasNextPage);
	                    
	                }else{
	                	dataView.setHint("No record yet.");
	                	//dataView.append("No record yet",true);
	                }
	            }
	          },true);
	},true);
	
	hike.registerAction('recharge.record', function(dataView, ctx){
		var url = "payment:recharge/record/";
		
		 dataView.preload();  
	        ctx.get(url, {
	            "curPage":dataView["curPage"],
	            "pSize":2
	        },{
	        	"200":function(data) {
	                if (data.lstRecord != null && data.total > 0) {
	                    var list = data.lstRecord;
	                    var len = list.length;
	                    
	                    var boardStr = "";
	                    for (var i = 0; i < len; i++) {
	                        var pay = list[i];
	                        boardStr += '<li><span>'
	                        	+  pay.amount + ' Rs</span><span>' + pay.updateTimeStrEn + '</span>'
	                        	+ '<span>' + pay.payBillId + '</span></li>';
	                    }
						
	                    dataView.append(boardStr,true);
	                    
	                }else{
	                	dataView.setHint("No record yet.");
	                	//dataView.append("No record yet",true);
	                }
	            }
	          });
	},true);
	
	
	hike.registerAction('payment.consumerecord', function(dataView, ctx){
		var url = "payment:consume/record";
		
		 dataView.preload();  
	        ctx.get(url, {
	            "curPage":dataView["curPage"],
	            "pSize":8
	        },{
	            "200":function(data) {
	                if (data.clist != null && data.total > 0) {
	                    var list = data.clist;
	                    var len = list.length;
	                    var cboardStr = "";
	                    for (var i = 0; i < len; i++) {
	                        var consume = list[i];
	                        var coinUnit = "";
	                        if(consume.amount == 1){
	                        	coinUnit = "Coin";
	                        }
	                        else{
	                        	coinUnit = "Coins";
	                        }
	                        cboardStr += '<li><dl><dt>Game name: <em class="info">'
	                        	+ consume.appName + '</em></dt><dt>Item: <em class="info">' + consume.coinName + '</em></dt><dd>Coin Cost: <em class="hint">'
	                        	+ consume.amount + ' ' + coinUnit +'</em></dd><dd><time>'+consume.updateTimeStrEn+'</time><div class="bk0"></div></dd></dl></li>';
	                    }
	                    dataView.append(cboardStr,data.hasNextPage);
	                }else{
	                	dataView.setHint("No record yet.");
	                	//dataView.append("No record yet",true);
	                }
	            }
	          },true);
	},true);
	

	hike.registerAction('consume.record', function(dataView, ctx){
		var url = "payment:consume/record";
		
		 dataView.preload();  
	        ctx.get(url, {
	            "curPage":dataView["curPage"],
	            "pSize":2
	        },{
	            "200":function(data) {
	                if (data.clist != null && data.total > 0) {
	                    var list = data.clist;
	                    var len = list.length;
	                    
	                    var cboardStr = "";
	                    for (var i = 0; i < len; i++) {
	                        var consume = list[i];
	                        cboardStr += '<li><span>'
	                        	+  consume.amount + ' coins</span><span>' + consume.coinName + '</span>'
	                        	+ '<span>' + consume.appName + '</span></li>';
	                    }
						
	                    dataView.append(cboardStr,true);
	                    
	                }else{
	                	dataView.setHint("No record yet.");
	                	//dataView.append("No record yet",true);
	                }
	            }
	          });
	},true);
	
	
	
	/*popup*/
	hike.registerAction('payment.goToRecharge', function(dataset, ctx){
		dataset['action'] = '';
		hike.log.info("goto recharge:=============");
		var amount = $("input[name='exchange']"); 
		var sel;
		for(var x=0;x<amount.length;x++)
		{
		    if(amount[x].checked)
		    {
		       sel=amount[x].value;
		       break;
		    }
		}
		
		var hikeCoin;
		
		if (sel.indexOf(':')>=0)
			{
			var strs= new Array();
			strs=sel.split(":");     
			sel=strs[0];
			hikeCoin=strs[1];
			}else
			{
			hikeCoin=sel;	
			}
		var name=dataset.userName;
		var id=dataset.userId;
		var mobleNo=dataset.userPhone;
		var token=dataset.jsp_token;
		var needPincode=dataset.needPincode;
		var rechargeType = dataset.rechargeType || 'SNS';
		var transactionId = dataset.transactionId;
		var returnUrl = dataset.returnUrl;
		var isNative = dataset.isNative;
		ctx.trigger('afterrecharging');
		ctx.trigger('beforerecharging');
		var url="payment:recharge/init";
		ctx.post(url, {
				"uid":id,
				"transactionId":transactionId,
	            "amount":sel,
	            "isNative":isNative,
	            "returnUrl":returnUrl,
	            "phoneNumber":mobleNo,
	            "nickName":name,
	            "rechargeType":rechargeType,
	            "jsp_token":token
	        },{
	        	"200":function(data) {
	        		dataset['action'] = 'payment.goToRecharge';
	        		ctx.trigger('afterrecharging');
	        		var token=data['jspToken'];
	        		var orderId=data['paybill'].payBillId;
	        		var needPincode=data['needcode'];
	        		var hikeCoin=data['paybill'].hikeCoin;
		var config=null;
		var coinUnit = "";
		var rupeeUnit = "";
		if(sel == 1){
			coinUnit = "Coin";
			rupeeUnit = "Rupee";
		}else{
			coinUnit = "Coins";
			rupeeUnit = "Rupees";
		}
		hike.log.info("needPincode value==========:"+needPincode);
		if(needPincode=='forbidden')
			{
			config = {
					title : '',
					content : '<div class="payment">Your phone number is forbidden to recharge! <br />'+
						'<div class="bk15"><input type="button" data-widget="Button" class="ybtn full" data-action="recharge.modify" id="cancel" value="Confirm" /></div></div>'
				};
			hike.ui.showDialog(config);
			hike.ui.lockDialog();
			}
		// do not need pincode
		else if(needPincode=='false')
			{
			config = {
					title : '',
					content : '<div class="payment">Please confirm payment info here:<br />'+
						'<ul class="recharge-list">'+
						'<li><span>Nick Name: </span><span class="hint">'+name+'</span></li>'+
						'<li><span>Mobile Number: </span><span class="hint">'+mobleNo+'</span></li>'+
						'<li><span>Recharge Amount: </span><span class="hint">'+hikeCoin+' '+coinUnit+'</span></li>'+
						'<li><span>Actual Payment: </span><span class="hint">'+sel+' '+rupeeUnit+'</span></li>'+
						'<li><span>Payment Method: </span><span class="hint">Carrier Billing</span></li>'+
						'<li><span>Transaction ID: </span><span class="hint">'+orderId+'</span></li>'+
						'</ul>'+
						'<p class="error"></p>'+
						'<div class="bk15"><input type="button" data-widget="Button" data-action="payment.onrecharge" class="bluebtn full" data-transaction-id="'+transactionId+'"  data-is-native="'+isNative+'"  data-return-url="'+returnUrl+'"  data-recharge-type = "'+rechargeType+'" data-user-id="'+id+'" data-user-name="'+name+'" data-user-phone="'+mobleNo+'" data-jsp_token="'+token+'" data-sel="'+sel+'" data-need-pin="'+needPincode+'" data-order-id="'+orderId+'" value="Confirm and Continue" />'+
						'<div class="bk15"><input type="button" data-widget="Button" class="graybtn full" data-action="recharge.modify" id="cancel" value="Cancel" /></div></div>'+
						'</div>'
				};
			hike.ui.showDialog(config);
			hike.ui.lockDialog();
			}
		else
		{
			hike.callNative("PaymentNativeUI", "requestPayment", name, mobleNo, sel,hikeCoin,id,token,needPincode,orderId); 
 			
			window.onDialogGoToCharge  = function(message){ 
				hike.log.info("UtilDialog message = " + message);
				var contentText = hike.util.decodeJson(message);
				var name=contentText.hikeName;
				var id=contentText.id;
				var mobileNo=contentText.mobileNumber;
				var sel=contentText.rechargeAcount;
				var token=contentText.token;
				var needpin=contentText.needPincode;
				var orderId=contentText.orderId;
				hike.log.info(name+","+id+","+mobileNo+","+sel+","+token+","+needpin+","+orderId);
				var url="payment:recharge/confirm";
				hike.ui.closeDialog();
				ctx.post(url, {
						"uid":id,
			            "amount":sel,
			            "phoneNumber":mobileNo,
			            "nickName":name,
			            "jsp_token":token,
			            "pinCode":needpin,
			            "orderId":orderId
			        },{	"200":function(data) {
			        		hike.log.info("==================200===============");
			        		ctx.invokeAction('recharge.onRecahrgeSuccessed', data);
			        	}
			   },true);
			}
		}
	    }
	    });
		
	 });
	
	
	hike.registerAction('payment.onrecharge', function(dataset, ctx) {
		var name=dataset.userName;
		var id=dataset.userId;
		var mobleNo=dataset.userPhone;
		var sel=dataset.sel;
		var token=dataset.jsp_token;
		var needpin=dataset.needPin;
		var pincode=null;
		var rechargeType = dataset.rechargeType;
		var transactionId = dataset.transactionId;
		var returnUrl = dataset.returnUrl;
		var isNative = dataset.isNative;
		if(needpin=='true')
		{
			pincode=$('.paymentPincode').val();
		}
		$('.error').css({'visibility':'hidden'});		
		if(needpin=='true' && (pincode==null||pincode==''))
		{
			 $('.error').html("Please enter the pin Code!");
             $('.error').css({'visibility':'visible'});
             return;
		}
		dataset['action'] = '';
		ctx.trigger('afterrecharging');
		ctx.trigger('beforerecharging');
		var url="payment:recharge/confirm";
		ctx.post(url, {
				"uid":id,
				"transactionId":transactionId,
	            "amount":sel,
	            "isNative":isNative,
	            "returnUrl":returnUrl,
	            "phoneNumber":mobleNo,
	            "nickName":name,
	            "rechargeType":rechargeType,
	            "jsp_token":token,
	            "pinCode":pincode
	        },{
	        	"200":function(data) {
	        		dataset['action'] = 'payment.onrecharge';
	        		hike.ui.closeDialog();
					if(t!=undefined)
					{
						window.clearInterval(t);
					}
	        		ctx.trigger('afterrecharging');
	        		if(rechargeType == 'Game'){
	        			ctx.invokeAction('recharge.onGameRecahrgeSuccessed', data);
	        		}else if(rechargeType == 'SNS'){
	        			ctx.invokeAction('recharge.onRecahrgeSuccessed', data);
	        		}else{
	        			ctx.invokeAction('recharge.onRecahrgeSuccessed', data);
	        		}
	        		
	        	},
	        "201":function(data) {
				ctx.trigger('afterrecharging');
				$('.error').html("Wrong PIN CODE! Try again!");
	            $('.error').css({'visibility':'visible'});
        	},
	      	  "202":function(data) {
					ctx.trigger('afterrecharging');
					 $('.error').html(data.msg);
		             $('.error').css({'visibility':'visible'});
	        	},
	        	"500":function(data) {
					ctx.trigger('afterrecharging');
					 $('.error').html(data.msg);
		             $('.error').css({'visibility':'visible'});
	        	}
	   },true);
	});
	
hike.registerAction('payment.getpincode', function(dataset, ctx) {
		var mobleNo=dataset.userPhone;
		secs=120;
		$('.codebtn').addClass('wbtn');
		$('.codebtn').data('action','');
		t=window.setInterval(function update() {
	     	 if(--secs>0)
	     	  {
	     		 var strSecond=secs%60+'';
					if(strSecond.length==1)
						{
						strSecond='0'+strSecond;
						}
	     		 var str=('0'+Math.floor(secs/60))+':'+strSecond;
	     		$('.codebtn').val(str); 
	     	  }	
	     	 if(secs<=0)
	     		  {
	     		  $('.codebtn').removeClass('wbtn');
	     		  $('.codebtn').data('action','payment.getpincode');
	     		  $('.codebtn').val('get code');
	     		  window.clearInterval(t);
	     		  }
	      },1000)
		//hike.ui.closeDialog();
		 $('.error').css({'visibility':'hidden'});
		var url="payment:recharge/getpincode";
		ctx.get(url, {
	            "phoneNumber":mobleNo,
	        },{
	        	"200":function(data) {
	        		 var pincode = data['pinCode'];
	                 $('.paymentPincode').val(pincode);
	                 dataset.pincode=pincode;
	        	},
	        
	        "202":function(data) {
				 $('.error').html("Wrong PIN CODE! Try again!");
	             $('.error').css({'visibility':'visible'});
        	}
	   },true);
	});
	
	hike.registerAction('recharge.onRecahrgeSuccessed', function(dataset, ctx) {
		ctx.openView('payment/recharge-success', dataset);
	});
	hike.registerAction('recharge.onGameRecahrgeSuccessed', function(dataset, ctx) {
		ctx.openView('payment/recharge-game-success', dataset);
	});
	
	
	hike.registerAction('recharge.modify', function(dataset, ctx) {
		hike.ui.closeDialog();
		if(t!=undefined)
		{
			window.clearInterval(t);
		}
	});
	
	hike.registerAction('recharge.feedback', function(dataset, ctx) {
		var url = "payment:recharge/feedback";
		ctx.get(url, null, {
			"200" : function(data) {
				ctx.openView('payment/feedback', data);
			}
		})
	});
	
	hike.registerAction('recharge.feedbackSubmit', function(dataset, ctx) {
		$('.error').css({'visibility':'hidden'});
		var url = "payment:recharge/feedbacksubmit";
		var jspToken = dataset["jsp_token"];
		var message = $("#message").val();
		ctx.post(url, {
			"message" : message,
			"jsp_token" : jspToken
		},{
			"204" : function(data) {
				$('.error').html('the page is expired!');
				$('.error').css({'visibility':'visible'});
			},
			"201" : function(data) {
				ctx.forward('home.launch');
			},
			"202" : function(data) {
				$('.error').html('message cannot be empty!');
				$('.error').css({'visibility':'visible'});
				dataset["jsp_token"] = data.jspToken ;
			},
			"203" : function(data) {
				$('.error').html('message should be less than 300 letters.');
				$('.error').css({'visibility':'visible'});
				dataset["jsp_token"] = data.jspToken ;
			},
			"200" : function() {
				ctx.forward('payment.paymentCenter');
			}
		})
	});	
	
	
	hike.registerAction('payment.confirmAndConsume', function(dataset, ctx) {
		var transactionId = dataset.transactionId;
		var returnUrl = dataset.returnUrl;
		var isNative = dataset.isNative;
		var jspToken = dataset.jsp_token;
		var url="payment:payment/charge";
		ctx.post(url, {
			"isNative" : isNative,
			"transactionId" : transactionId,
			"returnUrl" : returnUrl,
			"jsp_token" : jspToken
		}, function(data) {
				ctx.openView('payment/consume-success', data);
			});
	});
	
	hike.registerAction('payment.tabActivate', function(dataset, ctx) {
		ctx.getWidget(dataset['tabGroupId']).openPage(parseInt(dataset["tabIndex"]));
	});
	
	/*
	 * recharge records details
	 * */
	hike.registerAction('payment.RechargeDetails', function(dataset, ctx) {
		ctx.openView('payment/recharge-records', {});
	});
	
	/*
	 * consume records details
	 * */
	hike.registerAction('payment.ConsumeDetails', function(dataset, ctx) {
		ctx.openView('payment/consume-records', {});
	});
	/*
	 * consumeconfirm
	 * */
	 hike.registerAction('payment.consumeconfirm', function(dataset, ctx) {
			ctx.openView('payment/consume-confirm', {});
	});
	 
	 /*
	 * consumesuccess
	 * */	 
	hike.registerAction('payment.consumesuccess', function(dataset, ctx) {
			ctx.openView('payment/consume-success', {});
	});
	 
	

	 hike.registerAction('payment.rechargeSuccess', function(dataset, ctx) {
			ctx.openView('payment/recharge-success', {});
	});
	 
	
	 consumebacktogame=function(){
		var isNative = document.getElementById("isNative").value;
		var returnUrl = document.getElementById("returnUrl").value;
		if(isNative == 'true'){
			window.prompt('closePayment');
		}
		else{
			if(window.parent){
				window.parent.postMessage("backtogame1",returnUrl);
			}else{
				window.close();
			}
		}
	}
	
	 
	  backtogame1=function(msg){
			var isNative = document.getElementById("isNative").value;
			var returnUrl = document.getElementById("returnUrl").value;
			if(isNative == 'true'){
				window.parent.window.postMessage(msg,returnUrl);
				window.prompt('closePayment');
			}
			else{
				if(window.parent){
					window.parent.window.postMessage(msg,returnUrl);
				}else{
					window.close();
				}
			}
		}
	 
	 
})();


