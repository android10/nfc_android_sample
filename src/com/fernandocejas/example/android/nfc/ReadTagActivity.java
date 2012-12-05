package com.fernandocejas.example.android.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ReadTagActivity extends Activity {
	
	private static final String TAG = WriteTagActivity.class.getSimpleName();

    // NFC-related variables
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadTagFilters;
    
    private TextView mTextViewData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readtag);
        
        mTextViewData = (TextView)findViewById(R.id.textData);
        
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
        
        // Create intent filter to handle NDEF NFC tags detected from inside our
        // application when in "read mode":
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("application/com.fernandocejas.example.android.nfc");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Could not add MIME type.", e);
        }
        
        mReadTagFilters = new IntentFilter[] { ndefDetected };
    }
    
    /* Called when the activity will start interacting with the user. */
    @Override
    protected void onResume()
    {
        super.onResume();

        // Double check if NFC is enabled
        checkNfcEnabled();

        Log.d(TAG, "onResume: " + getIntent());

        if (getIntent().getAction() != null) {
            // tag received when app is not running and not in the foreground:
            if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
                NdefRecord record = msgs[0].getRecords()[0];
                byte[] payload = record.getPayload();
                
                String payloadString = new String(payload);
                
                if (payloadString.equals("vader")) {
                	finish();
                	startActivity(new Intent(ReadTagActivity.this, VaderActivity.class));
                
                } else if (payloadString.equals("ass")) {
                	finish();
                	startActivity(new Intent(ReadTagActivity.this, AssActivity.class));
                }
                
                mTextViewData.setText(payloadString);
            }
        }

        // Enable priority for current activity to detect scanned tags
        // enableForegroundDispatch( activity, pendingIntent, intentsFiltersArray, techListsArray );
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadTagFilters, null);

    }

    /* Called when the system is about to start resuming a previous activity. */
    @Override
    protected void onPause()
    {
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
    protected void onNewIntent(Intent intent)
    {
        Log.d(TAG, "onNewIntent: " + intent);

        // Currently in tag READING mode
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
            confirmDisplayedContentOverwrite(msgs[0]);
            
        } else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Toast.makeText(this, "This NFC tag has no NDEF data.", Toast.LENGTH_LONG).show();
        }
    }    
    
    /*
     * **** READING MODE METHODS ****
     */
    NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }
            
        } else {
            Log.e(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }    
    
    private void confirmDisplayedContentOverwrite(final NdefMessage msg) {
    	final String data = mTextViewData.getText().toString().trim();
    	
        new AlertDialog.Builder(this)
                .setTitle("New tag found!")
                .setMessage("Do you wanna show the content of this tag?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // use the current values in the NDEF payload to update the text fields
                        String payload = new String(msg.getRecords()[0].getPayload());
                        
                        mTextViewData.setText(new String(payload));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    	mTextViewData.setText(data);
                        dialog.cancel();
                    }
                }).show();
    }    
    
    /*
     * **** HELPER METHODS ****
     */    
    
    private void checkNfcEnabled() {
        Boolean nfcEnabled = mNfcAdapter.isEnabled();
        if (!nfcEnabled) {
            new AlertDialog.Builder(ReadTagActivity.this)
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
