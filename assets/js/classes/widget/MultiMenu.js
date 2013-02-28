hike.require('hike.widget.MaskLayer', function(MaskLayer){
	
	hike.defineClass('hike.widget.MultiMenu', 'hike.widget.Widget', {
		statics : {
			ts : new Date().getTime()
		},
		
		initElements : function(){
			this.callSuper();
			
			this.bodytouched=false;
			this.bodymoved=false;
			this.curX=0;
			this.curY=0;
			this.show=false;
			this.selftouched=false;
			
		},
		initLayer : function(){
			this.layer=$("<div class='control'></div>").get(0);
			$(this.layer).append($(this.element).children("div"));
			$("article").append($(this.layer));
			//$(this.layer).bind('click',this.hideLayer.delegate(this));
			//this.hideLayer();
		},
		initEvents : function(){
			this.callSuper();
			//$(this.element).bind('touchstart mousedown',this.onBodyTouchStart=this.onBodyTouchStart.delegate(this));
		},
		onClick:function(e){
			if(this.show){
				this.hideLayer();
			}else{
				this.showLayer();
			}
			e.stopPropagation();
		},
		
		onBodyTouchStart : function(e){
			this.curX = e.touches?e.touches[0].pageX:e.pageX;
			this.curY = e.touches?e.touches[0].pageY:e.pageY;
			this.bodytouched=true;
		},
		onBodyTouchMove:function(e){
			if(this.bodytouched)
			{
				var nowX = e.touches?e.touches[0].pageX:e.pageX;
				var nowY = e.touches?e.touches[0].pageY:e.pageY;
				if(Math.abs(nowX-this.curX)>30 || Math.abs(nowY-this.curY)>30)
					this.bodymoved=true;
			}
		},
		onBodyTouchEnd:function(e){
				if(!this.bodymoved && !e.withIn(this.element) && !(e.withIn(this.layer))){
					this.hideLayer();
				}
				this.bodymoved=false;
				this.bodytouched=false;
			//}
		},
		
		showLayer : function(){
			if(this.layer==null)
				this.initLayer();
			var $layer = $(this.layer);
			var el=$(this.element).children()[0];
			var x = this.getPageLeft(el);// - (this.layer.offsetWidth - el.offsetWidth) / 2;
			var y = this.getPageTop(el) + el.offsetHeight+8;
			var layerwidth=parseInt($(this.layer).css("width").replace("px",""));
			var layerheight=parseInt($(this.layer).css("height").replace("px",""));
			var screenWidth=document.documentElement.clientWidth;
			var screenHeight=document.documentElement.clientHeight;
			this.layer.className="control";
			if((screenWidth-x)<layerwidth)
				x=screenWidth-layerwidth;
			if((screenHeight-this.getWindowTop(el)-el.offsetHeight-8)<layerheight){
				y = this.getPageTop(el) - layerheight-8;
				this.layer.className="control reverse";
			}
			$layer.css('left', x+"px");
			$layer.css('top', y+"px");
			$layer.css('display', '-webkit-box');
			this.show=true;
			//alert("show"+$($(this.element).children()[1]).html());
		},
		
		hideLayer : function(){
			if(this.layer==null)
				return;
			window.setTimeout(function(){
				$(this.layer).css('display', 'none');
				this.show = false;
			}.delegate(this), 10);
		},
		
		getPageLeft : function(el){
			var left = 0;
			do{
				left += el.offsetLeft;
				el = el.offsetParent;
			}while(el != null)
			return left;
		},
		
		getWindowTop : function(el){
			var top = 0;
			do{
				top += el.offsetTop;
				el = el.offsetParent;
			}while(el && el.className != "inner")
			var scrollTop=document.body.scrollTop>document.documentElement.scrollTop?document.body.scrollTop:document.documentElement.scrollTop;
			return top-scrollTop;
		},
		
		getPageTop:function(el){
			var top = 0;
			do{
				top += el.offsetTop;
				el = el.offsetParent;
			}while(el && el.className != "inner")
			return top;
		},
		
		destroy : function(){
			this.callSuper();
			if(this.layer!=null)
				$(this.layer).remove();
		}
		
	});
	
});
