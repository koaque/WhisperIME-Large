package com.whispertflite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.whispertflite.databinding.ActivityDownloadBinding;
import com.whispertflite.utils.Downloader;

/**
 * Activity to handle initial model downloads from Hugging Face
 */
public class DownloadActivity extends Activity {

    private ActivityDownloadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Check if models are available
        boolean modelsExist = Downloader.checkModels(this);
        if (modelsExist) {
            // Models exist, show start button
            binding.downloadButton.setVisibility(View.GONE);
            binding.buttonStart.setVisibility(View.VISIBLE);
        } else {
            // Models don't exist, show download button
            binding.downloadButton.setVisibility(View.VISIBLE);
            binding.buttonStart.setVisibility(View.GONE);
        }
        
        // Check if update is needed
        if (Downloader.checkUpdate(this)) {
            binding.buttonUpdate.setVisibility(View.VISIBLE);
        }
    }
    
    public void download(View view) {
        // Start download
        binding.downloadProgress.setVisibility(View.VISIBLE);
        binding.downloadSize.setVisibility(View.VISIBLE);
        Downloader.downloadModels(this, binding);
    }
    
    public void updateModels(View view) {
        // Delete old models and start fresh download
        Downloader.deleteOldModels(this);
        download(view);
    }
    
    public void startMain(View view) {
        // Start the main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}