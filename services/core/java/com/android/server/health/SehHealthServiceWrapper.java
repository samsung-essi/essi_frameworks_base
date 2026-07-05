/*
 * Copyright (C) 2026 The LineageOS Project
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

package com.android.server.health;

import android.annotation.NonNull;
import android.hardware.health.IHealth;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IServiceCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.Trace;
import android.util.Slog;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import vendor.samsung.hardware.health.ISehHealth;
import vendor.samsung.hardware.health.ISehHealthInfoCallback;
import vendor.samsung.hardware.health.SehHealthInfo;

/**
 * Wraps the Samsung extended health HAL and delivers {@link SehHealthInfo} updates to
 * {@link BatteryService}.
 *
 * <p>{@link ISehHealth} is not a standalone service: the Samsung health HAL exposes it as a binder
 * <em>extension</em> of the AOSP {@code android.hardware.health.IHealth/default} service (attached
 * with {@code AIBinder_setExtension}). We therefore track the AOSP health service and read its
 * extension; no separate VINTF declaration or service_manager SELinux policy is required.
 *
 * <p>{@link #create} throws {@link NoSuchElementException} when the health service or its Seh
 * extension is unavailable, so callers can gracefully skip Samsung specific battery features.
 *
 * @hide
 */
public final class SehHealthServiceWrapper {
    private static final String TAG = "SehHealthServiceWrapper";
    static final String SERVICE_NAME = IHealth.DESCRIPTOR + "/default";

    /** Callback for updates from the Samsung health HAL. */
    public interface Callback {
        void onSehHealthInfoChanged(@NonNull SehHealthInfo info);
    }

    private final HandlerThread mHandlerThread = new HandlerThread("SehHealthServiceBinder");
    private final AtomicReference<ISehHealth> mLastService = new AtomicReference<>();
    private final IServiceCallback mServiceCallback = new ServiceCallback();
    private final ISehHealthInfoCallback mInfoCallback = new HealthInfoCallback();
    private final Callback mCallback;

    private SehHealthServiceWrapper(@NonNull Callback callback)
            throws RemoteException, NoSuchElementException {
        mCallback = callback;

        traceBegin("SehHealthInitGetService");
        final IBinder healthBinder;
        try {
            healthBinder = ServiceManager.waitForDeclaredService(SERVICE_NAME);
        } finally {
            traceEnd();
        }
        if (healthBinder == null) {
            throw new NoSuchElementException(
                    "IHealth service instance isn't available. Perhaps no permission?");
        }
        final ISehHealth sehService = getSehExtension(healthBinder);
        if (sehService == null) {
            throw new NoSuchElementException(
                    "ISehHealth extension isn't available on the health HAL");
        }
        mLastService.set(sehService);

        mHandlerThread.start();
        registerCallback(sehService);

        traceBegin("SehHealthInitRegisterNotification");
        try {
            ServiceManager.registerForNotifications(SERVICE_NAME, mServiceCallback);
        } finally {
            traceEnd();
        }
        Slog.i(TAG, "health: SehHealthServiceWrapper listening to health HAL extension");
    }

    /**
     * Create a new wrapper, registering {@code callback} for updates.
     *
     * @throws NoSuchElementException if the health HAL or its Seh extension is not available.
     */
    public static SehHealthServiceWrapper create(@NonNull Callback callback)
            throws RemoteException, NoSuchElementException {
        return new SehHealthServiceWrapper(callback);
    }

    /** Reads the {@link ISehHealth} extension from the given health service binder. */
    private static ISehHealth getSehExtension(@NonNull IBinder healthBinder) {
        try {
            final IBinder extension = Binder.allowBlocking(healthBinder).getExtension();
            return ISehHealth.Stub.asInterface(extension);
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to get ISehHealth extension from health HAL", e);
            return null;
        }
    }

    /** Requests the HAL to push a fresh {@link SehHealthInfo}. */
    public void scheduleUpdate() {
        getHandler().post(() -> {
            final ISehHealth service = mLastService.get();
            if (service == null) {
                Slog.e(TAG, "no seh health service");
                return;
            }
            try {
                service.update();
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.e(TAG, "Cannot call update on seh health HAL", e);
            }
        });
    }

    /**
     * Enables or disables a charging feature identified by its param {@code offset}.
     *
     * @param offset the param offset, or a negative value to no-op.
     * @param enable value to write for the param.
     */
    public void sehWriteEnableToParam(int offset, boolean enable) {
        if (offset < 0) {
            return;
        }
        final ISehHealth service = mLastService.get();
        if (service == null) {
            Slog.e(TAG, "no seh health service");
            return;
        }
        try {
            service.sehWriteEnableToParam(offset, enable);
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "Cannot call sehWriteEnableToParam on seh health HAL", e);
        }
    }

    private Handler getHandler() {
        return mHandlerThread.getThreadHandler();
    }

    private void registerCallback(@NonNull ISehHealth service) {
        try {
            service.registerCallback(mInfoCallback);
            service.update();
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "Failed to register seh health callback", e);
        }
    }

    private static void traceBegin(String name) {
        Trace.traceBegin(Trace.TRACE_TAG_SYSTEM_SERVER, name);
    }

    private static void traceEnd() {
        Trace.traceEnd(Trace.TRACE_TAG_SYSTEM_SERVER);
    }

    private final class HealthInfoCallback extends ISehHealthInfoCallback.Stub {
        @Override
        public void healthInfoChanged(SehHealthInfo info) {
            mCallback.onSehHealthInfoChanged(info);
        }

        @Override
        public int getInterfaceVersion() {
            return ISehHealthInfoCallback.VERSION;
        }

        @Override
        public String getInterfaceHash() {
            return ISehHealthInfoCallback.HASH;
        }
    }

    private final class ServiceCallback extends IServiceCallback.Stub {
        @Override
        public void onRegistration(String name, @NonNull final IBinder newBinder) {
            if (!SERVICE_NAME.equals(name)) {
                return;
            }
            getHandler().post(() -> {
                final ISehHealth newService = getSehExtension(newBinder);
                if (newService == null) {
                    Slog.e(TAG, "Health HAL re-registered without an ISehHealth extension");
                    return;
                }
                mLastService.set(newService);
                Slog.i(TAG, "Health HAL re-registered; re-acquired ISehHealth extension");
                registerCallback(newService);
            });
        }
    }
}
