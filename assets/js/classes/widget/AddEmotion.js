hike.require(function(){
	
	hike.defineClass('hike.widget.AddEmotion', 'hike.widget.Widget', {
		
		initElements : function(){
			this.callSuper();
			this.resetDom();
			this.bottom;
			this.showtable = false;
			this.up;
			this.down;
			this.tablePage = 0;
			this.spanName;
			this.tableActive;
			this.leftMax;
			this.cssLeft = 0;
			this.isClick = false;
			this.startX;
			this.endX;
			this.textarea = $(this.element).parent().find(".contents").get(0);
			this.maxlength = $(this.textarea).attr("maxLength");
		},
		initEvents : function(){
			this.callSuper();
			setTimeout(function(){
			    this.bindClick()
			}.delegate(this), 20);
			
		},
		bindClick : function(){
			$('.facetable').bind('touchstart mousedown',this.onTableDown=this.onTableDown.delegate(this));
			$('.facetable').bind('touchend mouseup',this.onTableUp=this.onTableUp.delegate(this));
			$('.facetable').bind('touchmove mousemove',this.onTableMove=this.onTableMove.delegate(this));
			//$('.send-face').bind('touchstart mousedown',this.showFace=this.showFace.delegate(this));
			//$('.facemian').bind('touchstart mousedown',this.changeStyle=this.changeStyle.delegate(this));
			//$('.facemian').bind('touchend mouseup',this.insertFace=this.insertFace.delegate(this));
		},
		addDom : function(){
			var faceArray = ['adore','aggre','angry','badly','beatn','beer','nogud','bmile','bomb','borin','call','ctail','coffe','cold','cool','cry','cryin','dspir','dsapt','dsgut','dizzy','money','excla','noexp','fpalm','frown','frust','furis','gigle','happy','hi','hug','hugry','hypno','hstri','idea','impis','kiss','kised','laugh','lOl','monoc','movie','music','nerd','ninja','party','pirat','puden','quesn','rage','rose','sad','satif','scare','shock','sick','sing','sleep','smile','smokn','snoty','sorry','stars','stop','stres','strug','study','suprs','sweat','angel','think','tdown','thmup','waitn','whstl','wink','woo','wnout','yawn'];
			var parentHeight = this.element.offsetParent.offsetHeight;
			var childTop = this.element.offsetTop;
			var childHeight = this.element.offsetHeight;
			var resultHeight = parseInt(parentHeight)-parseInt(childTop)-parseInt(childHeight);
			var allFacenum = faceArray.length;
			var facePagenum = Math.ceil(allFacenum/(4*8));
			var tdNumber = 8*facePagenum;
			var tdWith = 100/tdNumber;
			$facetable = $('<table class="facetable" cellspacing="1" cellpadding="0"></table>');
			var $faceport = $('<div class="faceport"></div>');
			var $page =$('<div class="pagespan"></div>');
			var trLast =document.createElement('tr');
			$facetable.css('width',facePagenum+'00%');
			for(i=0;i<4;i++){
				var tr = document.createElement('tr');
				$facetable.append(tr);
				for(j=0;j<tdNumber;j++){
					var td = document.createElement('td');
					td.style.width =tdWith+"%";
					td.className = "facemian";
					tr.appendChild(td);
				}
			};
			for(k=0;k<facePagenum;k++){
				var span = document.createElement('span');
				if(k==0){
					span.className = "active normal";
				}else{
					span.className = "normal";
				}
				$page.append(span);
			}
			var jsonNum = 0;
			var pageNum ='<td colspan="'+tdNumber+'" class="pagenum"></td>';
			trLast.innerHTML = pageNum ;
			
			$facetable.append(trLast);
			$faceport.append($facetable).append($page);
			
			$(this.element).append($faceport);
			//$faceport.css("visibility","hidden");
			bottom = resultHeight;
			//$faceport.css('height',bottom+'px');
			//$facetable.css('bottom',(-bottom)+'px');
			this.cssLeft = parseInt($facetable.css('left'));
			this.spanName = document.getElementsByClassName("normal");
			this.tableActive = this.spanName.length;
			this.leftMax = (this.tableActive-1)*100;
			for (p=1;p<=facePagenum;p++){
				for(h=0;h<4;h++){
					for(d=1;d<=8;d++){
						var tdList = (p-1)*8+d-1;
						var faceValue ="<img src='file:///android_asset/img/touch/emotion/"+faceArray[jsonNum]+".png'  alt='["+faceArray[jsonNum]+"]'/>";
						jsonNum++;
						if(jsonNum<=allFacenum){
							document.getElementsByTagName("tr")[h].childNodes[tdList].innerHTML=faceValue;
						}
					}
				}
			}
		},
		
		resetDom : function(){
			setTimeout(function(){
			    this.addDom()
			}.delegate(this), 10);
		},
		

		
		showFace : function(e){
			if(document.activeElement.tagName == 'INPUT'){
				return;
			}
			
			if(this.showtable == false){
				this.showUp();
			}else{
				this.closeDnow();				
			}
			e.stopPropagation();
			e.preventDefault();
		},

		showUp : function(){
			this.showtable = true;
			$(".faceport").css("visibility","visible");
			$(".facetable")[0].style.bottom ="0px";
			function showSpan(){
				$(".pagespan").show();
				$(".pagespan").find('span')[0].className = "active normal";
			}
			window.setTimeout(showSpan,1000);
		},

		closeDnow : function(){
			this.showtable = false;
			this.bottom=$(".facetable")[0].offsetHeight;
			this.cssLeft = 0;
			this.tablePage = 0;
			$(".facetable")[0].style.bottom = -(this.bottom)+"px";
			function hiddenFace(){
				$(".faceport").css("visibility","hidden");
				$(".facetable")[0].style.left ="0px";
				$('span.active').removeClass('active');
			}
			window.setTimeout(hiddenFace,1100);
			$(".pagespan").hide();
			//$('span.normal').first().addClass('active');
			
		},
		changeStyle:function(e){
			if(e.target.tagName=="IMG")
			{
				$(e.target).parent().css('background','#ddd');
				setTimeout(function(){$(e.target).parent().css('background','');},200);
			}
			else
			{
				$(e.target).css('background','#ddd');
				setTimeout(function(){$(e.target).css('background','');},200);
			}
			e.preventDefault();
		},
		insertFace : function (e){
			if(e.target.tagName=="IMG")
				$(e.target).parent().css('background','');
			else
				$(e.target).css('background','');
			
			if(document.activeElement.tagName == 'INPUT'){
				return;
			}
			var facetxt = $(e.target).html();
			var faceinfo = $(e.target).attr('alt');
			if(faceinfo == null){
				if(facetxt!=""){
					faceinfo = $(e.target).find('img').attr('alt');
					this.Insert(faceinfo);
					//this.textarea.blur();
					$(this.textarea).trigger('keyup');
					//this.showFace();
				}
				return;
			}else{
				faceinfo = $(e.target).attr('alt');
				this.Insert(faceinfo);
				//this.textarea.blur();
				$(this.textarea).trigger('keyup');
				//this.showFace();
			}
			return;
		},
		
		tableRight : function(){
			if(this.cssLeft<=(-this.leftMax)){
				this.cssLeft = (-this.leftMax);
				this.tablePage = this.tableActive-1;
				$facetable.css('left',this.cssLeft+"%");
			}else{
				this.cssLeft -= 100;
				this.tablePage++;
				$facetable.css('left',this.cssLeft+"%");
				for (m=0;m<this.tableActive;m++){
					if(m!=this.tablePage){
						this.spanName[m].className = "normal";
					}else{
						this.spanName[m].className = "active normal";
					}
				}

			}
		},
		tableLeft : function(){
			if(this.cssLeft >= 0){
				this.cssLeft = 0;
				$facetable.css('left',this.cssLeft+"%");
			}else{
				this.cssLeft += 100;
				this.tablePage--;
				$facetable.css('left',this.cssLeft+"%");
				for (m=0;m<this.tableActive;m++){
					if(m!=this.tablePage){
						this.spanName[m].className = "normal";
					}else{
						this.spanName[m].className = "active normal";
					}
				}
			}
		},
		Insert : function(str) {
			//var obj = document.getElementsByTagName("textarea")[0];
			var obj = this.textarea;
			if(this.maxlength-6 <= $(obj).val().length) return;
			if(document.selection) {
				//obj.focus();
				var sel=document.selection.createRange();
				document.selection.empty();
				sel.text = str;
			} else {
				//alert("start:"+obj.selectionStart+"end:"+obj.selectionEnd);
				var prefix, main, suffix;
				prefix = obj.value.substring(0, obj.selectionStart);
				main = obj.value.substring(obj.selectionStart, obj.selectionEnd);
				suffix = obj.value.substring(obj.selectionEnd);
				obj.value = prefix + str + suffix;
				
				if (obj.createTextRange) {
			        var range = obj.createTextRange();
			        range.collapse(true);
			        range.moveEnd('character', obj.value.length-suffix.length);
			        range.moveStart('character', obj.value.length-suffix.length);
			        range.select();
			    } else if (obj.setSelectionRange) {
			    	obj.focus();
			    	obj.setSelectionRange(obj.value.length-suffix.length, obj.value.length-suffix.length);
			    }
				//obj.value+=str;
			}
			//obj.focus();
			//obj.blur();
		},
		onTableDown : function(e){
			this.isDrag = true;
	  		this.isClick=true;
			if(e.targetTouches!=undefined){
				this.startX = e.targetTouches[0].pageX;
			}else{
				this.startX=e.pageX;
			}
			e.preventDefault();
		},
		onTableUp : function(e){
			if(!this.isDrag) return;
			this.isDrag = false;
			var  s = this.endX-this.startX;
			if(s>100 && this.isClick==false){
				this.tableLeft();
				this.isClick==true
			}else if(s<-100 && this.isClick==false){
				this.tableRight();
				this.isClick==true
			}else if(this.isClick==true){
				this.insertFace(e);
				return;
			}
			e.preventDefault();
			/*$('.facemian').unbind('touchend mouseup',this.insertFace=this.insertFace.delegate(this));
			if(s>100 && this.isClick==false){
				this.tableLeft();
				this.isClick==true
			}else if(s<-100 && this.isClick==false){
				this.tableRight();
				this.isClick==true
			}else if(this.isClick==true){
				$('.facemian').bind('touchend mouseup',this.insertFace=this.insertFace.delegate(this));
				return;
			}*/
		},
		onTableMove : function(e){
			if (!this.isDrag) return;
			if(this.isDrag){this.isClick=false;}		
			if(e.targetTouches!=undefined){
				this.endX = e.targetTouches[0].pageX;
			}else{
				this.endX=e.pageX;
			}
			e.preventDefault();
		},
		destroy : function(){
			this.callSuper();
		}
		
	});
	
});
