package net.homelinux.penecoptero.android.openbicing.receivers;

import java.util.HashMap;
import java.util.Map;

import net.homelinux.penecoptero.android.openbicing.app.MainActivity;
import net.homelinux.penecoptero.android.openbicing.app.R;
import net.homelinux.penecoptero.android.openbicing.app.RESTHelper;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.util.Log;

public class C2DMReceiver extends BroadcastReceiver {
	private static final int OPENBICING_PUSH_ID = 1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i("C2DM", "New !!! Action: "+action);
		Log.w("C2DM", "Registration Receiver called");
		if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
			Log.w("C2DM", "Received registration ID");
			final String registrationId = intent
					.getStringExtra("registration_id");
			String error = intent.getStringExtra("error");
			String deviceId = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);
			Log.d("C2DM", "device_id: "+deviceId);
			Log.d("C2DM", "dmControl: registrationId = " + registrationId
					+ ", error = " + error);
			// TODO Send this to my application server
			RESTHelper rHelper = new RESTHelper(false, "foo","bar");
			Map <String, String> data = new HashMap<String, String>();
			data.put("regId", registrationId);
			data.put("devId", deviceId);
			data.put("action","register");
			try{
				String response = rHelper.restPOST("http://laika.citybik.es:8282", data);
				Log.d("C2DM", "Response from laika: "+response);
			}catch (Exception e){
				Log.d("C2DM","Error posting to laika");
			}
			
			
		} else if ("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
				Log.w("C2DM", "Received message");
				final String payload = intent.getStringExtra("payload");
				Log.d("C2DM", "dmControl: payload = " + payload);
				//Toast toast = Toast.makeText(context,payload,Toast.LENGTH_LONG);
				//toast.show();
				
				String ns = Context.NOTIFICATION_SERVICE;
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
				String stationName;
				int stationId = -1;
				try{
					JSONObject station = new JSONObject(payload);
					stationName = station.getString("name");
					stationId = station.getInt("id");
					Log.i("C2DM","Station id is "+Integer.toString(stationId));
				} catch (Exception e){
					stationName = "error";
				}
				int icon = R.drawable.icon;
				CharSequence tickerText = "Your bike @ "+stationName+" is ready!";
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText, when);
				notification.defaults |= Notification.DEFAULT_SOUND;
				CharSequence contentTitle = "Open Bicing station ready!";
				CharSequence contentText = "Your bike @ "+stationName+" is ready!";
				Intent notificationIntent = new Intent(context, MainActivity.class);
				if (stationId != -1){
					notificationIntent.putExtra("c2dm_station_id", stationId);
					Log.i("C2DM","Putting extra to intent "+Integer.toString(stationId));
				}
				
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
						PendingIntent.FLAG_ONE_SHOT
				        + PendingIntent.FLAG_UPDATE_CURRENT);
				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(OPENBICING_PUSH_ID, notification);
		}
	}
}
