package com.android.contactsmanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.android.contactsmanager.bean.MyContacts;
import com.android.contactsmanager.dao.ContactsManagerDbAdater;
import com.android.contactsmanager.ui.PhotoEditorView;

public class EditContact extends Activity {



	ContactsManagerDbAdater contactsManagerDbAdapter;

	public static final String TAG = "EditContact";

	// ������ʶ�������๦�ܵ�activity
	private static final int CAMERA_WITH_DATA = 3023;

	// ������ʶ����gallery��activity
	private static final int PHOTO_PICKED_WITH_DATA = 3021;

	// ���յ���Ƭ�洢λ��
	private static final File PHOTO_DIR = new File(Environment
			.getExternalStorageDirectory()
			+ "/DCIM/Camera");

	private File mCurrentPhotoFile;//��������յõ���ͼƬ
	
	private PhotoEditorView mEditor;//ͷ��
	
	Cursor contactInfoCursor;//��ϵ����Ϣ
	
	MyContacts contactAllInfoCache=null;//������ϵ��������Ϣ 
	
	ArrayAdapter<String> adapter;//����GroupSpinner����
	
	//�������
	private EditText name;//����
	private EditText phoneNumber;//����
	private Spinner groupSpinner;//��
	private Button birthdayButton;//����
	private EditText address;//סַ
	private EditText email;//����
	private EditText information;//�������� 
	
	private Button ok;//ȷ��
	private Button cancel;//ȡ��
	
	//�û�������Ϣ
	String _name;
	byte[] img;//ͷ������
	String _phoneNumber;
	String _groupSpinner;
	String _birthdayButton;
	String _address;
	String _email;
	String _information;
	
	//��ϵ����Ϣ������ֵ
	int index_name=1;
	int index_contactIcon=2;
	int index_telePhone=3;
	int index_groupName=4;
	int index_birthday=5;
	int index_address=6;
	int index_email=7;
	int index_description=8;
	
	
	//int selectedGroupNameIndex;//��¼�û�ѡ����������,���µ�activity����ʱ�õ�
	
	//activity��������״̬������״̬���߱༭״̬
	private static final int STATE_INSERT=0;//����״̬
	private static final int STATE_EDIT=1;//�༭״̬
	
	private int state;//��¼��ǰ��״̬
	
	
	// ���գ��꣬�£���
    private int mYear;
    private int mMonth;
    private int mDay;
    
    String editContactName;//����Ҫ�༭����ϵ��
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.editcontact);
		contactsManagerDbAdapter=new ContactsManagerDbAdater(this);
		contactsManagerDbAdapter.open();
		
		initAllComponents();//��ʼ���������
		initGroupSpinnerData();
		Intent intent=getIntent();
		String action=intent.getAction();
		
		if(action.equals(Intent.ACTION_INSERT)){
			state=STATE_INSERT;
		}else if(action.equals(Intent.ACTION_EDIT)){
			state=STATE_EDIT;
			//����͸�ȡ���ݿ������������
			
			editContactName=intent.getStringExtra("name");
			String sql="select * from contacts where name=?";
			String selectionArgs[]={editContactName};
			System.out.println(editContactName);
			contactInfoCursor=contactsManagerDbAdapter.getCursorBySql(sql, selectionArgs);
			startManagingCursor(contactInfoCursor);
			//��֪��Ϊ��ֻ�м��������if��䵽resume������Ż�����ִ��
			if(contactInfoCursor!=null && contactInfoCursor.getCount()>0){
				System.out.println("<<<<<<<<<<<<<<<< test >>>>>>>>>>>>>>>>>>>");
			}
		}else{
			Log.e(TAG, "Unknown action,program will exit...");
			finish();
			return;
		}
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(state == STATE_INSERT){
			setTitle("�½���ϵ��");
			getNowTime();
			String birthday=mYear+"-"+mMonth+"-"+mDay;
			birthdayButton.setText(birthday);
		}else if(state == STATE_EDIT){
			setTitle("�༭��ϵ��");
			if(contactInfoCursor!=null && contactInfoCursor.getCount()>0){
				System.out.println(">>>>>>>>>>>>>>>>>> gogogogo >>>>>>>>>>>>>>>>>>");
				if(contactAllInfoCache==null){
						contactInfoCursor.moveToFirst();
						
						//�õ����ݿ��е���ϵ����Ϣ
						_name=contactInfoCursor.getString(1);
						img=contactInfoCursor.getBlob(2);
						_phoneNumber=contactInfoCursor.getString(3);
						_groupSpinner=contactInfoCursor.getString(4);
						_birthdayButton=contactInfoCursor.getString(5);
						System.out.println(_birthdayButton);
						initDateFromDb(_birthdayButton);
						_address=contactInfoCursor.getString(6);
						_email=contactInfoCursor.getString(7);
						_information=contactInfoCursor.getString(8);
					
				}else{
						_name=contactAllInfoCache.getName();
						//img�Ѿ���onActivityResult�����ڸ�����ֵ�������ٵ���contactAllInfoCache��getContactIcon
						//�������img�����ǵ������ԾͲ��ٵ�����
						//img=contactAllInfoCache.getContactIcon();
						_phoneNumber=contactAllInfoCache.getTelPhone();
						_groupSpinner=contactAllInfoCache.getGroupName();
						_birthdayButton=contactAllInfoCache.getBirthday();
						_address=contactAllInfoCache.getAddress();
						_email=contactAllInfoCache.getEmail();
						_information=contactAllInfoCache.getDescription();
				}
				name.setText(_name);
				mEditor.setPhotoBitmap(getBitmapFromByte(img));
				phoneNumber.setText(_phoneNumber);
				int groupIndex=adapter.getPosition(_groupSpinner);
				groupSpinner.setSelection(groupIndex);
				birthdayButton.setText(_birthdayButton);
				address.setText(_address);
				email.setText(_email);
				information.setTextKeepState(_information);
				System.out.println(">>>>>>>>>>>>>>>>>> end  >>>>>>>>>>>>>>>>>>");
			}
			
		}
	}
	
	private void initDateFromDb(String birthdayButton2) {
		System.out.println(birthdayButton2);
		String args[]=birthdayButton2.split("-");
		
		System.out.println(args.length);
		mYear = Integer.valueOf(args[0]);
        mMonth = Integer.valueOf(args[1]);
        mDay = Integer.valueOf(args[2]);
		
	}

	//���ӵ�ǰ��Activity�л�����һ��Activityʱ���ã��л���ȥʱ��ǰActivity��δ����(Destory)
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.v(TAG, "record original data");
		//������ϵ����Ϣ 
		contactAllInfoCache=new MyContacts();
		//�õ������û���Ϣ
		contactAllInfoCache.setName(name.getText().toString());
		BitmapDrawable bd=(BitmapDrawable)mEditor.getDrawable();
		Bitmap bitMap=bd.getBitmap();
		contactAllInfoCache.setContactIcon(getBitmapByte(bitMap));
		contactAllInfoCache.setTelPhone(phoneNumber.getText().toString());
		contactAllInfoCache.setGroupName(groupSpinner.getSelectedItem().toString());
		contactAllInfoCache.setBirthday(birthdayButton.getText().toString());
		contactAllInfoCache.setAddress(address.getText().toString());
		contactAllInfoCache.setEmail(email.getText().toString());
		contactAllInfoCache.setDescription(information.getText().toString());
		
		outState.putSerializable("originalData", contactAllInfoCache);
	}
	
	//��ʼ��Spinner����
	public void initGroupSpinnerData(){
		adapter=new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				getAllExistGroup()
				);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(adapter);
	}
	
	
	//�õ����е���
	public ArrayList<String> getAllExistGroup(){
		Cursor cursor=contactsManagerDbAdapter.getAllGroups();
		ArrayList<String> groups=new ArrayList<String>();
		if(cursor!=null){
			while(cursor.moveToNext()){
				groups.add(cursor.getString(cursor.getColumnIndexOrThrow("groupName")));
			}
		}
		cursor.close();
		return groups;
	}
	
	//�õ�ϵͳʱ��
	private void getNowTime() {
		Calendar time = Calendar.getInstance();
        mYear = time.get(Calendar.YEAR);
        mMonth = time.get(Calendar.MONTH)+1;
        mDay = time.get(Calendar.DAY_OF_MONTH);
	}

	private void initAllComponents() {
		name=(EditText)findViewById(R.id.name);
		mEditor=(PhotoEditorView)findViewById(R.id.icon);
		phoneNumber=(EditText)findViewById(R.id.phoneNumber);
		groupSpinner=(Spinner)findViewById(R.id.spinner_group);
		birthdayButton=(Button)findViewById(R.id.birthdayButtonPicker);
		address=(EditText)findViewById(R.id.address);
		email=(EditText)findViewById(R.id.email);
		information=(EditText)findViewById(R.id.information);
		ok=(Button)findViewById(R.id.btn_ok);
		cancel=(Button)findViewById(R.id.btn_cancel);
		setComponentsListener();
	}

	private void setComponentsListener() {
		//ͼ���ϵļ���
		mEditor.setEditorListener(new PhotoListener());
		//���ϵļ���,ע������֮ǰ��Ҫ��spinner��ֵ��������
		
		groupSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				_groupSpinner=adapter.getItem(position).toString();//�õ��û�ѡ�����
				//selectedGroupNameIndex=position;//��¼���û�ѡ����������
				parent.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		//���հ�ť����
		birthdayButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createTimePickerDialog().show();
			}
		});
		
		//ok����
		ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				verifyAllData();
			}
		});
		
		//cancel����
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				finish();
			}
		});
		
	}
	
	protected void verifyAllData() {
		// �õ�������Ϣ
		_name=name.getText().toString().trim();
		BitmapDrawable bd=(BitmapDrawable)mEditor.getDrawable();
		Bitmap bitMap=bd.getBitmap();
		img=getBitmapByte(bitMap);
		_phoneNumber=phoneNumber.getText().toString().trim();
		_groupSpinner=groupSpinner.getSelectedItem().toString();
		_birthdayButton=birthdayButton.getText().toString();
		_address=address.getText().toString().trim();
		_email=email.getText().toString().trim();
		_information=information.getText().toString().trim();
		//������ݵ���Ч��
		if(_name.equals("") || _name==null){
			showToast("���ֲ���Ϊ��");
			return;
		}
		if(_phoneNumber.equals("") || _name==null){
			showToast("���� ����Ϊ��");
			return;
		}
		if(_email.equals("")){
			showToast("���䲻��Ϊ��");
			return;
		}
		if(!isEmail(_email)){
			showToast("Email��ʽ����");
			return;
		}
		if(_information.equals("")){
			showToast("������������Ϊ��");
			return;
		}
		
		contactAllInfoCache=new MyContacts();
		//�õ������û���Ϣ
		contactAllInfoCache.setName(_name);
		contactAllInfoCache.setContactIcon(img);
		contactAllInfoCache.setTelPhone(_phoneNumber);
		contactAllInfoCache.setGroupName(_groupSpinner);
		contactAllInfoCache.setBirthday(_birthdayButton);
		contactAllInfoCache.setAddress(_address);
		contactAllInfoCache.setEmail(_email);
		contactAllInfoCache.setDescription(_information);
		
		if(state == STATE_INSERT){
			long count=contactsManagerDbAdapter.inserDataToContacts(contactAllInfoCache);
			if(count>0){
				showToast("�½���ϵ�˳ɹ�");
				finish();
			}else{
				showToast("�½���ϵ��ʧ��");
				finish();
			}
				
		}else{
			System.out.println(editContactName);
			int count=contactsManagerDbAdapter.updateDataToContacts(contactAllInfoCache, editContactName);
			System.out.println(count);
			
			if(count>0){
				showToast("��ϵ�˸��³ɹ�");
				finish();
			}else{
				showToast("��ϵ�˸���ʧ��");
				finish();
			}
		}
	}
	
	public Boolean isEmail(String str){
		String regex="[a-zA-Z_]{1,}[0-9]{0,}@(([a-zA-Z0-9]-*){1,}\\.){1,3}[a-zA-Z\\-]{1,}";
		return match(regex, str);
	}
	
	public Boolean match(String regex,String str){
		Pattern pattern=Pattern.compile(regex);
		Matcher matcher=pattern.matcher(str);
		return matcher.matches();
	}

	//��������¼�
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK ){
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setTitle("��ʾ");
			builder.setMessage("ȷ��Ҫ�˳�����?");
			builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					finish();
				}
			});
			builder.setNeutralButton("ȡ��", new DialogInterface.OnClickListener() {
				
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
	
	

	//����DatePickerDialog
	protected Dialog createTimePickerDialog() {
		
		return new DatePickerDialog(this,
                mDateSetListener,
                mYear, mMonth, mDay);
	}
	
	//DatePickerDialog�ϵļ���
	private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                updateToDisplay();
            }
        };

	//����ʱ��
	 private void updateToDisplay() {
		 birthdayButton.setText(
	            new StringBuilder()
	                    .append(mYear).append("-")
	                    .append(mMonth + 1).append("-")
	                    .append(mDay));
	                    
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

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
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
	
	//�õ��洢�����ݿ��е�ͷ��
	public Bitmap getBitmapFromByte(byte[] temp){
		if(temp!=null){
			Bitmap bitmap=BitmapFactory.decodeByteArray(temp, 0, temp.length);
			return bitmap;
		}else{
			//Bitmap bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.contact_add_icon);
			return null;
		}
	}

	//������ʾ��Ϣ
	public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

	public class PhotoListener implements PhotoEditorView.EditorListener,
			DialogInterface.OnClickListener {

		@Override
		public void onRequest(int request) {
			if (request == PhotoEditorView.REQUEST_PICK_PHOTO) {
				if (mEditor.hasSetPhoto()) {
					// ��ǰ�Ѿ�������Ƭ
					createPhotoDialog().show();
				}else{
					doPickPhotoAction();
				}
			}
		}

		private Dialog createPhotoDialog() {
			Context context = EditContact.this;
			final Context dialogContext = new ContextThemeWrapper(context,
					android.R.style.Theme_Light);
			String cancel="����";
			String[] choices;
			choices = new String[3];
			choices[0] = getString(R.string.use_photo_as_primary);
			choices[1] = getString(R.string.removePicture);
			choices[2] = getString(R.string.changePicture);
			final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
					android.R.layout.simple_list_item_1, choices);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					dialogContext);
			builder.setTitle(R.string.attachToContact);
			builder.setSingleChoiceItems(adapter, -1, this);
			builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			});
			return builder.create();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			switch (which) {
			case 0:
				break;// ʲôҲ����
			case 1:
				// ɾ��ͼ��
				mEditor.setPhotoBitmap(null);
				break;
			case 2:
				// �滻ͼ��
				doPickPhotoAction();
				break;
			}
		}

		private void doPickPhotoAction() {
			Context context = EditContact.this;

			// Wrap our context to inflate list items using correct theme
			final Context dialogContext = new ContextThemeWrapper(context,
					android.R.style.Theme_Light);
			String cancel="����";
			String[] choices;
			choices = new String[2];
			choices[0] = getString(R.string.take_photo);
			choices[1] = getString(R.string.pick_photo);
			final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
					android.R.layout.simple_list_item_1, choices);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					dialogContext);
			builder.setTitle(R.string.attachToContact);
			builder.setSingleChoiceItems(adapter, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							switch (which) {
							case 0:{
								String status=Environment.getExternalStorageState();
								if(status.equals(Environment.MEDIA_MOUNTED)){//�ж��Ƿ���SD��
									doTakePhoto();// �û�����˴��������ȡ
								}
								else{
									showToast("û��SD��");
								}
								break;
								
							}
							case 1:
								doPickPhotoFromGallery();// �������ȥ��ȡ
								break;
							}
						}
					});
			builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			});
			builder.create().show();
		}
	}

	/**
	 * ���ջ�ȡͼƬ
	 * 
	 */
	protected void doTakePhoto() {
		try {
			// Launch camera to take photo for selected contact
			PHOTO_DIR.mkdirs();// ������Ƭ�Ĵ洢Ŀ¼
			mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// �����յ���Ƭ�ļ�����
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.photoPickerNotFoundText,
					Toast.LENGTH_LONG).show();
		}
	}

	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	/**
	 * �õ�ǰʱ���ȡ�õ�ͼƬ����
	 * 
	 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date) + ".jpg";
	}

	// ����Gallery����
	protected void doPickPhotoFromGallery() {
		try {
			// Launch picker to choose photo for selected contact
			final Intent intent = getPhotoPickIntent();
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.photoPickerNotFoundText1,
					Toast.LENGTH_LONG).show();
		}
	}

	// ��װ����Gallery��intent
	public static Intent getPhotoPickIntent() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		intent.putExtra("return-data", true);
		return intent;
	}

	// ��Ϊ������Camera��Gally����Ҫ�ж����Ǹ��Եķ������,��������ʱ��������startActivityForResult
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case PHOTO_PICKED_WITH_DATA: {// ����Gallery���ص�
			final Bitmap photo = data.getParcelableExtra("data");
			// ���������ʾ��Ƭ��
			System.out.println(photo);
			//�����û�ѡ���ͼƬ
			img=getBitmapByte(photo);
			Log.v(TAG, "new photo set!");
			mEditor.setPhotoBitmap(photo);
			System.out.println("set new photo");
			break;
		}
		case CAMERA_WITH_DATA: {// ��������򷵻ص�,�ٴε���ͼƬ��������ȥ�޼�ͼƬ
			doCropPhoto(mCurrentPhotoFile);
			break;
		}
		}
	}

	protected void doCropPhoto(File f) {
		try {// f ����������µ���Ƭ
			// ����������Ҫ��������µ���Ƭ�ŵ�ý�������ģ����ǵ�ǰ��SDK1.5�汾��֧������ķ���
			// MediaScannerConnection.scanFile(
			// this,
			// new String[] { f.getAbsolutePath() },
			// new String[] { null },
			// null);
			// Launch gallery to crop the photo
			// ����galleryȥ���������Ƭ
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (Exception e) {
			Log.e(TAG, "Cannot crop image", e);
			Toast.makeText(this, R.string.photoPickerNotFoundText,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Constructs an intent for image cropping. ����ͼƬ��������
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		intent.putExtra("return-data", true);
		return intent;
	}
}
