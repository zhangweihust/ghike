hike.require(function(){
	hike.defineClass('hike.widget.LoadingPager', 'hike.widget.Widget', {
		initElements:function(){
			this.callSuper();
			this.perPage=4;
			this.curPage=1;
			this.pageCnt=5;
		},
		initEvents:function(){
			this.callSuper();
			this.updatePager();
			this.getData();
		},
		turnPage:function(){
			this.curPage=(this.curPage<this.pageCnt)?(this.curPage+1):1;
		},
		updatePager:function(){
			this.middle=$(this.element).children(".holder").children(".slider").children(".middle").get(0);
		},
		getData:function(){
			this.trigger('loaddata', this);
		},
		append : function(html){			
			$(this.middle).html("");
			$(this.middle).append(html);			
			this.update();
		},
		destroy : function(){
			this.callSuper();
		}
	});
});