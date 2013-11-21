package org.kjd.smsautoreply;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class ServiceWatcher extends Service
{
	public static boolean doIt = false;

	private NotificationManager notificationManager;

	private NotificationCompat.Builder noti;
	private Notification n;
	
	SharedPreferences prefs;
	Editor editor;


	public class LocalBinder extends Binder {
		ServiceWatcher getService() {
			return ServiceWatcher.this;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{		
		notificationManager.cancel(1);
		super.onDestroy();
	}@TargetApi(Build.VERSION_CODES.HONEYCOMB)

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		prefs = getSharedPreferences(
				"org.kjd.smsautoreply", Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		noti = new NotificationCompat.Builder(this)
		.setContentTitle(getResources().getString(R.string.notif_title))
		.setContentText(getResources().getString(R.string.notif_text))
		.setSmallIcon(R.drawable.ic_launcher);
		
		
//		noti.flags = Notification.FLAG_NO_CLEAR;
		
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		noti.setContentIntent(resultPendingIntent);
		n = noti.build();
		n.flags = Notification.FLAG_NO_CLEAR;
		
		if(prefs.contains("doit"))
			doIt = prefs.getBoolean("doit", false);
		
		if(doIt)
			notificationManager.notify(1, n);
		
		Log.d("ServiceWatcher", "Watch on SMS or not: " + doIt);

		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new LocalBinder();


	public void setDoIt(boolean check){
		doIt = check;
		editor.putBoolean("doit", check);
		editor.commit();
		if(doIt)
			notificationManager.notify(1, n);
		else
			notificationManager.cancel(1);
	}
}