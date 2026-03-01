/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os.epic;

import android.content.Context;
import com.android.server.SystemService;

import android.os.epic.IEpicObject;
import android.os.epic.EpicObject;
import android.os.Handler;
import android.os.HandlerThread;

import android.app.IActivityManager;
import android.app.ActivityManager;
import android.hardware.display.IDisplayManager;
import android.hardware.display.DisplayManager;
import android.content.pm.PackageManager;

import android.util.Log;

import android.os.epic.EpicDisplayListener;
import android.os.epic.EpicChromeDetector;
import android.os.SystemProperties;

/**
 * The epic manager service is responsible for coordinating epic management
 * functions on the device.
 */
public final class EpicManagerService extends SystemService
{
	private static final String TAG = "EpicManagerService";
	private static final String FEATURE_PROPERTY_KEY = "vendor.epic.feature";
	private static final long FEATURE_ACTIVITY = 0x1;
	private static final long FEATURE_WEB = 0x2;

	private final Context mContext;
	private IActivityManager mActivityManager;
	private PackageManager mPackageManager;
	private EpicChromeDetector mChromeDetector = null;
	private DisplayManager mDisplayManager;

	private EpicUidObserver mUidObserver = null;
	private DisplayManager.DisplayListener mDisplayListener = null;
	private HandlerThread mDisplayHandlerThread = null;

	/** @hide */
	public EpicManagerService(Context context) {
		super(context);
		mContext = context;
	}

	/** @hide */
	@Override
	public void onStart() {
		publishBinderService(Context.EPIC_SERVICE, new BinderService());
	}

	/** @hide */
	@Override
	public void onBootPhase(int phase) {
	}

	/** @hide */
	public void systemReady() {
		try {
			long featureValue = SystemProperties.getLong(FEATURE_PROPERTY_KEY, 0);

			if ((featureValue & FEATURE_ACTIVITY) == FEATURE_ACTIVITY) {
				mActivityManager = ActivityManager.getService();
				mPackageManager = mContext.getPackageManager();

				if ((featureValue & FEATURE_WEB) == FEATURE_WEB) {
					mChromeDetector = new EpicChromeDetector();

					mDisplayManager = (DisplayManager)mContext.getSystemService(Context.DISPLAY_SERVICE);
					mDisplayHandlerThread = new HandlerThread("DisplayChange");
					mDisplayHandlerThread.start();

					mDisplayListener = new EpicDisplayListener(mDisplayManager);
					mDisplayManager.registerDisplayListener(mDisplayListener, new Handler(mDisplayHandlerThread.getLooper()));
				}

				mUidObserver = new EpicUidObserver(mContext, mPackageManager, mActivityManager, mChromeDetector);
				mActivityManager.registerUidObserver(mUidObserver,
					ActivityManager.UID_OBSERVER_PROCSTATE | ActivityManager.UID_OBSERVER_GONE, ActivityManager.PROCESS_STATE_BOUND_TOP, null);
			}
		}
		catch (Exception e) {
			Log.i("EPICSVC", e.getMessage());
		}
	}

	/** @hide */
	private final class BinderService extends IEpicManager.Stub
	{
		@Override
		public IEpicObject Create(int scenario_id)
		{
			return new EpicObject(scenario_id);
		}

		@Override
		public IEpicObject Creates(int[] scenario_id_list)
		{
			return new EpicObject(scenario_id_list);
		}
	}
}
