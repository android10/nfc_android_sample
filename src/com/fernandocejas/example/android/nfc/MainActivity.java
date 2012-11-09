package com.fernandocejas.example.android.nfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private Button mButtonWriteTag; 
	private Button mButtonReadTag;
	private Button mButtonAndroidBeam;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mButtonWriteTag = (Button)findViewById(R.id.buttonWriteTag);
        mButtonWriteTag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, WriteTagActivity.class));
			}
		});
        
        mButtonReadTag = (Button)findViewById(R.id.buttonReadTag);
        mButtonReadTag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ReadTagActivity.class));
			}
		});
        
        mButtonAndroidBeam = (Button)findViewById(R.id.buttonAndroidBeam);
        mButtonAndroidBeam.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, BeamActivity.class));
			}
		});
    }
}
