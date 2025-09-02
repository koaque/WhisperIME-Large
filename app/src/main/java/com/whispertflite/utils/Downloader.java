package com.whispertflite.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.whispertflite.R;
import com.whispertflite.databinding.ActivityDownloadBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Downloader {
    private static final String TAG = "Downloader";

    // Only large Whisper model
    static final String modelName = "whisper-large-v3.tflite";
    static final String modelURL  = "https://huggingface.co/cik009/whisper/resolve/43804efaf0605cc62d7f132fa94901731733c75b/whisper-large-v3.tflite";
    // Exact MD5 and size for whisper-large-v3
    static final String modelMD5   = "B346515BC5E3D8178680577DA0CC2D99";
    static final long   modelSize  = 1556766936L;

    static long downloadedSize = 0L;
    static boolean modelFinished = false;

    public static boolean checkUpdate(final Activity activity) {
        File modelFile = new File(activity.getExternalFilesDir(null), modelName);
        return !modelFile.exists();
    }

    public static boolean checkModels(final Activity activity) {
        copyAssetsToSdcard(activity);
        File modelFile = new File(activity.getExternalFilesDir(null), modelName);
        String calcMD5 = "";
        if (modelFile.exists()) {
            try {
                calcMD5 = calculateMD5(modelFile.getPath());
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        if (modelFile.exists() && !calcMD5.equalsIgnoreCase(modelMD5)) {
            Log.d(TAG, String.format("Checksum mismatch during check: expected %s, got %s", modelMD5, calcMD5));
            modelFile.delete();
            modelFinished = false;
        }
        return calcMD5.equalsIgnoreCase(modelMD5);
    }

    public static void deleteOldModels(final Activity activity) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        sp.edit().remove("modelName").apply();
        sp.edit().remove("recognitionServiceModelName").apply();
        File modelFile = new File(activity.getExternalFilesDir(null), modelName);
        if (modelFile.exists()) modelFile.delete();
    }

    public static void downloadModels(final Activity activity, ActivityDownloadBinding binding) {
        checkModels(activity);
        binding.downloadProgress.setProgress(0);
        binding.downloadButton.setEnabled(false);

        File modelFile = new File(activity.getExternalFilesDir(null), modelName);
        if (!modelFile.exists()) {
            modelFinished = false;
            Log.d(TAG, "Large model not found locally, starting download");
            Thread thread = new Thread(() -> {
                try {
                    URL url = new URL(modelURL);
                    URLConnection ucon = url.openConnection();
                    ucon.setReadTimeout(5000);
                    ucon.setConnectTimeout(10000);

                    InputStream is = ucon.getInputStream();
                    BufferedInputStream inStream = new BufferedInputStream(is, 5 * 1024);

                    modelFile.createNewFile();
                    FileOutputStream outStream = new FileOutputStream(modelFile);
                    byte[] buff = new byte[5 * 1024];
                    int len;
                    while ((len = inStream.read(buff)) != -1) {
                        outStream.write(buff, 0, len);
                        downloadedSize = modelFile.length();
                        Log.d(TAG, String.format("Downloaded %d/%d bytes", downloadedSize, modelSize));
                        activity.runOnUiThread(() -> {
                            binding.downloadSize.setText((downloadedSize / 1024 / 1024) + " MB");
                            binding.downloadProgress.setProgress((int) ((double) downloadedSize / modelSize * 100));
                        });
                    }
                    outStream.flush();
                    outStream.close();
                    inStream.close();

                    String calcMD5 = calculateMD5(modelFile.getPath());
                    Log.d(TAG, String.format("MD5 check: expected %s, got %s", modelMD5, calcMD5));
                    if (!calcMD5.equalsIgnoreCase(modelMD5)) {
                        modelFile.delete();
                        modelFinished = false;
                        activity.runOnUiThread(() -> {
                            String msg = String.format("Download error: checksum mismatch. Got %s", calcMD5);
                            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                            binding.downloadButton.setEnabled(true);
                        });
                    } else {
                        modelFinished = true;
                        activity.runOnUiThread(() -> binding.buttonStart.setVisibility(View.VISIBLE));
                    }
                } catch (NoSuchAlgorithmException | IOException e) {
                    modelFile.delete();
                    modelFinished = false;
                    Log.e(TAG, "Download error: ", e);
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        binding.downloadButton.setEnabled(true);
                    });
                }
            });
            thread.start();
        } else {
            downloadedSize = modelSize;
            modelFinished = true;
            activity.runOnUiThread(() -> binding.buttonStart.setVisibility(View.VISIBLE));
        }
    }

    public static String calculateMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new BufferedInputStream(new FileInputStream(filePath), 8192)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] hash = md.digest();
        return String.format("%032x", new BigInteger(1, hash));
    }

    public static void copyAssetsToSdcard(Context context) {
        String[] extensions = {"bin"};
        File sdcardDataFolder = context.getExternalFilesDir(null);
        AssetManager assetManager = context.getAssets();

        try {
            String[] assetFiles = assetManager.list("");
            if (assetFiles == null) return;
            for (String assetFileName : assetFiles) {
                if (assetFileName.endsWith(".bin")) {
                    File outFile = new File(sdcardDataFolder, assetFileName);
                    if (outFile.exists()) break;
                    try (InputStream in = assetManager.open(assetFileName);
                         OutputStream out = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Asset copy failed: ", e);
        }
    }
}