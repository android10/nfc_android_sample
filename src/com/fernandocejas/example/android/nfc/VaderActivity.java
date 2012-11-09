package com.fernandocejas.example.android.nfc;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;

public class VaderActivity extends Activity {

	MediaPlayer mMediaPlayer;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vader);
    }
	
    @Override
	protected void onStart() {
		super.onStart();
		playSound();
	}
    
	@Override
	protected void onStop() {
		super.onStop();
		
		try {
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
			
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}    	
	}    
    
    private void playSound() {
    	try {
    	    FileDescriptor fd = null;

    	    File baseDir = Environment.getExternalStorageDirectory();
    	    String audioPath = baseDir.getAbsolutePath() + "/vader.mp3";
    	    FileInputStream fis = new FileInputStream(audioPath);
    	    fd = fis.getFD();

    	    if (fd != null) {
    	        mMediaPlayer = new MediaPlayer();
    	        mMediaPlayer.setDataSource(fd);
    	        mMediaPlayer.prepare();
    	        mMediaPlayer.start();
    	    }
    	    
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}    	
    }
}
