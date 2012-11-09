package com.fernandocejas.example.android.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class BeamActivity extends Activity implements CreateNdefMessageCallback {

	private EditText mEditTextData;
	
	NfcAdapter mNfcAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beam);
        
        mEditTextData = (EditText)findViewById(R.id.textData);
        
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);        
    }
    
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
        // get the values from the form's text fields:
        String data = mEditTextData.getText().toString().trim();

        // create a new NDEF record and containing NDEF message using the app's custom MIME type:
        String mimeType = "application/com.fernandocejas.example.android.nfc";
        
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] id = new byte[0];
        
        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, dataBytes);
        
        /**
         * The Android Application Record (AAR) is commented out. When a device
         * receives a push with an AAR in it, the application specified in the AAR
         * is guaranteed to run. The AAR overrides the tag dispatch system.
         * You can add it back in to guarantee that this
         * activity starts when receiving a beamed message. For now, this code
         * uses the tag dispatch system.
         */
         //,NdefRecord.createApplicationRecord("com.example.android.beam")        
        
        NdefMessage message = new NdefMessage(new NdefRecord[] { record });

        // return the NDEF message
        return message;
	}    
}
