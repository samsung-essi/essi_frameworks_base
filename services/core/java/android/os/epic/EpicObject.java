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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.util.Log;

/** @hide */
public final class EpicObject extends IEpicObject.Stub {
	private static final String TAG = "EpicObject";

	private static final String EPIC_CLASS = "vendor.samsung_slsi.hardware.epic.V1_0.IEpicRequest";
	private static final String EPIC_HANDLE_CLASS = "vendor.samsung_slsi.hardware.epic.V1_0.IEpicHandle";

	/** @hide */
	private static boolean m_has_load = false;
	private static Class<?> m_epic_request_cls = null;
	private static Class<?> m_epic_handle_cls = null;
	private static Method m_epic_getservice_func = null;
	private static Method m_epic_init_func = null;
	private static Method m_epic_init_multi_func = null;
	private static Method m_epic_request_func = null;
	private static Method m_epic_release_func = null;
	private static Method m_epic_request_opt_func = null;
	private static Method m_epic_request_opt_multi_func = null;
	private static Method m_epic_request_conditional_func = null;
	private static Method m_epic_release_conditional_func = null;
	private static Method m_epic_perf_hint_func = null;
	private static Method m_epic_hint_release_func = null;

	private Object m_request_obj = null;
	private Object m_handle_obj = null;

	/** @hide */
	private EpicObject()
	{
		create_instance();
	}

	/** @hide */
	public EpicObject(int scenario_id)
	{
		this();

		if (m_has_load == false ||
				m_epic_getservice_func == null)
			return;

		try {
			m_request_obj = m_epic_getservice_func.invoke(null);
			if (m_request_obj != null) {
				m_handle_obj = m_epic_init_func.invoke(m_request_obj, scenario_id);
			}
		}
		catch (Exception e) {
		}
	}

	/** @hide */
	public EpicObject(int[] scenario_id_list)
	{
		this();

		if (m_has_load == false ||
				m_epic_getservice_func == null)
			return;

		try {
			m_request_obj = m_epic_getservice_func.invoke(null);
			if (m_request_obj != null) {
				ArrayList<Integer> scenario_id_short_list = new ArrayList<Integer>();
				for (int i = 0; i < scenario_id_list.length; ++i) {
					scenario_id_short_list.add(scenario_id_list[i]);
				}
				m_handle_obj = m_epic_init_multi_func.invoke(m_request_obj, scenario_id_short_list);
			}
		}
		catch (Exception e) {
		}
	}

	/** @hide */
	@Override
	public boolean acquire_lock()
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ret = ((int)m_epic_request_func.invoke(m_request_obj, m_handle_obj) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	@Override
	public boolean release_lock()
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ret = ((int)m_epic_release_func.invoke(m_request_obj, m_handle_obj) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	@Override
	public boolean acquire_lock_option(int value, int usec)
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ret = ((int)m_epic_request_opt_func.invoke(m_request_obj, m_handle_obj, value, usec) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	public boolean acquire_lock_option_multi(int[] value_list, int[] usec_list)
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int i = 0; i < value_list.length; ++i) {
				values.add(value_list[i]);
			}

			ArrayList<Integer> usecs = new ArrayList<Integer>();
			for (int i = 0; i < usec_list.length; ++i) {
				usecs.add(usec_list[i]);
			}

			ret = ((int)m_epic_request_opt_multi_func.invoke(m_request_obj, m_handle_obj, values, usecs) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	public boolean acquire_lock_conditional(String condition)
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ret = ((int)m_epic_request_conditional_func.invoke(m_request_obj, m_handle_obj, condition) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	public boolean release_lock_conditional(String condition)
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ret = ((int)m_epic_release_conditional_func.invoke(m_request_obj, m_handle_obj, condition) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	@Override
	public boolean perf_hint(String name)
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ret = ((int)m_epic_perf_hint_func.invoke(m_request_obj, m_handle_obj, name) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	@Override
	public boolean hint_release(String name)
	{
		boolean ret = false;

		if (m_has_load == false ||
				m_request_obj == null)
			return ret;

		try {
			ret = ((int)m_epic_hint_release_func.invoke(m_request_obj, m_handle_obj, name) != 0);
		}
		catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	/** @hide */
	private void create_instance()
	{
		synchronized(EpicObject.class) {
			try {
				if (m_has_load == true)
					return;

				m_epic_request_cls = Class.forName(EPIC_CLASS);
				m_epic_handle_cls = Class.forName(EPIC_HANDLE_CLASS);

				Class[] getService_args = new Class[] {};
				Class[] init_args = new Class[] {int.class};
				Class[] init_multi_args = new Class[] {ArrayList.class};
				Class[] acquire_args = new Class[] {m_epic_handle_cls};
				Class[] release_args = new Class[] {m_epic_handle_cls};
				Class[] acquire_option_args = new Class[] {m_epic_handle_cls, int.class, int.class};
				Class[] acquire_option_multi_args = new Class[] {m_epic_handle_cls, ArrayList.class, ArrayList.class};
				Class[] acquire_conditional_args = new Class[] {m_epic_handle_cls, String.class};
				Class[] release_conditional_args = new Class[] {m_epic_handle_cls, String.class};
				Class[] perf_hint_args = new Class[] {m_epic_handle_cls, String.class};
				Class[] hint_release_args = new Class[] {m_epic_handle_cls, String.class};

				m_epic_getservice_func = m_epic_request_cls.getMethod("getService", getService_args);
				m_epic_init_func = m_epic_request_cls.getMethod("init", init_args);
				m_epic_init_multi_func = m_epic_request_cls.getMethod("init_multi", init_multi_args);
				m_epic_request_func = m_epic_request_cls.getMethod("acquire_lock", acquire_args);
				m_epic_release_func = m_epic_request_cls.getMethod("release_lock", release_args);
				m_epic_request_opt_func = m_epic_request_cls.getMethod("acquire_lock_option", acquire_option_args);
				m_epic_request_opt_multi_func = m_epic_request_cls.getMethod("acquire_lock_multi_option", acquire_option_multi_args);
				m_epic_request_conditional_func = m_epic_request_cls.getMethod("acquire_lock_conditional", acquire_conditional_args);
				m_epic_release_conditional_func = m_epic_request_cls.getMethod("release_lock_conditional", release_conditional_args);
				m_epic_perf_hint_func = m_epic_request_cls.getMethod("perf_hint", perf_hint_args);
				m_epic_hint_release_func = m_epic_request_cls.getMethod("hint_release", hint_release_args);

				m_has_load = true;
			}
			catch(Exception e) {
			}
		}
	}
}
