// WhisperRecognitionServiceSettingsActivity.java
package com.whispertflite;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.whispertflite.utils.Downloader;

import java.io.File;
import java.util.Collections;

public class WhisperRecognitionServiceSettingsActivity extends AppCompatActivity {
    private static final String LARGE_MODEL_NAME = "whisper-large-v3.tflite";

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_recognition_service_settings);

        ActionBar ab = getSupportActionBar();
        if (ab!=null) ab.setDisplayHomeAsUpEnabled(true);

        if (!Downloader.checkModels(this)) {
            startActivity(new Intent(this, DownloadActivity.class));
            finish();
            return;
        }

        Spinner spnr = findViewById(R.id.spnrTfliteFiles);
        File f = new File(getExternalFilesDir(null),
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("recognitionServiceModelName", LARGE_MODEL_NAME));
        spnr.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Collections.singletonList(f)));
        spnr.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        if (i.getItemId()==android.R.id.home) {
            finish(); return true;
        }
        return super.onOptionsItemSelected(i);
    }
}
