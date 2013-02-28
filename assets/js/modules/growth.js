(function (){
	/*hike.registerAction('growth.newbie', function(dataset, ctx){
		ctx.get('growth/newbie', {}, function(data) {
				ctx.forward('growth.popNewbie',data);			
		})
	});
	
	hike.registerAction('growth.popNewbie', function(dataset, ctx){*/
	hike.registerAction('growth.newbie', function(dataset, ctx){
		var userName = dataset['userName'];
		var friendCount = dataset['friendCount'];
		var gameCount = dataset['gameCount'];
		var growLevel = dataset['growLevel'];
		var mark = dataset['mark'];
		var flagNewbieMission = dataset['flagNewbieMission'];
		var html = '';
		if(friendCount >=10){
			html += '<div class="mission finished" ><div class="btn"><span class="logo"></span></div><span class="info">Add 10 friends</span></div>';
		}else if(friendCount ==-1){
			html +='';
		}else{
			html += '<div class="mission"><div class="btn" data-widget="Button" data-forward-action="home.to-search" data-action="growth.closeDialogAndForward"><span class="logo"></span></div><span class="info">Add 10 friends</span></div>';
		}
		
		if(gameCount >=5){
			html += '<div class="mission finished" ><div class="btn"><span class="logo"></span></div><span class="info">Play 5 games</span></div>';
		}else if(gameCount ==-1){
			html +='';
		}else{
			html += '<div class="mission"><div class="btn" data-widget="Button" data-forward-action="games.gamelist" data-action="growth.closeDialogAndForward" data-game-type="all"><span class="logo"></span></div><span class="info">Play 5 games</span></div>';
		}
		
		if(flagNewbieMission == "true"){
			html += '<div class="mission finished" ><div class="btn"><span class="logo"></span></div><span class="info">Enrich Profile</span></div>';
		}else if(flagNewbieMission ==-1){
			html +='';
		}else{
			html += '<div class="mission"><div class="btn" data-widget="Button" data-forward-action="profile.personalinfo" data-action="growth.closeDialogAndForward" ><span class="logo"></span></div><span class="info">Enrich Profile</span></div>';
		}
		var title='';
		var but,func;
		if(growLevel == 1 && (friendCount >=10 && gameCount >=5 && flagNewbieMission == "true")){
			title += '<div class="msg">Hi,'+userName+'!<br/><em>Congratulations! You have got 28 EXP.</em></div>';
			but = 'Back';
			func = 'growth.completeNewbie';
		    }else if(growLevel == 1 && mark == "0"){
		    title += '<div class="msg">Hi,'+userName+'!<br/><em>Congratulations! You\'ve got to Lv1 and Newbie Mission will be closed.</em></div>';	
		    html = '';
		    but = 'Back';
			func = 'growth.completeNewbie';
		    }else{
			title += '<div class="msg">Hi,'+userName+'! Enjoy zone from here,and you will get <em>28</em> EXP.</div>';	
			but = 'Try it Later';
		    }
		
		var config = {
 			title : 'Newbie Mission',
 			buttons:[{text:but,fn:func}],
 	  		content:'<div class="growth newbie">'
 	  				+ title
 	  				+'<div class="missions">'
 	  				+ html
 	  				+'</div>'
 	  				+'</div>'
 		};
 		hike.ui.showDialog(config);
	});
	
	hike.registerAction('growth.closeDialogAndForward', function(dataset, ctx){
		hike.ui.closeDialog();
		ctx.forward(dataset['forwardAction'],dataset);		
	});
	
	hike.registerAction('growth.completeNewbie', function(dataset, ctx){
			hike.ui.closeDialog();
			$(".controlpanel .newbie").hide();
			ctx.get('growth/newbie', {}, function(data) {
		})
			
	});
	
	hike.registerAction('growth.signin', function(dataset, ctx){
		var config = {
 			title : 'Sign in Today',
 	  		content:'<div class="growth signin">'
 	  				+'<div class="msg">Sign in every day and get more experience point.</div>'
 	  				+'<div class="signs">'
 	  				+'<div class="sign tomorrow"><span class="day">Next<br/>DAY</span><span class="info"><em></em>10 EXP per day after the 3rd day.</span></div>'
 	  				+'<div class="sign today"><span class="day">2nd<br/>DAY</span><span class="info"><em></em>5 EXP</span></div>'
 	  				+'<div class="sign yesterday"><span class="day">1st<br/>DAY</span><span class="info"><em></em>2 EXP</span></div>'
 	  				+'</div>'
 	  				+'<div class="btn" data-action="">Sign in</div>'
 	  				+'</div>'
 		};
 		hike.ui.showDialog(config);
	});
	
	hike.registerAction('growth.signin-success', function(dataset, ctx){
		var config = {
 			title : 'Sign in Today',
 			buttons:[{text:'Back'}],
 	  		content:'<div class="growth">'
 	  				+'<div class="msg"><em>Sign in successfully!</em><br/>You have got <em>2</em> EXP for today.<br/>Sign in tomorrow and you will get another 5 EXP.</div>'
 	  				+'</div>'
 		};
 		hike.ui.showDialog(config);
	});
})();
