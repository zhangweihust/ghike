hike.require(function(){
	hike.defineClass('hike.widget.InputFocus', 'hike.widget.Widget', {
		initElements : function(){
			this.callSuper();
			this.parentEle="LI";
			this.kindIsNumber=false;
		},
		initEvents : function(){
			this.callSuper();
			$(this.element).bind("click",this.onClick.delegate(this));
			$(this.element).bind("blur",this.onBlur.delegate(this));
			if(this.element.getDataset()["kind"]=="number"){
				this.kindIsNumber=true;
				$(this.element).attr("type","text");
			}
		},
		onClick:function(e){
			e.preventDefault();
			var $p=$(this.GetParent());
			$p.addClass("active");
			if(this.kindIsNumber){
				$(this.element).attr("type","number");
			}
		},
		onBlur:function(e){
			e.preventDefault();
			var $p=$(this.GetParent());
			$p.removeClass("active");
			if(this.kindIsNumber){
				$(this.element).attr("type","text");
			}
		},
		onTouchStart : function(e){
			if(this.kindIsNumber){
				$(this.element).attr("type","number");
			}
		},
		onBodyTouchStart:function(e){
			if(!e.withIn(this.element) && this.kindIsNumber){
				$(this.element).attr("type","text");
			}
		},
		GetParent:function(){
			var p=$(this.element).parent().get(0);
			for(;p!=null;p=$(p).parent().get(0)){
				if(p.tagName==this.parentEle)
					return p;
			}
			return null;
		},
		destroy : function(){
			this.callSuper();
		}
		
	});
	
});
