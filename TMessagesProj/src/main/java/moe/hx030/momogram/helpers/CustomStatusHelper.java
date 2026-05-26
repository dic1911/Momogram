package moe.hx030.momogram.helpers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.telegram.messenger.DispatchQueue;
import org.telegram.ui.Components.AnimatedFileDrawable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import moe.hx030.momogram.NekoXConfig;
import moe.hx030.momogram.NekoXConfig.CustomEmojiStatusText;

public class CustomStatusHelper {
    private static OkHttpClient okHttpClient;
    public static final int DEFAULT_BACKGROUND_COLOR = 9803157;
    private static final ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return "until".equalsIgnoreCase(f.getName());
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };
    private static final Gson gson = new GsonBuilder()
            .addDeserializationExclusionStrategy(exclusionStrategy)
            .addSerializationExclusionStrategy(exclusionStrategy)
            .create();
    private static Boolean updating = false;

    private static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            var builder = new OkHttpClient.Builder();
            builder.connectTimeout(120, TimeUnit.SECONDS);
            builder.readTimeout(120, TimeUnit.SECONDS);
            builder.writeTimeout(120, TimeUnit.SECONDS);
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    public static void updateCustomStatus() {
        if (updating) return;
        updating = true;
        getOkHttpClient();
        new Thread(() -> {
            Call c = okHttpClient.newCall(new Request.Builder()
                    .get()
                    .url("https://raw.githubusercontent.com/im030/Momogram/refs/heads/test/metadata/custom_status.json")
                    .build());
            try (Response r = c.execute()) {
                String body = r.body().string();
                CustomEmojiStatusText[] customStatuses = gson.fromJson(body, NekoXConfig.CustomEmojiStatusText[].class);
                synchronized (NekoXConfig.customStatus) {
                    for (CustomEmojiStatusText s : customStatuses) {
                        if (s.center_color == 0) s.center_color = DEFAULT_BACKGROUND_COLOR;
                        NekoXConfig.customStatus.put(s.id, s);
                    }
                }
                Log.i("030-status", "updated custom status data");
            } catch (Exception e) {
                Log.e("030-status", "failed to fetch custom status update", e);
            }
            updating = false;
        }).start();
    }
}
