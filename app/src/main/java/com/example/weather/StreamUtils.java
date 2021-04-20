package com.example.weather;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {
    private static final String TAG = StreamUtils.class.getSimpleName();

    @Nullable
    public static String streamToString(@NonNull InputStream inputStreamm) {
        StringBuilder buffer;
        try {
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(inputStreamm));
            buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "streamToString: got IOException", e);
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
