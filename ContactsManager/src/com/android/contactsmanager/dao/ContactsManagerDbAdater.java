package com.android.contactsmanager.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.contactsmanager.bean.MyContacts;

public class ContactsManagerDbAdater {
	public static final String TAG="ContactsManagerDbAdater";
	public static final String DATABASE_NAME="contactsmanager.db";
	public static final int DATABASE_VERSON=3;
	public static final String TABLE_CONTACTS="contacts";
	public static final String TABLE_GROUPS="groups";
	public static final String TABLECONTACTS=
		"create table contacts("+
		"_id INTEGER PRIMARY KEY,"+//rowID
		"name TEXT  NOT NULL,"+ //����
		"contactIcon BLOB,"+ //��ϵ��ͼ��
		"telPhone TEXT NOT NULL,"+ //�绰����
		"groupName TEXT,"+ //��������
		"birthday TEXT,"+ //����
		"address TEXT,"+ //��ַ
		"email TEXT NOT NULL,"+ //����
		"description TEXT NOT NULL,"+ //��������
		"createTime TEXT,"+ //����ʱ��
		"modifyTime TEXT"+ //�޸�ʱ��
		");";
	public static final String TABLEGROUPS=
		"create table groups("+
		"_id INTEGER PRIMARY KEY,"+ //rowId
		"groupName TEXT UNIQUE NOT NULL,"+ //����
		"createTime TEXT,"+ //����ʱ��
		"modifyTime TEXT"+ //�޸�ʱ��
		");";
	
	private Context context;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase mSQLiteDatabase = null;
	
	
	
	public ContactsManagerDbAdater(Context context){
		this.context=context;
	}
	
	public void open(){
		dbHelper=new DatabaseHelper(context);
		mSQLiteDatabase=dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>> CLOSE <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSON);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("TAG", "create table start...");
			
			db.execSQL(TABLECONTACTS);
			db.execSQL(TABLEGROUPS);
			//������ʱ����
			String tempGroups[]={"����","����","ͬѧ","ͬ��","android������","����","������ֲ�"};
			for(int i=0;i<tempGroups.length;i++){
				String sql="insert into groups values(?,?,null,null)";
				Object[] bindArgs={i+1,tempGroups[i]};
				db.execSQL(sql,bindArgs);
			}
			
			String tempName[]={
					"android",
					"google",
					"windows mobile",
					"microsoft",
					"symbian",
					"nokia",
					"bada",
					"sumsung",
					"IBM",
					"QQ"
			};
			
			Random random=new Random();
			int index=0;
			//������ʱ����ϵ��
			for(int i=0;i<10;i++){
				String sql="insert into contacts values(?,?,null,'15927614509',?,'1986-11-03','����','lhb@163.com',?,null,null)";
				index=random.nextInt(tempGroups.length);
				Object[] bindArgs={i+1,tempName[i],tempGroups[index],"this is a scroll text,you can move cursor to here move it..."};
				db.execSQL(sql, bindArgs);
			}
			
			Log.i("TAG", "create table over...");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("TAG", "contactsmanager.db Upgrade...");
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_CONTACTS);
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_GROUPS);
			onCreate(db);
			
		}
		
	}
	
	
	//��ͷ��ת����byte[]�Ա��ܽ�ͼƬ�浽���ݿ�
	public byte[] getBitmapByte(Bitmap bitmap){
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "transform byte exception");
		}
		return out.toByteArray();
	}
	
	
	//table contacts
	public static final String contacts_id="_id";
	public static final String contacts_icon="contactIcon";
	public static final String contacts_name="name";
	public static final String contacts_description="description";
	public static final String contacts_telPhone="telPhone";
	public static final String contacts_email="email";
	String contactProjection[]={
				contacts_id,
				contacts_icon,
				contacts_name,
				contacts_description,
				contacts_telPhone,
				contacts_email
			}; 
	
	//table groups
	public static final String groups_id="_id";
	public static final String groups_groupName="groupName";
	String groupsProjection[]={
			groups_id,
			groups_groupName
		};
	
	//����������
	public Cursor getAllGroups(){
		return mSQLiteDatabase.query(
				TABLE_GROUPS, 
				groupsProjection, 
				null, null, null, null, null);
		
	}
	//�õ�����������г�Ա
	public Cursor getContactsByGroupName(String groupName){
		return mSQLiteDatabase.query(
				TABLE_CONTACTS, 
				contactProjection, 
				"groupName='"+groupName+"'", 
				null, null, null, null);
	}
	//ͳ�Ƹ����������
	public int getCountContactByGroupName(String groupName){
		int count=0;
		String sql="select count(*) from contacts where groupName='"+groupName+"'";
		Cursor cursor=mSQLiteDatabase.rawQuery(sql, null);
		if(cursor.moveToFirst()){
			count=cursor.getInt(0);
		}
		cursor.close();
		return count;
	}
	
	//ͬ������contacts��groupName�ֶ���Ϣ
	public void updateSyncData(String sql,Object[] Args){
		mSQLiteDatabase.execSQL(sql, Args);
	}
	
	//��ѯ��ϵ�����ĸ���
	public String checkContactGroup(String sql,String selectionArgs[]){
		String groupName="";
		Cursor cursor=mSQLiteDatabase.rawQuery(sql, selectionArgs);
		if(cursor.moveToFirst()){
			groupName=cursor.getString(0);
		}
		cursor.close();
		return groupName;
	}
	
	//��ѯ
	public Cursor getCursorBySql(String sql,String selectionArgs[]){
		return mSQLiteDatabase.rawQuery(sql, selectionArgs);
	}
	
	//���һ����
	public long inserDataToGroups(String groupName){
		
		String formatTime=getSysNowTime();
		ContentValues content=new ContentValues();
		content.put(groups_groupName, groupName);
		content.put("createTime", formatTime);
		content.put("modifyTime", formatTime);
		return mSQLiteDatabase.insert(TABLE_GROUPS, null, content);
		
	}
	
	//ɾ��һ����
	public int deleteDataFromGroups(String groupName){
		return mSQLiteDatabase.delete(TABLE_GROUPS, "groupName='"+groupName+"'", null);
	}
	
	//����һ����
	public int updateDataToGroups(String newgroupName,String oldgroupName){
		String formatTime=getSysNowTime();
		ContentValues content=new ContentValues();
		content.put(groups_groupName, newgroupName);
		content.put("modifyTime", formatTime);
		return mSQLiteDatabase.update(TABLE_GROUPS, content, "groupName='"+oldgroupName+"'", null);
	}
	
	//���һ����ϵ��
	public long inserDataToContacts(MyContacts contactInfo){
			String formatTime=getSysNowTime();
			ContentValues content=new ContentValues();
			content.put("name", contactInfo.getName());
			content.put("birthday", contactInfo.getBirthday());
			content.put("address", contactInfo.getAddress());
			content.put("telPhone", contactInfo.getTelPhone());
			content.put("email", contactInfo.getEmail());
			content.put("contactIcon", contactInfo.getContactIcon());
			content.put("description", contactInfo.getDescription());
			content.put("groupName", contactInfo.getGroupName());
			content.put("createTime", formatTime);
			content.put("modifyTime", formatTime);
			return mSQLiteDatabase.insert(TABLE_CONTACTS, null, content);
			
		}
		
	//ɾ��һ����ϵ��
	public int deleteDataFromContacts(String name){
		return mSQLiteDatabase.delete(TABLE_CONTACTS, "name='"+name+"'", null);
	}
	
	//������ϵ��
	/**
	 * 
	 * contactInfo:�û����±༭����ϵ����Ϣ
	 * name:�༭�����ĸ���ϵ��
	 */
	public int updateDataToContacts(MyContacts contactInfo,String name){
		String formatTime=getSysNowTime();
		ContentValues content=new ContentValues();
		content.put("name", contactInfo.getName());
		content.put("birthday", contactInfo.getBirthday());
		content.put("address", contactInfo.getAddress());
		content.put("telPhone", contactInfo.getTelPhone());
		content.put("email", contactInfo.getEmail());
		content.put("contactIcon", contactInfo.getContactIcon());
		content.put("description", contactInfo.getDescription());
		content.put("groupName", contactInfo.getGroupName());
		content.put("modifyTime", formatTime);
		System.out.println("update success");
		System.out.println(name);
		return mSQLiteDatabase.update(TABLE_CONTACTS, content, "name=?", new String[]{name});
	}
	
	//get sysTime
	public String getSysNowTime(){
		Date now=new Date();
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
		String formatTime=format.format(now);
		return formatTime;
	}
}
