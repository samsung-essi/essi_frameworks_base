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

import android.os.epic.EpicChromeDetector;

import com.samsung.epic.Request;

import java.util.Timer;
import java.util.TimerTask;

/** @hide */
public final class EpicChromeTask extends TimerTask {
	private EpicChromeDetector mDetector = null;
	private Request mHandle = null;
	private Request mRequest;

	private String mCheckPkgName = null;
	private boolean mCancel = false;
	private boolean mAcquired = false;

	public EpicChromeTask(EpicChromeDetector detector, EpicChromeTask task)
	{
		mDetector = detector;
		mHandle = new Request(100300);

		if (task != null)
			mAcquired = task.mAcquired;
	}

	public void setCheckPkgName(String pkgName)
	{
		synchronized (this) {
			mCheckPkgName = pkgName;
		}
	}

	public String getCheckPkgName()
	{
		return mCheckPkgName;
	}

	public void reset()
	{
		mCancel = false;
	}

	public void setCancel()
	{
		mCancel = true;
	}

	@Override
	public void run() {
		synchronized (this) {
			if (mDetector == null ||
					mCancel)
				return;

			boolean ret = mDetector.CheckChromeBrowser(mCheckPkgName);

			if (mCancel)
				return;

			if (ret) {
				if (!mAcquired)
					mHandle.acquire_lock();

				mAcquired = true;
			}
			else {
				if (mAcquired)
					mHandle.release_lock();

				mAcquired = false;
			}
		}
	}
}
