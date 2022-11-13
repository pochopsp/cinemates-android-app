package it.unina.cinemates.cloudservices.cloudinary;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.unina.cinemates.BuildConfig;

public class CloudinaryHelper {

    public static void config(Context context) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);
        MediaManager.init(context, config);
    }

    public static String imagePath(String relativePath) {
        return MediaManager.get().url().generate(relativePath).replace("http", "https");
    }

    public static String imagePath(Integer relativePath) {
        if (relativePath == null)
            return MediaManager.get().url().generate("").replace("http", "https");
        else
            return MediaManager.get().url().generate(relativePath.toString()).replace("http", "https");
    }

    public static void uploadImageWithIntegerId(Uri filePath, Integer id, OnSuccessfulUploadAction onSuccessfulUploadAction, String TAG) {

        MediaManager.get().upload(filePath)
                .option("public_id", id.toString())
                .option("connect_timeout", 10000)
                .option("read_timeout", 10000)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.e(TAG, "starting upload to cloudinary...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.e(TAG, "upload to cloudinary successful");
                        onSuccessfulUploadAction.perform();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.e(TAG, error.getDescription());
                    }
                })
                .dispatch();
    }

    public static void deleteImageWithIntegerId(Integer id, String TAG) {
        if (id != null) {
            new Thread(() -> {
                Map deleteParams = ObjectUtils.asMap("invalidate", true);
                try {
                    MediaManager.get().getCloudinary().uploader().destroy(id.toString(), deleteParams);
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }).start();
        }
    }
}
