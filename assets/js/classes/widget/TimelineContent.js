hike.require(function(){
	
	hike.defineClass('hike.widget.TimelineContent', 'hike.widget.Widget', {
		
		initElements : function(){
			this.callSuper();
			this.replaceFace();
		},
		
		replaceFace : function(){
			var faceArray = ['adore','aggre','angry','badly','beatn','beer','nogud','bmile','bomb','borin','call','ctail','coffe','cold','cool','cry','cryin','dspir','dsapt','dsgut','dizzy','money','excla','noexp','fpalm','frown','frust','furis','gigle','happy','hi','hug','hugry','hypno','hstri','idea','impis','kiss','kised','laugh','lOl','monoc','movie','music','nerd','ninja','party','pirat','puden','quesn','rage','rose','sad','satif','scare','shock','sick','sing','sleep','smile','smokn','snoty','sorry','stars','stop','stres','strug','study','suprs','sweat','angel','think','tdown','thmup','waitn','whstl','wink','woo','wnout','yawn','ioi'];
			var beginValue = this.element.innerHTML;
			var getValue = "";
			var  leftReg = new RegExp('\\[\\w*\\]', "g");
			var hasLeft= beginValue.indexOf("[");
			var hasRight= beginValue.indexOf("]");
			var startValue = beginValue.match(leftReg);
			if(hasLeft !=-1 && hasRight !=-1&& null!=startValue){
				var leftSize = startValue.length;
				for(i=0;i<leftSize;i++){
					var replaceValue =startValue[i];
					for(arr in faceArray){
						if(replaceValue=='['+faceArray[arr]+']'){
							faceValue = "<img src='file:///android_asset/img/touch/emotion/"+faceArray[arr]+".png'  alt='"+faceArray[arr]+"' width='36' height='36' />";
							this.element.innerHTML=this.element.innerHTML.replace(replaceValue,faceValue);
						}
					}
				}
			}
			this.update();
		},
		
		destroy : function(){
			this.callSuper();
		}
		
	});
	
});