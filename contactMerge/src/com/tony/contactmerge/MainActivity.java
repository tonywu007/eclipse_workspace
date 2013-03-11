package com.tony.contactmerge;

import java.util.ArrayList;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Thread() {
		public void run(){
			getContacts();
		}
		}.start();		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	final static String TAG="contactMergeA";
	
 public static final String[] PROJECTION_CONTACTS = { Contacts._ID,
		Contacts.PHOTO_ID, Contacts.IN_VISIBLE_GROUP,
		Contacts.HAS_PHONE_NUMBER, Contacts.DISPLAY_NAME,
		Contacts.CUSTOM_RINGTONE }; 
 
 private static int[] getColumnIndexs(String[] projections, Cursor c) {
	 int[] ret = new int[projections.length];
	 for (int i = 0; i < projections.length; i++) {
         ret[i] = c.getColumnIndex(projections[i]);
	 }
     return ret;
 } 
 
 void prt(String str){
	 if(str!=null)
	 for(int i=0;i<  str.length();++i){
		 int k =str.charAt(i);
		 Log.d(TAG,""+k);
	 }
 }
	private void getContacts() {
		Log.d(TAG,"enter getContacts");
		Cursor cursorContact = null;
		ContentResolver cr=getBaseContext().getContentResolver();
		
		try {
		cursorContact = cr.query(ContactsContract.Contacts.CONTENT_URI,
		         PROJECTION_CONTACTS, Contacts.IN_VISIBLE_GROUP + "=1",
		         null, null);
		Log.d(TAG,"中文");
		Log.e(TAG, "联系人 = " + cursorContact.getCount());
		int[] indexs = getColumnIndexs(PROJECTION_CONTACTS, cursorContact);
		if(cursorContact != null && cursorContact.moveToNext())
		while (cursorContact.moveToNext()) {
		Log.e(TAG, "------------------------------------");
		for (int i = 0; i < PROJECTION_CONTACTS.length; i++) {
			String value = cursorContact.getString(indexs[i]);
			prt(value);
			Log.e(TAG, PROJECTION_CONTACTS[i] + "=" + value);
			}
		}
		} catch (Exception e) {
		Log.e(TAG, e.toString());
		} finally {
		if (cursorContact != null) {
		cursorContact.close();
		}else Log.e(TAG,"cursor is null");
		}
		Log.d(TAG,"leave getContacts");
	}

	 // phone
	 private static final String[] PROJECTION_PHONENUMBER_CONTACT = {
	 Phone.NUMBER, Phone.TYPE, Phone.LABEL };
	 /* DISPLAY_NAME唯一性 */
	 private static final String[] PROJECTION_DISPLAYNAME_CONTACT = { StructuredName.DISPLAY_NAME };
	 // Email
	 private static final String[] PROJECTION_EAMIL_CONTACT = { Email.DATA1,
	 Email.TYPE, Email.LABEL };
	 // IM
	 private static final String[] PROJECTION_IM_CONTACT = new String[] {
	 Im.DATA, Im.TYPE, Im.LABEL, Im.PROTOCOL };
	 // address
	 private static final String[] PROJECTION_ADDRESS_CONTACT = new String[] {
	 StructuredPostal.STREET, StructuredPostal.CITY,
	 StructuredPostal.REGION, StructuredPostal.POSTCODE,
	 StructuredPostal.COUNTRY, StructuredPostal.TYPE,
	 StructuredPostal.LABEL, StructuredPostal.POBOX,
	 StructuredPostal.NEIGHBORHOOD, };
	 // Organization
	 private static final String[] PROJECTION_ORGANIZATION_CONTACT = new String[] {
	 Organization.COMPANY, Organization.TYPE, Organization.LABEL,
	 Organization.TITLE };
	 // note
	 private static final String[] PROJECTION_NOTES_CONTACT = new String[] { Note.NOTE };
	 // nickname
	 private static final String[] PROJECTION_NICKNAMES_CONTACT = new String[] {
	 Nickname.NAME, Nickname.TYPE, Nickname.LABEL };
	 // website
	 private static final String[] PROJECTION_WEBSITES_CONTACT = new String[] {
	 Website.URL, Website.TYPE, Website.LABEL };
	
	 /**
	 * 功能：根据contactId查询联系人详细
	 *
	 * 在android.provider.ContactsContract.Data表里查询
	 * */
	 public static void _getContactByContactId(ContentResolver cr,
	 String contactId) {
	 Cursor c = null;
	
	 c = cr.query(Data.CONTENT_URI, null, Data.CONTACT_ID + "=?",
	 new String[] { contactId }, null);
	
	 String mimeType = null;
	 String[] contentValue = null;
	
	 ArrayList<String[]> displayNameList = new ArrayList<String[]>();// 存显示名
	 ArrayList<String[]> phoneList = new ArrayList<String[]>();// 存电话号码，可多个
	 ArrayList<String[]> emailList = new ArrayList<String[]>();// 存Email，可多个
	 ArrayList<String[]> imList = new ArrayList<String[]>();// 存im，可多个
	 ArrayList<String[]> postalList = new ArrayList<String[]>();// 存postal地址，可多个
	 ArrayList<String[]> organizationList = new ArrayList<String[]>();// 存organization组织，可多个
	 ArrayList<String[]> noteList = new ArrayList<String[]>();// 存note备注
	 ArrayList<String[]> nicknameList = new ArrayList<String[]>();// 存Nickname昵称
	 ArrayList<String[]> websiteList = new ArrayList<String[]>();// 存Website网站
	
	 while (c.moveToNext()) {
	 // 根据mimeType分类信息
	 mimeType = c.getString(c.getColumnIndex(Data.MIMETYPE));
	 if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_DISPLAYNAME_CONTACT);
	 displayNameList.add(contentValue);
	 } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 // 每个contentValue存一类PROJECTION_PHONENUMBER_CONTACT数据
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_PHONENUMBER_CONTACT);
	 phoneList.add(contentValue);
	 } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_EAMIL_CONTACT);
	 emailList.add(contentValue);
	 } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_IM_CONTACT);
	 imList.add(contentValue);
	 } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_ADDRESS_CONTACT);
	 postalList.add(contentValue);
	 } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_ORGANIZATION_CONTACT);
	 organizationList.add(contentValue);
	 } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_NOTES_CONTACT);
	 noteList.add(contentValue);
	 } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_NICKNAMES_CONTACT);
	 nicknameList.add(contentValue);
	 } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
	 contentValue = getStringInContactCursor(c,
	 PROJECTION_WEBSITES_CONTACT);
	 websiteList.add(contentValue);
	 }
	 }
	 c.close(); 
	 // log
	 logContactsDetails("displayName",PROJECTION_DISPLAYNAME_CONTACT, displayNameList);
	 logContactsDetails("phoneNumber",PROJECTION_PHONENUMBER_CONTACT, phoneList);
	 logContactsDetails("Email", PROJECTION_EAMIL_CONTACT,emailList);
	 logContactsDetails("IM", PROJECTION_IM_CONTACT, imList);
	 logContactsDetails("Address", PROJECTION_ADDRESS_CONTACT,postalList);
	 logContactsDetails("Organization",PROJECTION_ORGANIZATION_CONTACT, organizationList);
	 logContactsDetails("Note", PROJECTION_NOTES_CONTACT, noteList);
	 logContactsDetails("NickName", PROJECTION_NICKNAMES_CONTACT,nicknameList);
	 logContactsDetails("WebSit", PROJECTION_WEBSITES_CONTACT,websiteList);
	 } 
	
	public static String[] getStringInContactCursor(Cursor c,
			   String[] projection) {
			   String[] contentValue = new String[projection.length];
			   for (int i = 0; i < contentValue.length; i++) {
			   String value = c.getString(c.getColumnIndex(projection[i]));
			   if (value == null) {
			   contentValue[i] = "";
			   } else {
			   contentValue[i] = value;
			  }
			  }
			  return contentValue;
			  }

			  public static void logContactsDetails(String title, String[] projection,
			  ArrayList<String[]> data) {
				  
		      Log.d(TAG,"title="+title);
			  Log.e(TAG, "$--------x" + title + "y--------");

			  for (int i = 0; i < data.size(); i++) {
			  for (int j = 0; j < data.get(i).length; j++) {
			  Log.e(TAG, projection[j] + "=" + data.get(i)[j]);
			  }
			  }
			  } 	
}
