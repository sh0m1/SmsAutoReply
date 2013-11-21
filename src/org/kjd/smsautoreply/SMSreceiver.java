package org.kjd.smsautoreply;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class SMSreceiver extends BroadcastReceiver
{
	private final String TAG = this.getClass().getSimpleName();
	private Context context;

	SharedPreferences prefs;
	Editor editor;
//	public SMSreceiver(Context context){
//		this.context = context;
//		prefs = context.getSharedPreferences(
//				"org.kjd.smsautoreply", Context.MODE_PRIVATE);
//		editor = prefs.edit();
//
//	}
	public SMSreceiver(){}


	private static Map<String, String[]> RetrieveMessages(Intent intent) {
		Map<String, String[]> msg = null; 
		SmsMessage[] msgs = null;
		Bundle bundle = intent.getExtras();

		if (bundle != null && bundle.containsKey("pdus")) {
			Object[] pdus = (Object[]) bundle.get("pdus");

			if (pdus != null) {
				int nbrOfpdus = pdus.length;
				msg = new HashMap<String, String[]>(nbrOfpdus);
				msgs = new SmsMessage[nbrOfpdus];

				// There can be multiple SMS from multiple senders, there can be a maximum of nbrOfpdus different senders
				// However, send long SMS of same sender in one message
				for (int i = 0; i < nbrOfpdus; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

					String originatinAddress = msgs[i].getOriginatingAddress();

					// Check if index with number exists                    
					if (!msg.containsKey(originatinAddress)) { 
						// Index with number doesn't exist                                               
						// Save string into associative array with sender number as index
						msg.put(msgs[i].getOriginatingAddress(), new String[]{msgs[i].getMessageBody(), String.valueOf(msgs[i].getTimestampMillis())}); 

					} else {    
						// Number has been there, add content but consider that
						// msg.get(originatinAddress) already contains sms:sndrNbr:previousparts of SMS, 
						// so just add the part of the current PDU
						String previousparts = msg.get(originatinAddress)[0];
						String allParts = previousparts + msgs[i].getMessageBody();
						msg.put(msgs[i].getOriginatingAddress(), new String[]{allParts, String.valueOf(msgs[i].getTimestampMillis())});
					}
				}
			}
		}

		return msg;
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		prefs = context.getSharedPreferences(
				"org.kjd.smsautoreply", Context.MODE_PRIVATE);
				
		Map<String, String[]> msg = RetrieveMessages(intent);

		if (msg == null) {
			// unable to retrieve SMS
			return;
		} else  {
			// send all SMS via XMPP by sender
			for (String sender : msg.keySet()) {

//				long msgDate = System.currentTimeMillis();
//				Toast.makeText(context,messagio.get(messagio), 
//						Toast.LENGTH_SHORT).show();

				Intent msgIntent = new Intent(context, PostDataIntentService.class);
				msgIntent.putExtra(PostDataIntentService.INTENT_MESSAGE_BODY, msg.get(sender)[0]);
				msgIntent.putExtra(PostDataIntentService.INTENT_PHONE_NUMBER, sender);
				msgIntent.putExtra(PostDataIntentService.INTENT_MESSAGE_TIME, Long.valueOf(msg.get(sender)[1])/1000+"");
				String s = prefs.getString("txt", "");
				msgIntent.putExtra(PostDataIntentService.INTENT_RESPONSE, s);
				
				
				boolean doit = false;
				if(prefs.contains("doit"))
					doit = prefs.getBoolean("doit", false);
				
				if(doit)
					context.startService(msgIntent);
				
//				Log.d(TAG, "Sender: " + messagio + "; message: " + messagio.getMessageBody() + "; time: " 
//				+ messagio.getTimestampMillis());

//				editor.putLong(String.valueOf(prefs.getAll().size()), msgDate);
//				editor.commit();

			}
		}



//		Bundle extras = intent.getExtras();
//
//		String strMessage = "";
//
//		if ( extras != null && MainActivity.doIt)
//		{
//			Object[] pdus = (Object[]) extras.get( "pdus" );
//
//			final SmsMessage[] messages = new SmsMessage[pdus.length];
//
//			for ( int i = 0; i < pdus.length; i++ )
//			{
//				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
//				SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])pdus[i]);
//
//				String strMsgBody = smsmsg.getMessageBody().toString();
//				String strMsgSrc = smsmsg.getOriginatingAddress();
//				long msgDate = smsmsg.getTimestampMillis();
//
//				strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;   
//
//				Toast.makeText(context,strMessage, 
//						Toast.LENGTH_SHORT).show();
//
//				Intent msgIntent = new Intent(context, PostDataIntentService.class);
//				msgIntent.putExtra(PostDataIntentService.INTENT_MESSAGE_BODY, strMsgBody);
//				msgIntent.putExtra(PostDataIntentService.INTENT_PHONE_NUMBER, strMsgSrc);
//				msgIntent.putExtra(PostDataIntentService.INTENT_MESSAGE_TIME, msgDate);
//				context.startService(msgIntent);
//
//				editor.putLong(String.valueOf(prefs.getAll().size()), msgDate);
//				editor.commit();
//
//
//				Log.i(TAG, strMessage);
//			}
//
//		}

	}



}