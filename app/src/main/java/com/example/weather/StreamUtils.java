package com.example.weather;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {
    private static final String TAG = Thread.currentThread().getStackTrace()[2].getClassName();

    @Nullable
    public static String StreamToString(InputStream inputStreamm) {
        StringBuilder buffer = null;
        try {
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(inputStreamm));
            buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG + Thread.currentThread().getStackTrace()[2].getMethodName(), "Stream conversion Error");
            return null;
        }
        return buffer.toString();
    }

    public static void closeAll(@Nullable Closeable... closeables) throws IOException {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                closeable.close();
            }
        }
    }
}
