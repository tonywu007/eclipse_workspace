package com.android.contactsmanager;

import java.util.Random;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.android.contactsmanager.bean.MyConstant;
import com.android.contactsmanager.dao.ContactsManagerDbAdater;

public class ContactsManager extends ExpandableListActivity {
	
	public static final String TAG="ContactsManager";
	private ContactsManagerDbAdater contactsManagerDbAdapter;
	int groupNameIndex;
	private MyCursrTreeAdapter myCursorTreeAdapter;
	
	View view;
	PopupWindow pop;
	
	Button btnSms;
	Button btnEmail;
	Button btnCall;
	
	//缓存除了所选联系人所在组的所有组，用在移动联系人上
	String groups[];
	
	//缓存用户所在的组,用在移动联系人上
	String mygroupName;
	
	//长按分组上的 菜单
	public static final int MENU_GROUP_ADD=Menu.FIRST;
	public static final int MENU_GROUP_DELETE=Menu.FIRST+1;
	public static final int MENU_GROUP_MODIFY=Menu.FIRST+2;
	public static final int MENU_GROUP_ADDCONTACT=Menu.FIRST+3;
	
	//长按联系人菜单
	public static final int MENU_CONTACTS_DELETE=Menu.FIRST;
	public static final int MENU_CONTACTS_MODIFY=Menu.FIRST+1;
	public static final int MENU_CONTACTS_MOVE=Menu.FIRST+2;
	
	//联系人各个字段索引
	private static final int icon_index=1;
	private static final int name_index=2;
	private static final int description_index=3;
	private static final int telPhone_index=4;
	private static final int email_index=5;
	
	//组上groupName字段索引
	private static final int groupName_index=1;
	
	Cursor groupCursor;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.main);
        getExpandableListView().setBackgroundResource(R.drawable.default_bg);
        registerForContextMenu(getExpandableListView());
        
        contactsManagerDbAdapter=new ContactsManagerDbAdater(this);
        contactsManagerDbAdapter.open();
        
        initMyAdapter();
        
        initPopupWindow();
        
        getExpandableListView().setCacheColorHint(0);//拖动时避免出现黑色
        getExpandableListView().setDivider(null);//去掉每项下面的黑线(分割线)
        //自定义下拉图标
        getExpandableListView().setGroupIndicator(getResources().getDrawable(R.drawable.expander_ic_folder));
    }
    
    private void initPopupWindow()
	{
		view = this.getLayoutInflater().inflate(R.layout.popup_window, null);
		pop = new PopupWindow(view, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setOutsideTouchable(true);
		btnSms=(Button)view.findViewById(R.id.btnSms);
		btnEmail=(Button)view.findViewById(R.id.btnEmail);
		btnCall=(Button)view.findViewById(R.id.btnCall);
	}
    
    //给适配器赋值，刷新界面的时候也会用到
    public void initMyAdapter(){
    	groupCursor=contactsManagerDbAdapter.getAllGroups();
        startManagingCursor(groupCursor);
        //get the groupName column index
        groupNameIndex=groupCursor.getColumnIndexOrThrow("groupName");
        
        //set my adapter
        myCursorTreeAdapter=new MyCursrTreeAdapter(
        		groupCursor,
        		this,
        		true
        		);
        setListAdapter(myCursorTreeAdapter);
    }
   

    
    
    public class MyCursrTreeAdapter extends CursorTreeAdapter {

		public MyCursrTreeAdapter(Cursor cursor, Context context,
				boolean autoRequery) {
			super(cursor, context, autoRequery);
		}

		

		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			// TODO Auto-generated method stub
			Log.v(TAG, "bindGroupView");
			TextView groupName=(TextView)view.findViewById(R.id.groupName);
			String group=cursor.getString(groupName_index);
			groupName.setText(group);
			
			TextView groupCount=(TextView)view.findViewById(R.id.groupCount);
			int count=contactsManagerDbAdapter.getCountContactByGroupName(group);
			groupCount.setText("["+count+"]");
		}
		
		@Override
		protected View newGroupView(Context context, Cursor cursor,
				boolean isExpanded, ViewGroup parent) {
			Log.v(TAG, "newGroupView");
			LayoutInflater inflate=LayoutInflater.from(ContactsManager.this);
			View view=inflate.inflate(R.layout.grouplayout, null);
			
			bindGroupView(view, context, cursor, isExpanded);
			
			return view;
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			Log.v(TAG, "getChildrenCursor");
			String groupName=groupCursor.getString(groupName_index);//得到当前的组名
			Cursor childCursor=contactsManagerDbAdapter.getContactsByGroupName(groupName);
			startManagingCursor(childCursor);
			return childCursor;
		}

		@Override
		protected View newChildView(Context context, Cursor cursor,
				boolean isLastChild, ViewGroup parent) {
			Log.v(TAG, "newChildView");
			LayoutInflater inflate=LayoutInflater.from(ContactsManager.this);
			View view=inflate.inflate(R.layout.childlayout, null);
			
			bindChildView(view, context, cursor, isLastChild);
			
			return view;
		}
		
		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {
			// TODO Auto-generated method stub
			Log.v(TAG, "bindChildView");
			ImageView contactIcon=(ImageView)view.findViewById(R.id.contactIcon);
			contactIcon.setImageBitmap(getBitmapFromByte(cursor.getBlob(icon_index)));
			
			TextView name=(TextView)view.findViewById(R.id.name);
			name.setText(cursor.getString(name_index));
			
			TextView description=(TextView)view.findViewById(R.id.description);
			description.setTextKeepState(cursor.getString(description_index));
			
			final String phoneNumber=cursor.getString(telPhone_index);
			final String email=cursor.getString(email_index);
			
			ImageView mycursor=(ImageView)view.findViewById(R.id.myCursor);
			mycursor.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//showToast("点击了图片");
					if(pop.isShowing())
					{
						pop.dismiss();
					}
					else
					{ 
						pop.showAsDropDown(v); 
						
						btnSms.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								pop.dismiss();
								Uri uri=Uri.parse("smsto:"+phoneNumber);
								Intent it = new Intent(Intent.ACTION_SENDTO, uri);   
								it.putExtra("sms_body", "呵呵！好久不见");   
								startActivity(it);  
							}
						});
						
						btnEmail.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								pop.dismiss();
								Uri uri = Uri.parse("mailto:"+email);
								Intent it = new Intent(Intent.ACTION_SENDTO, uri);
								startActivity(it);
							}
						});
						
						btnCall.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								pop.dismiss();
								Uri uri = Uri.parse("tel:"+phoneNumber);
								Intent it = new Intent(Intent.ACTION_DIAL, uri);  
								startActivity(it);
							}
						});
					}
				}
			});
		}
    }
    
  //得到存储在数据库中的头像
	public Bitmap getBitmapFromByte(byte[] temp){
		if(temp!=null){
			Bitmap bitmap=BitmapFactory.decodeByteArray(temp, 0, temp.length);
			return bitmap;
		}else{
			return getRandomIcon();
		}
	}
	
	//得到随机图片
	public Bitmap getRandomIcon(){
		Integer allIcon[]={
				R.drawable.h001,
				R.drawable.h002,
				R.drawable.h003,
				R.drawable.h004,
				R.drawable.h005,
				R.drawable.h006,
				R.drawable.h007,
				R.drawable.h008,
				R.drawable.h009,
				R.drawable.h010,
				R.drawable.h011,
				R.drawable.h012,
				R.drawable.h013,
				R.drawable.h014,
				R.drawable.h015,
				R.drawable.h016,
				R.drawable.h017,
				R.drawable.h018,
				R.drawable.h019,
				R.drawable.h020,
			};
		Random random=new Random();
		int index=random.nextInt(20);
		Resources res=getResources();
		Bitmap bmp=BitmapFactory.decodeResource(res,allIcon[index]);
		return bmp;
	}
	
	//弹出提示信息
	public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
    	ExpandableListContextMenuInfo info=(ExpandableListContextMenuInfo)menuInfo;
    	
    	int type = ExpandableListView.getPackedPositionType(info.packedPosition);
    	if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP){//在组上长按
    		String title=((TextView)info.targetView.findViewById(R.id.groupName)).getText().toString();
    		menu.setHeaderTitle(title);
    		menu.add(0, MENU_GROUP_ADD, 0, "添加分组");
    		menu.add(0, MENU_GROUP_DELETE, 0, "删除分组");
    		menu.add(0, MENU_GROUP_MODIFY, 0, "重命名");
    		menu.add(0, MENU_GROUP_ADDCONTACT, 0, "添加联系人");
    		
    	}else if(type == ExpandableListView.PACKED_POSITION_TYPE_CHILD){//在联系人上长按
    		String title=((TextView)info.targetView.findViewById(R.id.name)).getText().toString();
    		Drawable icon=((ImageView)info.targetView.findViewById(R.id.contactIcon)).getDrawable();
    		menu.setHeaderTitle(title);
    		menu.setHeaderIcon(icon);
    		menu.add(0, MENU_CONTACTS_DELETE, 0, "删除联系人");
    		menu.add(0, MENU_CONTACTS_MODIFY, 0, "编辑联系人");
    		menu.add(0, MENU_CONTACTS_MOVE, 0, "移动联系人到...");
    	}
    	
	}
    
	public boolean onContextItemSelected(MenuItem item) {
		
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
        	final String name=((TextView)info.targetView.findViewById(R.id.name)).getText().toString();
            switch(item.getItemId()){
            case MENU_CONTACTS_DELETE :{
            	AlertDialog.Builder builder=new AlertDialog.Builder(this);
            	builder.setTitle("确定要删除联系人吗？");
            	builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
							contactsManagerDbAdapter.deleteDataFromContacts(name);
							initMyAdapter();
							showToast("删除成功");					
						}
				});
            	builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
							dialog.dismiss();					
						}
				});
            	builder.show();
            	break;
            }
            case MENU_CONTACTS_MODIFY :{
            	Intent intent=new Intent();
            	intent.putExtra("name", name);
            	intent.setAction(Intent.ACTION_EDIT);
            	intent.setDataAndType(Uri.parse(MyConstant.CONTENT_URI), MyConstant.CONTENT_TYPE_EDIT);
            	startActivity(intent);
            	break;
            }
            case MENU_CONTACTS_MOVE :
            	createMoveContactDialog(name).show();
            	break;
    	}
            
            return true;
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
        	String groupName=((TextView)info.targetView.findViewById(R.id.groupName)).getText().toString();
        	System.out.println(groupName);
        	switch(item.getItemId()){
	            case MENU_GROUP_ADD :
	            	createDialog("addGroup",groupName).show();
	            	break;
	            case MENU_GROUP_DELETE :
	            	createDialog("deleteGroup",groupName).show();
	            	break;
	            case MENU_GROUP_MODIFY :
	            	createDialog("modifyGroup",groupName).show();
	            	break;
	            case MENU_GROUP_ADDCONTACT :{
	            	Intent intent=new Intent();
	            	intent.setAction(Intent.ACTION_INSERT);
	            	intent.setDataAndType(Uri.parse(MyConstant.CONTENT_URI), MyConstant.CONTENT_TYPE_INSERT);
	            	startActivity(intent);
	            	break;
	            }
        	}
        	
        	return true;
        }
        return false;
        
        
	}
	
	private Dialog createMoveContactDialog(final String name){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("移动联系人到...");
		builder.setSingleChoiceItems(getSpecAllGroup(name), -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//得到用户要移动到的组
				String newgroupName=groups[which];
				String sql="update contacts set groupName=? where groupName=? and name=?";
				Object[] Args={newgroupName,mygroupName,name};
				contactsManagerDbAdapter.updateSyncData(sql, Args);
				initMyAdapter();
				showToast("成功移动联系人到"+newgroupName);
				dialog.dismiss();
			}
		});
		return builder.create();
	}
	
	private String[] getSpecAllGroup(String name){
		String sql="select groupName from contacts where name=?";
		String selectionArgs[]={name};
		mygroupName=contactsManagerDbAdapter.checkContactGroup(sql, selectionArgs);
		Cursor cursor=contactsManagerDbAdapter.getAllGroups();
		int count=cursor.getCount()-1;
		groups=new String[count];
		int i=0;
		while(cursor.moveToNext()){
			String newgroupName=cursor.getString(1);
			if(!newgroupName.equals(mygroupName)){
				groups[i]=newgroupName;
				i++;
			}
		}
		cursor.close();
		return groups;
	}
	

	
	private Dialog createDialog(String msg,final String groupName) {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		if(msg.equals("addGroup")){
			final EditText content=new EditText(this);
			builder.setTitle("添加组");
			builder.setView(content);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//添加新的组到数据库
					String groupName=content.getText().toString().trim();
					Cursor cursor=contactsManagerDbAdapter.getAllGroups();
					if(!groupName.equals("")){
						while(cursor.moveToNext()){
							if(cursor.getString(1).equals(groupName)){
								showToast(groupName+"已存在！");
								return;
							}
						}
						contactsManagerDbAdapter.inserDataToGroups(groupName);
						initMyAdapter();
						showToast("添加成功");
						System.out.println(">>>>>>>>>>>>>>add>>>>>>>>>>>>>>>>>>>>>>>>");
					}
				}
			});
			builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					dialog.dismiss();
				}
			});
			return builder.create();
		}
		if(msg.equals("deleteGroup")){
			builder.setTitle("确定要删除该组和该组内的所有联系人吗?");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					contactsManagerDbAdapter.deleteDataFromGroups(groupName);
					String sql="delete from contacts where groupName=?";
					Object Args[]={groupName};
					contactsManagerDbAdapter.updateSyncData(sql, Args);
					initMyAdapter();
					showToast("删除成功");
					System.out.println(">>>>>>>>>>>>>>>delete>>>>>>>>>>>>>>>>>>>>>");
				}
			});
			builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return builder.create();
		}
		if(msg.equals("modifyGroup")){
			final EditText content=new EditText(this);
			content.setText(groupName);
			builder.setTitle("请输入新的组名");
			builder.setView(content);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newgroupName=content.getText().toString().trim();
					Cursor cursor=contactsManagerDbAdapter.getAllGroups();
					if(!newgroupName.equals("")){
						while(cursor.moveToNext()){
							if(cursor.getString(1).equals(newgroupName)){
								if(!newgroupName.equals(groupName)){
									showToast(newgroupName+"已存在");
									return;
								}else{
									return;
								}
							}
						}
						contactsManagerDbAdapter.updateDataToGroups(newgroupName, groupName);
						String sql="update contacts set groupName=? where groupName=?";
						Object Args[]={newgroupName,groupName};
						contactsManagerDbAdapter.updateSyncData(sql, Args);
						initMyAdapter();
						showToast("修改成功");
						System.out.println(">>>>>>>>>>>>>>>update>>>>>>>>>>>>>>>>>>>>>");
					}
				}
			});
			builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return builder.create();
		}
		return null;
	}
	
	//处理键盘事件
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK ){
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("确定要退出程序?");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					finish();
				}
			});
			builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		}else{
		
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(contactsManagerDbAdapter!=null){
			contactsManagerDbAdapter.close();
			contactsManagerDbAdapter=null;
		}
	}
}