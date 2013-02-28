hike.require('hike.android.Context','hike.fwk.Executor','hike.fwk.ViewMgr','hike.fwk.ModuleLoader',
	'hike.xui.Animation','hike.android.History','hike.android.TemplateLoader','hike.tpl.ViewBuilder', 'hike.widget.MaskLayer', 'hike.util.UserAgent', 'hike.android.MainFrame',
	'hike.android.TimeUtil','hike.android.TplBatchLoader','hike.android.BIFilter',
	
function(Context, Executor, ViewMgr, ModuleLoader, Animation, History, TemplateLoader, ViewBuilder, MaskLayer, UserAgent, MainFrame,TimeUtil,TplBatchLoader,BIFilter){
		
	var loader;
	var executor;
	var animation;
	var viewMgr;
	var context;
	var timeUtil;
	var tplBatchLoader;
	var unmaskTimeout;
	var biFilter;
	
	var doRecharge = function(){
		MaskLayer.mask('<div class="recharger">Recharging</div>');
	};
	
	var doUnmask = function(){
		MaskLayer.unmask();
	};
	
	var afterViewOpenTimeout;
	
	hike.defineClass('hike.android.Launcher', {
		
		statics : {
			launch : function(){
				
				var frame = MainFrame.getInstance();
				
				loader = new ModuleLoader({
					'payment' : 'file:///android_asset/js/modules',
					'*' : 'file:///android_asset/js/modules'
				});
				executor = new Executor(loader);
				animation = Animation.getInstance();
				viewMgr = new ViewMgr(new ViewBuilder(new TemplateLoader(), animation, frame));
				
				context = new Context(executor, viewMgr, {
					'payment' : hike.domain.URL_SNS_PAY,
					'*' : hike.domain.URL_SNS_TOUCH
				});
				
				context.bind('beforerequest', function(){
					hike.callNative('NativeUI', 'onBeforeRequest');
				}, this);
				context.bind('afterrequest', function(){
					hike.callNative('NativeUI', 'onRequestSuccess');
				}, this);
				context.bind('requesterror', function(){
					hike.callNative('NativeUI', 'onRequestError');
				}, this);
				context.bind('beforeopenview', function(){
					if(context.reloading)
						return;
					hike.callNative('NativeUI', 'onBeforeOpenView');
				}, this);
				context.bind('afteropenview', function(){
					if(context.reloading)
						return;
					if(afterViewOpenTimeout != undefined)
						window.clearTimeout(afterViewOpenTimeout);
					afterViewOpenTimeout = window.setTimeout(function(){
						hike.callNative('NativeUI', 'onAfterOpenView');
					}, 100);
				}, this);
				
				//payment
				context.bind('beforerecharging', doRecharge, this);
				context.bind('afterrecharging', doUnmask, this);
				biFilter = new BIFilter(context,this);

				//timeUtil
				timeUtil = new TimeUtil();
				tplBatchLoader = new TplBatchLoader([
				              			            {101:'Changing_nickName'},
				            			            {102:'Change_Picture'},
				            			            {103:'Register'},	 
				            			            {301:'Relationship_Onedegree'}, 
				            			            {501:'Update_Status'},
				            			            {502:'Share'},
				            			            {601:'Game_Play'}, 
				            			            {602:'Game_Like'},
				            			            {603:'Game_Invite'},  
//				            			            {604:'Game_Challenge'},  
				            			            {605:'Game_Rank'},
//				            			            {606:'Game_Defeat'},
//				            			            {607:'Game_Tie'},
				            			            {608:'Challenge'},
				            			            {609:'Game_Competition'},
				            			            {610:'Subscribe_Forum'},  
				            			            {801:'Platform_Badge'},
				            			            {802:'Platform_LevelUp'},
				            			            {9999:'Share_Ment'},
				            			            {10000:'api'},
				            			            {9998:'Comment'}
				            			]);
				tplBatchLoader.loadAllCallback(function(){
					hike.log.debug('load newsfeed tpl finish ');
				});
				
				//init ui functions
				hike.ui = {
					showDialog : function(config){
						hike.require('hike.widget.Dialog', function(Dialog){
							Dialog.show(config, context);
						});
					},
					closeDialog : function(){
						hike.require('hike.widget.Dialog', function(Dialog){
							Dialog.close();
						});
					},
					lockDialog : function(){
						hike.require('hike.widget.Dialog', function(Dialog){
							Dialog.lock();
						});
					},
					unlockDialog : function(){
						hike.require('hike.widget.Dialog', function(Dialog){
							Dialog.unlock();
						});
					},
					showError : function(info){
						hike.callNative('NativeUI', 'showError', info);
					},
					focus : function(el){
						setTimeout(function(){
							frame.focusTo(el);
						}, 10);
					}
				};
				
				//public interface
				hike.registerAction = function(name, fn){
					loader.defineAction(name, fn);
				};
				
				hike.registerRequestInterceptor = function(pattern, interceptor){
					context.addRequestInterceptor(pattern, interceptor);
				};
				
				hike.registerViewMapping = function(from, to){
					viewMgr.setViewMapping(from, to);
				};
				
				hike.invokeAction = function(actionName, dataset){
					context.invokeAction(actionName, dataset);
				};
				
				hike.getContext = function(){
					return context;
				};
				
				window.onerror = function(e){
					hike.log.error(e);
				};
				
				hike.callNative = function(){
					var url = [];
					for(var i=0;i<arguments.length;i++){
						url.push(arguments[i] == null?
							'null'
							:(typeof arguments[i] == 'object'?
								hike.util.encodeJson(arguments[i])
								:arguments[i]));
					}
					url = url.join('&');
					return window.prompt(url);
				};
				
				hike.backward = function(){
					History.getInstance().backward();
				};
				
				hike.replaceState = function(action){
					History.getInstance().replaceState(action, {});
				};
				
				hike.log.info('Web App starting');

				loader.load('challenge');
				loader.load('forum');
				loader.load('friend');
				loader.load('games');
				loader.load('growth');
				loader.load('home');
				loader.load('notify');
				loader.load('payment');
				loader.load('profile');
				loader.load('topic');
				
				//hike.loadScript('file:///android_asset/js/modules/android.js', function(){
					
				frame.init(document.body, context);
				var launcher = window.prompt("Launch");
				if(launcher != null){
					launcher = hike.util.decodeJson(launcher);
					context.invokeAction('home.launch', launcher);
				}else{
					var state = History.getInstance().getState();
					if(state == null){
						context.invokeAction('home.launch', {});
					}else{
						context.forward('home.launch', state);
						History.timestamp = state.ts;
					}
				}
			}
		}
		
	});
});
