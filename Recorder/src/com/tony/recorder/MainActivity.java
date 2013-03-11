package com.tony.recorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.fish.meetingnotepad.utils.Utils;
//import com.fish.meetingnotepad.views.VolumeView;

public class MainActivity extends Activity{

private static final String tag="MainActivity";
    private MediaRecorder mRecorder;   
    
private ImageButton mRecorderBtn;
private ImageButton mStopBtn;
private TextView mStatusTextView ;
private TextView mTimeTextView;
private LinearLayout mVolumeLayout;
private MyHandler mHandler = new MyHandler();
private VolumeView volumeView ;
private static String mFilepath ;
private List<File> mTmpFile = new ArrayList<File>();
private int mSeagments =  1;
private MediaRecorderState mRecordState = MediaRecorderState.STOPPED;
private enum MediaRecorderState
    {
STOPPED, RECORDING, PAUSED
    }
final String[] OutFilePostfix = {"amr","mp4","3gp"};
int mCurOutputFormat;
final String TAG="Recorder";
@Override
    public void onCreate(Bundle savedInstanceState) {
	Log.d(TAG,"enter onCreate");
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_recorder);
       
        mFilepath = Environment.getExternalStorageDirectory().getPath()+"/";//Utils.getStorePath();
        Log.d(TAG,"mFilepath="+mFilepath);
        mStatusTextView = (TextView)this.findViewById(R.id.recording_tip_tv_id);
        mStatusTextView.setText("µã»÷Â¼Òô°´Å¥¿ªÊ¼Â¼Òô.");
        mStatusTextView.setTextColor(Color.WHITE);


        mTimeTextView = (TextView)this.findViewById(R.id.recording_time_tv_id1);
        
        mRecorderBtn = (ImageButton)this.findViewById(R.id.recorder_btn_id);
        mRecorderBtn.setOnClickListener(new ButtonOnClickListener());
       
        mStopBtn = (ImageButton)this.findViewById(R.id.stop_btn_id);
        mStopBtn.setOnClickListener(new ButtonOnClickListener());
        mStopBtn.setEnabled(false);
        
        mVolumeLayout = (LinearLayout)this.findViewById(R.id.volume_show_id);
        int[] location = new int[2];
        mVolumeLayout.getLocationOnScreen(location);
        volumeView = new VolumeView(this,location[1]+100);
        mVolumeLayout.removeAllViews();
        mVolumeLayout.addView(volumeView);
        Thread t = new Thread(new HandlerInvocation());
        t.start();
        Log.d(TAG,"leave onCreate");
}

@Override  
protected void onStop() {
	if (mRecorder != null && mRecordState != MediaRecorderState.STOPPED) {
	stopRecording();
	}
	super.onStop();
}

private void startRecording(){
	Log.d(TAG,"enter startRecording");
	mRecordState = MediaRecorderState.RECORDING;
	File file = new File(mFilepath+(new Date().getTime())+"_"+mSeagments+"."+OutFilePostfix[mCurOutputFormat]);
	mTmpFile.add(file);
	mSeagments++;
	if(file.exists()){
	if(file.delete())
	try {
	file.createNewFile();
	} catch (IOException e) { 
	e.printStackTrace();
	}
	}else{
	try {
	file.createNewFile();
	} catch (IOException e) {
	e.printStackTrace();
	}
	}
	mRecorder = new MediaRecorder();
	mCurOutputFormat=1;
	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//.THREE_GPP);//.AMR_NB);// AAC_ADTS);//THREE_GPP); //RAW_AMR
	mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);//DEFAULT); //AMR_NB HE_AAC
	mRecorder.setOutputFile(file.getAbsolutePath());
	Log.d(TAG,"output file is set as "+file.getAbsolutePath());
	
	mRecorder.setOnErrorListener(new OnErrorListener(){
	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
	mRecorder.reset();
	}
	});
	try {
		mRecorder.prepare();
		mRecorder.start();
		Thread t = new Thread(new DbThread());
		t.start();
	} catch (Exception e) {
		e.printStackTrace();
		mRecorder.release();
	}finally{
		Log.d(TAG,"leave startRecording");	
	}
}

private void pauseRecording(){
	mRecordState = MediaRecorderState.PAUSED;
	if(mRecorder!=null){
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}
}
private void stopRecording(){
	Log.d(TAG,"enter stopRecording");
	mRecordState = MediaRecorderState.STOPPED;
	timeCount = 0;
	if (mRecorder != null) {
	mRecorder.stop();
	mRecorder.release();
	mRecorder = null;
	}
	Log.d(TAG,"recording stopped");
	File finalFile = new File(mFilepath+(new Date().getTime())+"."+OutFilePostfix[mCurOutputFormat]);
	if (!finalFile.exists()) {
	try {
	finalFile.createNewFile();
	} catch (IOException e) {
	e.printStackTrace();
	}
	}
	Log.d(TAG,"finalFile is set as "+finalFile.getAbsolutePath());
	FileOutputStream fileOutputStream = null;
	try {
	fileOutputStream = new FileOutputStream(finalFile);
	} catch (IOException e) {
	e.printStackTrace();
	}
	for (int i = 0; i < mTmpFile.size(); i++) {
	File tmpFile = mTmpFile.get(i);
	FileInputStream fis = null;
	try {
	fis = new FileInputStream(tmpFile);
	byte[] tmpBytes = new byte[fis.available()];
	int lenght = tmpBytes.length;
	if (i == 0) {
	while (fis.read(tmpBytes) != -1) {
	fileOutputStream.write(tmpBytes, 0, lenght);
	}
	} else {
	while (fis.read(tmpBytes) != -1) {
	fileOutputStream.write(tmpBytes, 6, lenght - 6);
	}
	}
	fileOutputStream.flush();
	fis.close();
	} catch (FileNotFoundException e) {
	e.printStackTrace();
	} catch (IOException e) {
	e.printStackTrace();
	} finally {
	fis = null;
	}
	}
	try {
	if (fileOutputStream != null)
	fileOutputStream.close();
	} catch (IOException e) {
	e.printStackTrace();
	} finally {
	fileOutputStream = null;
	}
	for (File f : mTmpFile)
	f.delete();
	mTmpFile.clear();
	mSeagments = 1;
}

class ButtonOnClickListener implements OnClickListener{
@Override
public void onClick(View paramView) {
	Log.d(TAG,"in onClick "+paramView.getId());
	switch(paramView.getId()){
	case R.id.recorder_btn_id:
		mStopBtn.setEnabled(true);
		if(mRecordState == MediaRecorderState.STOPPED || mRecordState == MediaRecorderState.PAUSED){
			startRecording();
			mRecorderBtn.setImageResource(R.drawable.record_stop);
			mStatusTextView.setText("ÕýÔÚ½øÐÐÂ¼Òô...");
		    mStatusTextView.setTextColor(Color.GREEN);   
		}else if(mRecordState == MediaRecorderState.RECORDING){
			pauseRecording();
			mRecorderBtn.setImageResource(R.drawable.record_start);
			mStatusTextView.setText("Â¼ÒôÒÑÔÝÍ£");
		    mStatusTextView.setTextColor(Color.GRAY);
		}
		break;
	
	case R.id.stop_btn_id:
		stopRecording();
		mStopBtn.setEnabled(false);
		mStopBtn.setImageResource(R.drawable.stop_stopped);
		mRecorderBtn.setImageResource(R.drawable.record_start);
		mStatusTextView.setText("Â¼ÒôÒÑÍ£Ö¹");
		mStatusTextView.setTextColor(Color.RED);
		break;
	}
}
}

private int timeCount = 0;
class MyHandler extends Handler{
public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }
        @Override
        public void handleMessage(Message msg) {
        super.handleMessage(msg);
		if (msg.what == -1) {
		int minute = timeCount / 60;
		int second = timeCount % 60;
		String min = minute >= 10 ? minute + "" : "0" + minute;
		String sec = second >= 10 ? second + "" : "0" + second;
		mTimeTextView.setText(min + ":" + sec);
		}else if(volumeView!=null){
		volumeView.changed(msg.what);
		}
    }
}

class HandlerInvocation implements Runnable{
@Override
public void run() {
while (true) {
if (mRecordState == MediaRecorderState.RECORDING) {
Message msg = new Message();
msg.what = -1;
MainActivity.this.mHandler.sendMessage(msg);
timeCount++;
}
try {
Thread.sleep(1000);
} catch (InterruptedException e) {
e.printStackTrace();
}
}
}
}
class DbThread implements Runnable{
@Override
public void run() {
while(true){
int db = 0;
try {
Thread.sleep(100);
} catch (InterruptedException e) {
e.printStackTrace();
}
if(mRecorder!=null && mRecordState==MediaRecorderState.RECORDING){
try{
double f=10*Math.log10(mRecorder.getMaxAmplitude());
if(f<0){
db = 0;
}else{
db = (int)(f*2);
}
}catch(Exception e){
e.printStackTrace();
continue;
}
Message msg = new Message();
msg.what = db;
MainActivity.this.mHandler.sendMessage(msg);
}else{
Message msg = new Message();
msg.what = db;
MainActivity.this.mHandler.sendMessage(msg);
break;
}
}
}
}
}
