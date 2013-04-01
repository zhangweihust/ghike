package in.android.games.in.widget;


import java.util.ArrayList;

import zonesdk.in.android.games.in.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;



public class FaceAdapter extends BaseAdapter{
	private Context context;
	private ArrayList<Integer> lstDate;


	public static final int SIZE = 28;
    public FaceAdapter(int[] list,Context context, int page) {

        this.context=context;
        lstDate = new ArrayList<Integer>();
        
		int i = page * SIZE;
		int iEnd = i + SIZE;
		
		while ((i < list.length) && (i < iEnd)) {
			lstDate.add(list[i]);
			i++;
		}
        
        
    }
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		
	
		return lstDate.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lstDate.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return lstDate.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int faceRes = lstDate.get(position);
		
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.face_item, null);
			convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
			holder = new ViewHolder();
			holder.faceView = (ImageView) convertView.findViewById(R.id.face_item_img);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

//		int count = lstDate.size();
//		if(position == count - 1){
//			 ImageView image = new ImageView(context);   
//		        image.setPadding(20, 20, 20, 20);//
//		        image.setImageDrawable(context.getResources().getDrawable(R.drawable.head_bt));   
//		        convertView=image;
//		} else {
		Drawable faceIcon = context.getResources().getDrawable(faceRes);
		faceIcon.setBounds(0, 0, 45, 45);
		holder.faceView.setImageDrawable(faceIcon);

			
//		}
       
        return convertView; 
	}
	
	static class ViewHolder {
		ImageView faceView;
	}
	
}