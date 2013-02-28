(function(){
	
var requests = {};
	
hike.defineClass('hike.android.BIFilter', {

		BIFilter : function(context){
			this.context = context;
			this.context.bind('afteraction', this.sendAction,this);
			this.context.bind('beforeopenview', this.sendView,this);
			this._get = this.context.get;
			this._post = this.context.post;
			this.context.get = this.delegateGet.delegate(this);
			this.context.post = this.delegatePost.delegate(this);
		},

		delegateGet : function(name, data, callbacks, silent){
			var startTime = new Date().getTime();
			var ctx = this.context;
			var tracker = this;
			return this._get.call(this.context, name, data, function(data){
				if(typeof callbacks == 'function'){
					callbacks.call(data, data);
				}else{
					var status = data['status'];
					if((status + '') in callbacks){
						callbacks[status].call(data, data);
					}else{
						hike.log.error('Unhandled status: ' + status + ' from ' + url);
					}
				}
				tracker.sendTiming('get', name, new Date().getTime() - startTime);
			}, silent);
		},
		
		delegatePost : function(name, data, callbacks, silent){
			var startTime = new Date().getTime();
			var ctx = this.context;
			var tracker = this;
			return this._post.call(this.context, name, data, function(data){
				if(typeof callbacks == 'function'){
					callbacks.call(data, data);
				}else{
					var status = data['status'];
					if((status + '') in callbacks){
						callbacks[status].call(data, data);
					}else{
						hike.log.error('Unhandled status: ' + status + ' from ' + url);
					}
				}
				tracker.sendTiming('post', name, new Date().getTime() - startTime);
			}, silent);
		},
		
		sendTiming : function(type, name, time){
			hike.callNative('Tracker', 'trackTiming', type, name, time);
		},
		
		sendAction : function(request){
			var fr = "";
			var accountId = "30";
			var utmr = "";
			if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)
					|| /MSIE(\d+\.\d+);/.test(navigator.userAgent)) {
			} else {
				utmr = location.href;
			}
			var utmhn = hike.domain.DOMAIN_SNS; 
			var utmpa="";
			var utmp = request.action; //action
			var utfr="";
			if(fr.length>0){
				utfr=fr;
			}
			var utmcc = "";
			
			var name = "passport" + hike.domain.ROOT_DOMAIN;
			var value = this.getCookie(name);
			utmcc = value != null ? value : utmcc;
			var img = document.getElementById("gummyStateImg");
			if (!img) {
				var img = document.createElement("img");
				img.id = "gummyStateImg";
				img.style.display = "none";
				img.src = "http://"+hike.domain.DOMAIN_STAT+"/stat.gif?utmac=" + accountId
						+ "&utmcc=" + utmcc + "&utmhn=" + utmhn + "&utmp=" + utmp
						+ "&utmpa=" + utmpa + "&utmr=" + utmr +"&utfr="+utfr+"&utmn="
						+ Math.random();
				document.getElementsByTagName("body")[0].appendChild(img);
			} else {
				img.src = "http://"+hike.domain.DOMAIN_STAT+"/stat.gif?utmac=" + accountId
						+ "&utmcc=" + utmcc + "&utmhn=" + utmhn + "&utmp=" + utmp
						+ "&utmpa=" + utmpa + "&utmr=" + utmr+"&utfr="+utfr + "&utmn="
						+ Math.random();
			}
		},
		
		sendView : function(evt){
			hike.callNative('Tracker', 'trackView', evt.viewName);
		},

		getCookie : function(name){
			var user =  this.context.getItem('user');
			var userid = null;
			if(user!=null){
				userid = user.userId;
			}
			return userid;
		}
});

})();