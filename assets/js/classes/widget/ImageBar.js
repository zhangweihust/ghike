hike.require(function(){
	hike.defineClass('hike.widget.ImageBar', 'hike.widget.Widget', {
		
		initElements:function(){
			this.callSuper();
			
			this.perPageWidth=document.documentElement.clientWidth*0.96;
			this.curPage=1;
		},
		initEvents:function(){
			this.callSuper();
			this.holder=$(this.element).get(0);
			this.slider=$(this.element).children(".image_bar").get(0);
			var itemcnt=$(this.slider).children().length;
			if(itemcnt<=4)
				this.scroll=false;
			else
				this.scroll=true;
			this.pageCnt=parseInt((itemcnt-1)/4)+1;
			if(itemcnt<4){
				for(var i=0;i<4-itemcnt;i++){
					$(this.slider).append($("<li><div class='avata' style='background-image:url(file:///android_asset/img/game_place_holder.png)' ></div><span>&nbsp;</span></li>"));
				}
			}	
			this.right=document.documentElement.clientWidth*0.24*($(this.slider).children().length-4);
		},
		onTouchStart:function(e){
			if(this.scroll){
				this.moving = true;
				this.startX = e.touches?e.touches[0].pageX:e.pageX;
				this.startLeft=this.getCssLeft();
				this.started = false;
				e.preventDefault();
			}
		},
		onTouchMove:function(e){
			if(this.scroll){
				if(!this.moving)
					return;
				this.curX = e.touches?e.touches[0].pageX:e.pageX;
				var dist = this.curX-this.startX;
				if(!this.started && Math.abs(dist) < 20)
					return;
				this.started = true;
				//$(this.slider).css("left",(this.startLeft+dist)+"px");
				$(this.slider).css("-webkit-transition","");
				this.slider.style.webkitTransform="translateX("+(this.startLeft+dist)+"px)";
				e.preventDefault();
			}
		},
		onTouchEnd:function(e){
			if(this.scroll){
				if(!this.moving)
					return;
				if(this.started == false){
					var evt = document.createEvent("MouseEvents");
					evt.initMouseEvent('click', true, true, window,
						    0, 0, 0, 0, 0, false, false, false, false, 0, null)
					e.target.dispatchEvent(evt);
					return;
				}
				var dist=this.curX-this.startX;
				if(dist>0)
					this.curPage=this.curPage-1<1?1:this.curPage-1;
				if(dist<0)
					this.curPage=this.curPage+1>this.pageCnt?this.pageCnt:this.curPage+1;
				var curLeft=-this.perPageWidth*(this.curPage-1);
				$(this.slider).css("-webkit-transition","-webkit-transform 0.5s ease-out");
				this.slider.style.webkitTransform="translateX("+this.getEndleft(-this.perPageWidth*(this.curPage-1))+"px)";
				this.moving = false;
			}
		},
		getCssLeft:function(){
			return parseInt(this.slider.style.webkitTransform.replace("translateX(","").replace("px)","")) || 0;
		},
		getEndleft:function(dist){
			//var curleft=(this.startLeft+dist>0)?0:((this.startLeft+dist)<-this.right?-this.right:(this.startLeft+dist));
			return dist<-this.right?-this.right:dist;
		},
		destroy : function(){
			this.callSuper();
		}
	});
});