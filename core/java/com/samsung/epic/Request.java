package com.samsung.epic;

import android.os.ServiceManager;
import android.os.RemoteException;

import android.os.epic.IEpicManager;
import android.os.epic.IEpicObject;

public class Request {
	private final static String TAG = "EpicRequest";
	private static boolean mHasLoad = false;
	private static IEpicManager mEpicManager;

	private IEpicObject mEpicObject;

	private Request() {
		get_service();
	}

	public Request(int scenario_id)
	{
		this();

		try {
			mEpicObject = mEpicManager.Create(scenario_id);
		}
		catch (Exception e) {
			mEpicObject = null;
		}
	}

	public Request(int[] scenario_id_list)
	{
		this();

		try {
			mEpicObject = mEpicManager.Creates(scenario_id_list);
		}
		catch (Exception e) {
			mEpicObject = null;
		}
	}

	public boolean acquire_lock() {
		boolean ret = false;

		if (mEpicObject == null)
			return ret;

		try {
			ret = mEpicObject.acquire_lock();
		}
		catch (RemoteException e) {
		}

		return ret;
	}

	public boolean release_lock() {
		boolean ret = false;

		if (mEpicObject == null)
			return false;

		try {
			ret = mEpicObject.release_lock();
		}
		catch (RemoteException e) {
		}

		return ret;
	}

	public boolean acquire_lock(int value, int usec) {
		boolean ret = false;

		if (mEpicObject == null)
			return false;

		try {
			ret = mEpicObject.acquire_lock_option(value, usec);
		}
		catch (RemoteException e) {
		}

		return ret;
	}

	public boolean acquire_lock(int[] value_list, int[] usec_list) {
		boolean ret = false;

		if (mEpicObject == null)
			return false;

		try {
			ret = mEpicObject.acquire_lock_option_multi(value_list, usec_list);
		}
		catch (RemoteException e) {
		}

		return ret;
	}

	public boolean perf_hint(String name)
	{
		boolean ret = false;

		if (mEpicObject == null)
			return false;

		try {
			ret = mEpicObject.perf_hint(name);
		}
		catch (RemoteException e) {
		}

		return ret;
	}

	public boolean hint_release(String name)
	{
		boolean ret = false;

		if (mEpicObject == null)
			return false;

		try {
			ret = mEpicObject.hint_release(name);
		}
		catch (RemoteException e) {
		}

		return ret;
	}

	private void get_service()
	{
		synchronized(Request.class) {
			try {
				if (mHasLoad == true)
					return;

				mEpicManager = IEpicManager.Stub.asInterface(ServiceManager.getService("epic"));

				mHasLoad = true;
			}
			catch (Exception e){
				mEpicManager = null;
			}
		}
	}
}
