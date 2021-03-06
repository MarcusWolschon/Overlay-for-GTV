/*
 * Copyright (C) 2012 ENTERTAILION, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.entertailion.android.overlay;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Alarm broadcast receiver for periodic mover activations. Alarms created in ConfigActivity.
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	private static final String LOG_CAT = "AlarmReceiver";
	private static final String APP_LIVE_TV = "com.google.tv.player";

	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.d(LOG_CAT, "onReceive");
		startMover(context);
	}

	/**
	 * Start the mover.
	 * 
	 * @param context
	 */
	public static void startMover(Context context) {
		if (isLiveTv(context)) { 
			// only show this during live TV since the
			// activity will block user input
			SharedPreferences preferences = context.getSharedPreferences(
					ConfigActivity.PREFS_NAME, Activity.MODE_PRIVATE);
			long lastTimeRun = preferences.getLong(
					ConfigActivity.LAST_TIME_RUN, 0);
			int timing = preferences.getInt(ConfigActivity.PREFERENCE_TIMING,
					ConfigActivity.PREFERENCE_TIMING_DEFAULT);
			long currentTime = System.currentTimeMillis();
			Log.d(LOG_CAT, "startMover: "+(currentTime - lastTimeRun) * 1000 * 60+", "+timing);
			if ((currentTime - lastTimeRun) * 1000 * 60 >= timing) {
				Intent intent = new Intent(context, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);

				SharedPreferences.Editor edit = preferences.edit();
				edit.putLong(ConfigActivity.LAST_TIME_RUN, currentTime);
				edit.commit();
			}
		} else {
			Log.d(LOG_CAT, "not live tv");
		}
	}

	/**
	 * Check if live TV is the top-most app running on Google TV.
	 * 
	 * @param context
	 * @return
	 */
	private static boolean isLiveTv(Context context) {
		try {
			ActivityManager mActivityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> runningTaskInfoList = mActivityManager
					.getRunningTasks(1);
			ComponentName componentName = runningTaskInfoList.get(0).topActivity;
			String packageName = componentName.getPackageName();
			if (packageName.equals(APP_LIVE_TV)) {
				return true;
			}
		} catch (SecurityException e) {
			Log.e(LOG_CAT, "isLiveTv", e);
		}
		return false;
	}

}
