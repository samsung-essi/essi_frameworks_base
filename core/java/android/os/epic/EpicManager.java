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

import java.util.List;

import android.annotation.SystemService;
import android.content.Context;
import android.os.RemoteException;

import android.os.epic.IEpicManager;
import android.os.epic.IEpicObject;

/** @hide */
@SystemService(Context.EPIC_SERVICE)
public final class EpicManager {
	private static final String TAG = "EpicManager";

	final IEpicManager mService;

	/** @hide */
	public EpicManager(IEpicManager service)
	{
		mService = service;
	}

	/** @hide */
	IEpicObject Create(int scenario_id)
	{
		IEpicObject ret = null;

		if (mService == null)
			return ret;

		try {
			ret = mService.Create(scenario_id);
		}
		catch (RemoteException e) {
		}

		return ret;
	}

	/** @hide */
	IEpicObject Creates(int[] scenario_id_list)
	{
		IEpicObject ret = null;

		if (mService == null)
			return ret;

		try {
			ret = mService.Creates(scenario_id_list);
		}
		catch (RemoteException e) {
		}

		return ret;
	}
}
