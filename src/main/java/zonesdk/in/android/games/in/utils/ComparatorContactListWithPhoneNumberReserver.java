package zonesdk.in.android.games.in.utils;

import java.util.Comparator;

import zonesdk.in.android.games.in.widget.GetContactList.MyContacts;



public class ComparatorContactListWithPhoneNumberReserver implements Comparator<MyContacts>{
	@Override
	public int compare(MyContacts arg1, MyContacts arg2){
		
		   // first compare with ishike  then  compare with phonenumber
/*		  int arg0IsHike = arg1.isgHike?1:0;
		  int arg1IsHike = arg2.isgHike?1:0;
		  if(arg0IsHike>arg1IsHike){
			  return -1 ; 
		  }else  if(arg0IsHike<arg1IsHike){
			  return 1;
		  }
		  */
		  
		return compareto(arg1.userPhoneNumber, arg2.userPhoneNumber);
		  
	}
	

	public static int compareto(String arg1, String arg2){
	  	  String  str1 =arg1.replace("-", "");
	  	  String  str2 =arg2.replace("-", "");
	  	  
	  	  int result = 0;
	  	  char A,B;
	  	  int length1 = str1.length();
	  	  int length2 = str2.length();
	  	  
	  	  if((length1==0) && (length2==0)){
	  		  return 0;
	  	  }else if((length1==0) && (length2!=0)){
	  		  return 1;
	  	  }else if((length1!=0) && (length2==0)){
	  		  return -1;
	  	  }else{
		  	  for(int i = 0; (i < length1) && (i< length2); i++){
		  		  A = str1.charAt(length1 - i -1);
		  		  B = str2.charAt(length2 - i - 1);
		  		  if (A > B){
		  			  result = 1;
		  			  return result;  //str1 > str2
		  			  
		  		  }
		  		  else if (A < B){
		  			  result = -1;
		  			  return result;  //str1 < str2
		  		  }
		  		  else{
		  			  continue; 			  
		  		  }
		  	  }
		  	  
		  	  return 0;
	  	  }
		
	}
	
  }
