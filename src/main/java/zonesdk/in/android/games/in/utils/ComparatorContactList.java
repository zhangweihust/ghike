package zonesdk.in.android.games.in.utils;
import java.util.Comparator;

import zonesdk.in.android.games.in.widget.GetContactList.MyContacts;


/**
 * @author raomengwen
 * @version $1.0, 2012-12-13 2012-12-13
 * @since JDK5
 */
public class ComparatorContactList implements Comparator<MyContacts>{
	@Override
	public int compare(MyContacts arg0, MyContacts arg1) {

	   // first compare with ishike  then  compare with contactname
	  int arg0IsHike = arg0.isgHike?1:0;
	  int arg1IsHike = arg1.isgHike?1:0;
	  if(arg0IsHike>arg1IsHike){
		  return -1 ; 
	  }else  if(arg0IsHike<arg1IsHike){
		  return 1;
	  }
	  String  str0 =arg0.userName.toLowerCase();
	  String  str1 =arg1.userName.toLowerCase();
	  int flag=str0.compareTo(str1);
	  return flag;
	}
}