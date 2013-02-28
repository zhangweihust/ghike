(function(){
	
	var getDateMap = function(mon){
		switch (mon) {
		case 1:
			return "Jan.";
			break;
		case 2:
			return "Feb.";
			break;
		case 3:
			return "March";
			break;
		case 4:
			return "April";
			break;
		case 5:
			return "May";
			break;
		case 6:
			return "June";
			break;
		case 7:
			return "July";
			break;
		case 8:
			return "Aug.";
			break;
		case 9:
			return "Sept.";
			break;
		case 10:
			return "Oct.";
			break;
		case 11:
			return "Nov.";
			break;
		case 12:
			return "Dec.";
			break;
		default: return "";
			break;
		}
	};
	
	hike.defineClass('hike.android.TimeUtil', {
		
		TimeUtil : function(){
		},
		
		reckonTime : function(time){
			if(null != time) {
				
				var d = new Date();
				var min = (d.getTime() - time) / (60 * 1000);
				min = min >> 0;
				if(min <= 0){
					return "Just now";
				}else if(min < 60){
					if(min==1){
						return min+" minute ago";
					}else{
						return min+" minutes ago";
					}
					
				}
				if(min < 1440){
					var iHour = (d.getTime() - time) / (60 * 60 * 1000);
					iHour = iHour >> 0;
					if(iHour==1){
						return iHour+" hour ago";
					}else{
						return iHour+" hours ago";
					}
					
				}else{
					d.setTime(time);

					var mon = d.getMonth()+1;
					var date = d.getDate();
					var hour = d.getHours();
					var minute = d.getMinutes();
					var dateStr = "";
					var hourStr = "";
					var minStr = "";
					if(date<10){
						dateStr = "0"+date;
					}else{
						dateStr = ""+date;
					}
					
					if(hour<10){
						hourStr = "0"+hour;
					}else{
						hourStr = ""+hour;
					}
					
					if(minute<10){
						minStr = "0"+minute;
					}else{
						minStr = ""+minute;
					}
					
					var month = getDateMap(mon);
					var result = month + " " + dateStr + " at " + hourStr + ":" + minStr;

					return result;
				}
			}
			return "";
		},
		timeOffset : function(lastLoadTime,offset){
			if(null != lastLoadTime) {
				var currentTime = new Date();
				var min = (currentTime.getTime() - lastLoadTime) / (60 * 1000);
				min = min >> 0;
				return min;
			}
			return -1;
		},
		serverTime : function(time){
			if(null != time) {
				var d = new Date();
				d.setTime(time);

				var mon = d.getMonth()+1;
				var date = d.getDate();
				var hour = d.getHours();
				var minute = d.getMinutes();
				var dateStr = "";
				var hourStr = "";
				var minStr = "";
				if(date<10){
					dateStr = "0"+date;
				}else{
					dateStr = ""+date;
				}
				
				if(hour<10){
					hourStr = "0"+hour;
				}else{
					hourStr = ""+hour;
				}
				
				if(minute<10){
					minStr = "0"+minute;
				}else{
					minStr = ""+minute;
				}
				
				var month = getDateMap(mon);
				var result = month + " " + dateStr + " at " + hourStr + ":" + minStr;

				return result;
			}
			return "";
		}
	});
	
})();

