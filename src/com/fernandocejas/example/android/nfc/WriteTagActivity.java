package com.fernandocejas.example.android.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class WriteTagActivity extends Activity {
	
	private static final String TAG = WriteTagActivity.class.getSimpleName();

    // NFC-related variables
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mWriteTagFilters;
    private boolean mWriteMode = false;	
    
    private ImageView mImageViewImage;
    private EditText mEditTextData;
    private Button mButtonWrite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writetag);
        
        mImageViewImage = (ImageView)findViewById(R.id.image);
        mEditTextData = (EditText)findViewById(R.id.textData);
        mButtonWrite = (Button)findViewById(R.id.buttonWriteTag);
        mButtonWrite.setOnClickListener(mTagWriter);
        
        // get an instance of the context's cached NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
        // if null is returned this demo cannot run. Use this check if the
        // "required" parameter of <uses-feature> in the manifest is not set
        if (mNfcAdapter == null){
            Toast.makeText(this, "Your device does not support NFC. Cannot run demo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // check if NFC is enabled
        checkNfcEnabled();

        // Handle foreground NFC scanning in this activity by creating a
        // PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag so each new scan
        // is not added to the Back Stack
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Create intent filter to detect any NFC tag when attempting to write
        // to a tag in "write mode"
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        // create IntentFilter arrays:
        mWriteTagFilters = new IntentFilter[] { tagDetected };
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        // Double check if NFC is enabled
        checkNfcEnabled();

        Log.d(TAG, "onResume: " + getIntent());
    }    
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: " + getIntent());
        mNfcAdapter.disableForegroundDispatch(this);
    }

    /*
     * This is called for activities that set launchMode to "singleTop" or
     * "singleTask" in their manifest package, or if a client used the
     * FLAG_ACTIVITY_SINGLE_TOP flag when calling startActivity(Intent).
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: " + intent);

        if (mWriteMode) {
            // Currently in tag WRITING mode
            if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                writeTag(buildNdefMessage(), detectedTag);
                	
                mImageViewImage.setImageDrawable(getResources().getDrawable(R.drawable.android_blue_logo));
                mEditTextData.setEnabled(true);
            }
        }
    }    
    
    /*
     * **** WRITING MODE METHODS ****
     */

    private View.OnClickListener mTagWriter = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
        	if (mEditTextData.getText().toString().trim().length() == 0) {
        		Toast.makeText(WriteTagActivity.this, "The data to write is empty. Please fill it!", Toast.LENGTH_LONG).show();
        	} else {
        		enableTagWriteMode();
        	}
        }
    };    
    
    private void enableTagWriteMode() {
        mWriteMode = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
        
        mImageViewImage.setImageDrawable(getResources().getDrawable(R.drawable.android_writing_logo));
        mEditTextData.setEnabled(false);
    }

    boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    Toast.makeText(this, "Cannot write to this tag. This tag is read-only.", Toast.LENGTH_LONG).show();
                    return false;
                }
                
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(this, "Cannot write to this tag. Message size (" + size
                                       + " bytes) exceeds this tag's capacity of "
                                       + ndef.getMaxSize() + " bytes.", Toast.LENGTH_LONG).show();
                    return false;
                }

                ndef.writeNdefMessage(message);
                Toast.makeText(this, "A pre-formatted tag was successfully updated.", Toast.LENGTH_LONG).show();
                return true;
            } 
            
            Toast.makeText(this, "Cannot write to this tag. This tag does not support NDEF.", Toast.LENGTH_LONG).show();
            return false;
            
        } catch (Exception e) {
            Toast.makeText(this, "Cannot write to this tag due to an Exception.", Toast.LENGTH_LONG).show();
        }

        return false;
    }   
    
    private NdefMessage buildNdefMessage() {
        // get the values from the form's text fields:
        String data = mEditTextData.getText().toString().trim();

        // create a new NDEF record and containing NDEF message using the app's custom MIME type:
        String mimeType = "application/com.fernandocejas.example.android.nfc";
        
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] id = new byte[0];
        
        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, dataBytes);
        NdefMessage message = new NdefMessage(new NdefRecord[] { record });

        // return the NDEF message
        return message;
    }
    
    /*
     * **** HELPER METHODS ****
     */    
    
    private void checkNfcEnabled() {
        Boolean nfcEnabled = mNfcAdapter.isEnabled();
        if (!nfcEnabled) {
            new AlertDialog.Builder(WriteTagActivity.this)
                    .setTitle(getString(R.string.text_warning_nfc_is_off))
                    .setMessage(getString(R.string.text_turn_on_nfc))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.text_update_settings),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id){
                                    startActivity(new Intent( android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).create().show();
        }
    }    
}
