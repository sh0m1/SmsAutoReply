package org.kjd.smsautoreply;

import org.kjd.smsautoreply.ServiceWatcher.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

public class MainActivity extends Activity {



	ServiceWatcher mService;
	boolean mBound = false;
	EditText inputText;
	ToggleButton toggleButton;
	
	SharedPreferences prefs;
	Editor editor;
	boolean doIt = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prefs = getSharedPreferences(
				"org.kjd.smsautoreply", Context.MODE_PRIVATE);
		editor = prefs.edit();

		inputText = (EditText)findViewById(R.id.editText1);
		if(prefs.contains("txt")) {
			String s = prefs.getString("txt", "");
			inputText.setText(s);
		}
		inputText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(!s.equals("")) {
//					ServiceWatcher.response = s.toString();
					editor.putString("txt", s.toString());
					editor.commit();
				}

			}
		});
		
		if(prefs.contains("doit"))
			doIt = prefs.getBoolean("doit", false);

		toggleButton = (ToggleButton)findViewById(R.id.toggleButton1);
		toggleButton.setChecked(doIt);
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
					mService.setDoIt(isChecked);
			}
		});
		
		startService(new Intent(getBaseContext(), ServiceWatcher.class));
	}
	
	  @Override
	    protected void onStart() {
	        super.onStart();
	        // Bind to LocalService
	        Intent intent = new Intent(this, ServiceWatcher.class);
	        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        // Unbind from the service
	        if (mBound) {
	            unbindService(mConnection);
	            mBound = false;
	        }
	    }

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		// Unregister the SMS receiver
		//	        unregisterReceiver(mSMSreceiver);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

}
