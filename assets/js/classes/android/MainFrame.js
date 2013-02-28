hike.require(function(){
	
	var userInfoPattern = [/^profile\/comfirm-avata$/, /personal/];
	
	var ignoreTypes = {
		'input' : true,
		'textarea' : true,
		'select' : true,
	};
	
	var preventDefault = function(e){
		if(e.target.nodeName.toLowerCase() in ignoreTypes)
			return;
		e.preventDefault();
	};
	
	var mainTpl = '<div class="main">' + 
		'<div class="wrapper"><div class="inner"></div></div>'
	'</div>';
	
	hike.defineClass('hike.android.MainFrame', {
		
		playing : false,
		
		statics : {
			
			getInstance : function(){
				if(!this.instance)
					this.instance = new hike.android.MainFrame();
				return this.instance;
			}
			
		},
		
		MainFrame : function(el){},
		
		init : function(el, context){
			this.element = el;
			this.context = context;
			this.mainBody = $(mainTpl).get(0);
			$(el).append(this.mainBody);
			var $main = $(this.mainBody);
			$main.children().css('min-width', document.documentElement.clientWidth + 'px');
			$main.css('min-height', document.documentElement.clientHeight + 'px');
			this.initEvents();
			this.initHock();
		},
		
		initEvents : function(){
			this.context.bind('change', this.onContextChange, this);
			this.context.bind('afteropenview', this.afterViewOpen, this);
			this.context.bind('afterrequest', this.afterRequest, this);
			$(window).bind('resize', this.resolveSize.delegate(this));		
		},
		
		initHock : function(){
			var frame = this;
			var ctx = this.context;
			
			var interceptAction = function(actionName){
				hike.registerAction('xui.' + actionName, function(dataset, ctx){
					hike.log.info('Set navigator to menu');
					ctx.setItem('navigator', 'menu');
					ctx.forward(actionName, dataset);
				});
			}
			
			interceptAction('about.version');
			interceptAction('profile.basic-info');
			interceptAction('home.openHomepage');
			interceptAction('profile.openMyFriendList');
			interceptAction('games.gamelist');
			interceptAction('games.browseGames');
			interceptAction('games.leaderboard');
			interceptAction('payment.paymentCenter');
			interceptAction('games.openDetail');
			interceptAction('home.openTimeLine');
			interceptAction('games.mygames');
			
			///window.NativeUI.closeSlideMenu();
			window.UIProxy = {
				openAbout : function(){
					ctx.invokeAction('xui.about.version');
				},
				openProfile : function(){
					ctx.invokeAction('xui.profile.basic-info');
				},
				openTimeline : function(){
					ctx.invokeAction('xui.home.openTimeLine');
				},
				openTopGames : function(tabIndex){
					ctx.invokeAction('xui.home.openHomepage', {
						tabIndex : tabIndex
					});
				},
				openMyFriends : function(){
					ctx.invokeAction('xui.profile.openMyFriendList');
				},
				openRecentlyPlayed : function(){
					ctx.invokeAction('xui.games.gamelist', {
						gameType : 'recently-played'
					});
				},
				openMyGames : function(){
					ctx.invokeAction('xui.games.mygames', {});
				},
				openGameCategory : function(){
					ctx.invokeAction('xui.games.browseGames');
				},
				openSearch : function(){
					ctx.invokeAction('home.to-search');
				},
				openHikeCoins : function(){
					ctx.invokeAction('xui.payment.paymentCenter');
				},				
				openSetting : function(){
					ctx.invokeAction('profile.setting');
				},
				openGames : function(){
					ctx.invokeAction('xui.games.gamelist',{
						'gameType':"all"
					});
				},
				openNotify : function(){
					ctx.invokeAction('notify.friend');
				},
				openGameDetail : function(gameId){
					ctx.invokeAction('games.openDetail',{'gameId':gameId});
				},
				openLeaderboard : function(){
					ctx.invokeAction('xui.games.leaderboard',{'boardType':'overall-leaderboard'});
				},
				invokeToolAction : function(actionname,json){			
					var dataset = hike.util.decodeJson(json);
					ctx.invokeAction(actionname,dataset);
					
				},
				goBack : function(){
					hike.backward();
				}
			};
		},
		
		resolveSize : function(){
			$(this.mainBody).css('min-height', this.getPageHeight() + 'px');
		},
		
		getContext : function(){
			return this.context;
		},
		
		afterViewOpen : function(view){
			this.onPageRendered(view.widget.element);
		},
		
		afterRequest : function(evt){
			var name = evt.name;
			for(var p=0;p<userInfoPattern.length;p++){
				if(userInfoPattern[p].test(name)){
					this.reloadUserInfo();
					return;
				}
			}
		},
		
		reloadUserInfo : function(){
			var cache = this.getContext().getCache();
			this.context.get("checkuser", {}, function(data){
				var user = data.user;
				cache.persist('userInfo', user);
				this.updateUserInfo(user || {});
			}.delegate(this), true);
		},
		
		onContextChange : function(evt){
			if(evt.name == 'user'){
				this.updateUserInfo(evt.value || {});
			}
		},
		
		updateUserInfo : function(user){
			hike.callNative('NativeUI', 'setUserInfo', user.nickname, user.headUrl, user.coverUrl);
		},
		
		getMainBody : function(){
			return this.mainBody;
		},
		
		getPageContainer : function(){
			return this.getMainBody();
		},
		
		getPageHeight : function(){
			return document.documentElement.clientHeight;
		},
		
		onPageRendered : function(el){
			var ds = el.getDataset();
			var navigator = this.getContext().getItem('navigator') || ds.navigator || 'back';
			this.renderHeader({
				title : ds.pageTitle,
				tool : ds.tool || (navigator == 'menu' ? 'notify':null),
				navigator : navigator,
				dataset : ds
			});
			hike.log.info('Clear navigator ');
			this.getContext().setItem('navigator', undefined);
			this.currentPageEl = el;
			window.scrollTo(0, 0);
		},
		
		renderPage : function(el){
			$(this.getPageContainer()).append(el);
			$(el).css({
				minHeight : this.getPageHeight() + 'px'
			});
		},
		
		renderHeader : function(cfg){
			var ctx = this.getContext();
			var navigator = cfg.navigator || null;
			var tool = cfg.tool || null;
			var title = cfg.title || null;
			hike.callNative('NativeUI', 'setHeader', title);
			hike.callNative('NativeUI', 'setNavigator', navigator);
			hike.callNative('NativeUI', 'setTool', tool);
		},
		
		isOnline : function(){
			return true;
		},
		
		focusTo : function(el){
			el.focus();
		}
		
	});
	
});
