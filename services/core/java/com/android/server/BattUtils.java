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

package com.android.server;

import android.annotation.Nullable;
import android.util.Slog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Small helper for reading/writing Samsung battery related sysfs and efs nodes.
 *
 * <p>This mirrors the {@code BattUtils} used by Samsung's {@code BatteryService}. Writing to these
 * nodes from {@code system_server} additionally requires the corresponding SELinux policy in the
 * device tree.
 *
 * @hide
 */
public final class BattUtils {
    private static final String TAG = "BattUtils";

    private BattUtils() {}

    /** @return {@code true} if {@code path} exists and is readable. */
    public static boolean isFileSupported(String path) {
        final boolean supported = new File(path).exists();
        if (!supported) {
            Slog.d(TAG, path + " is not found");
        }
        return supported;
    }

    /** Writes the decimal representation of {@code value} to {@code path}. */
    public static boolean writeNode(long value, String path) {
        return writeNode(path, Long.toString(value));
    }

    /** Writes {@code "1"} when {@code enable} is {@code true}, otherwise {@code "0"}. */
    public static boolean writeNode(String path, boolean enable) {
        return writeNode(path, enable ? "1" : "0");
    }

    /** Writes {@code value} to {@code path}. */
    public static boolean writeNode(String path, String value) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(value.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "Failed to write \"" + value + "\" to " + path, e);
            return false;
        }
    }

    /**
     * Reads {@code path} and returns its trimmed contents, or {@code null} if it could not be read.
     */
    @Nullable
    public static String readNode(String path, boolean trim) {
        try {
            final String data = new String(Files.readAllBytes(Paths.get(path)),
                    StandardCharsets.UTF_8);
            return trim ? data.trim() : data;
        } catch (IOException e) {
            Slog.e(TAG, "Failed to read " + path, e);
            return null;
        }
    }

    /**
     * Reads {@code path} and parses it as a {@code long}, or returns {@code -1} on any failure.
     */
    public static long readNodeAsLong(String path) {
        final String data = readNode(path, true);
        if (data == null || data.isEmpty()) {
            return -1;
        }
        try {
            return Long.parseLong(data);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Failed to parse \"" + data + "\" from " + path, e);
            return -1;
        }
    }
}
