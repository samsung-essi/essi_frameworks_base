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
import android.app.IActivityManager;
import android.app.ActivityManager;
import android.app.IUidObserver;
import android.content.pm.PackageManager;

import java.util.Timer;
import java.util.TimerTask;

import com.samsung.epic.Request;
import android.os.epic.EpicChromeDetector;
import android.os.epic.EpicChromeTask;

/** @hide */
public final class EpicUidObserver extends IUidObserver.Stub {
	private final Context mContext;
	private final PackageManager mPackageManager;
	private final IActivityManager mActivityManager;
	private Request mRequest;
	private String mPrevPkgName = null;
	private EpicChromeDetector mChromeDetector = null;
	private EpicChromeTask mBrowserTimerTask = null;
	private Timer mCheckTimer = null;

	private final static long DELAY_CHECK_MS = 1000;

	EpicUidObserver(Context clientContext, PackageManager packageManager, IActivityManager activityManager, EpicChromeDetector chromeDetector)
	{
		mContext = clientContext;

		mRequest = new Request(0);

		mPackageManager = packageManager;
		mActivityManager = activityManager;
		mChromeDetector = chromeDetector;

		if (mChromeDetector == null)
			return;

		if (!mChromeDetector.LinkLibrary())
			mChromeDetector = null;
		else {
			mChromeDetector.Initialize();
			mCheckTimer = new Timer();
		}
	}

	@Override
	public void onUidStateChanged(int uid, int procState, long procStateSeq, int capability)
	{
		if (procState != (int)ActivityManager.PROCESS_STATE_TOP)
			return;

		String pkgName = null;

		try
		{
			pkgName = mPackageManager.getNameForUid(uid);
		}
		catch (Exception e)
		{
			return;
		}

		if (mPrevPkgName != pkgName)
			mRequest.hint_release(mPrevPkgName);

		mRequest.perf_hint(pkgName);
		mPrevPkgName = pkgName;

		if (mChromeDetector == null)
			return;

		if (mBrowserTimerTask != null) {
			if (!mBrowserTimerTask.cancel())
				mBrowserTimerTask.setCancel();
		}

		mBrowserTimerTask = new EpicChromeTask(mChromeDetector, mBrowserTimerTask);
		mBrowserTimerTask.setCheckPkgName(pkgName);
		mBrowserTimerTask.reset();
		mCheckTimer.schedule(mBrowserTimerTask, DELAY_CHECK_MS);
	}

	@Override
	public void onUidGone(int uid, boolean disabled)
	{
		String pkgName = null;

		try
		{
			pkgName = mPackageManager.getNameForUid(uid);
		}
		catch (Exception e)
		{
			return;
		}

		if (mChromeDetector != null &&
			pkgName != null)
			mChromeDetector.RemoveUid(pkgName);
	}

	@Override
	public void onUidActive(int uid)
	{
	}

	@Override
	public void onUidIdle(int uid, boolean disabled)
	{
	}

	@Override public void onUidCachedChanged(int uid, boolean cached)
	{
	}

	@Override
	public void onUidProcAdjChanged(int uid, int adj) {
	}
}
