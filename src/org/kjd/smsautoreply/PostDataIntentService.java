package org.kjd.smsautoreply;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


public class PostDataIntentService extends IntentService {

	public static final String INTENT_PHONE_NUMBER = "nmbr";
	public static final String INTENT_MESSAGE_BODY = "msg";
	public static final String INTENT_MESSAGE_TIME = "time";

	public static final String INTENT_RESPONSE = "resp";
	
	public static final String TAG = "PostDataService";
	public static final String ACTION = "org.kjd.smsautoreply.PostDataIntentService";

	//	SharedPreferences prefs = this.getSharedPreferences(
	//			"org.kjd.smsautoreply", Context.MODE_PRIVATE);

	public PostDataIntentService() {
		super("PostDataIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String number = intent.getStringExtra(INTENT_PHONE_NUMBER);
		String message = intent.getStringExtra(INTENT_MESSAGE_BODY);
		String autoresponse = intent.getStringExtra(INTENT_RESPONSE);
		String time = intent.getStringExtra(INTENT_MESSAGE_TIME);

		sendSMS(number, autoresponse);

		proceed(number, message, time);

	}

	public void proceed(final String number, final String message, final String time){
		if(isOnline())
			postData(number, message, time+"");
		else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_connection_problem), 
					Toast.LENGTH_LONG).show();
			SystemClock.sleep(300000);
			Log.d(TAG,"Offline, try later....");
			proceed(number, message, time);
			
		}
	}

	public void postData(String number, String message, String time) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://dev.kakavjedoktor.org/sms/get/");

		try {
			// Add your data
			
			Log.d(TAG,"post request: " + ", number: " + number + ", message: " + message + ", time: " + time);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("number", number));
			nameValuePairs.add(new BasicNameValuePair("text", message));
			nameValuePairs.add(new BasicNameValuePair("date", time));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			String resp = response.getAllHeaders().toString();

			Log.d(TAG,"response from server: " + response.getStatusLine() + " i ovo: " + resp);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	} 

	public boolean isOnline() {
		ConnectivityManager cm =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public void sendSMS(String phoneNumber, String message) {
		SmsManager smsManager = SmsManager.getDefault();
//		smsManager.sendTextMessage(phoneNumber, null, message, null, null);
		ArrayList<String> parts = smsManager.divideMessage(message);
		smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
	}


}
