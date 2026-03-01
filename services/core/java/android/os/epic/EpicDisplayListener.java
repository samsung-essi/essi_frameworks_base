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

import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.FrameMetrics;

import android.util.Log;

/** @hide */
public final class EpicDisplayListener implements DisplayManager.DisplayListener {
	private DisplayManager mDisplayManager;

	public EpicDisplayListener(DisplayManager manager)
	{
		mDisplayManager = manager;
	}

	@Override public void onDisplayAdded(int displayId) {
	}

	@Override public void onDisplayRemoved(int displayId) {
	}

	@Override public void onDisplayChanged(int displayId) {
		if (displayId == Display.INVALID_DISPLAY)
			return;

		Display changedDisplay = mDisplayManager.getDisplay(displayId);
		if (changedDisplay == null)
			return;

		Display.Mode activeMode = changedDisplay.getMode();
		if (activeMode == null)
			return;

		float displayRefreshRate = activeMode.getRefreshRate();
		int displayStatus = changedDisplay.getState();

		switch (displayStatus) {
			case Display.STATE_OFF:
				break;
			case Display.STATE_ON:
				break;
			default:
				break;
		}
	}
}
