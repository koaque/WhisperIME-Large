package com.whispertflite;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.whispertflite.asr.Recorder;
import com.whispertflite.asr.Whisper;
import com.whispertflite.asr.WhisperResult;
import com.whispertflite.utils.HapticFeedback;
import com.whispertflite.utils.InputLang;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // single large model
    public static final String LARGE_MODEL_NAME = "whisper-large-v3.tflite";
    public static final String VOCAB_FILE       = "filters_vocab_multilingual.bin";

    private ProgressBar processingBar;
    private TextView tvStatus, tvResult;
    private Spinner spinnerLang;
    private FloatingActionButton fabCopy;
    private Recorder mRecorder;
    private Whisper mWhisper;
    private File sdcard;
    private File modelFile;
    private SharedPreferences sp;
    private boolean isRecording = false;
    private CountDownTimer countDownTimer;
    private int langToken;
    private CheckBox cbTranslate, cbSimpleChinese, cbTTS;

    @SuppressLint("ClickableViewAccessibility")
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sdcard = getExternalFilesDir(null);
        modelFile = new File(sdcard,
                sp.getString("modelName", LARGE_MODEL_NAME));
        if (!modelFile.exists()) {
            startActivity(new Intent(this, DownloadActivity.class));
            finish(); return;
        }

        processingBar = findViewById(R.id.processing_bar);
        tvStatus      = findViewById(R.id.tvStatus);
        tvResult      = findViewById(R.id.tvResult);
        spinnerLang   = findViewById(R.id.spnrLanguage);
        fabCopy       = findViewById(R.id.fabCopy);
        cbTranslate   = findViewById(R.id.mode_translate);
        cbSimpleChinese = findViewById(R.id.mode_simple_chinese);
        cbTTS         = findViewById(R.id.mode_tts);

        // Copy to clipboard
        fabCopy.setOnClickListener(v -> {
            String txt = tvResult.getText().toString().trim();
            ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText("out", txt));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        });

        // Language spinner
        String[] langs = getResources().getStringArray(R.array.top40_languages);
        spinnerLang.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, langs));
        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                langToken = InputLang.getIdForLanguage(
                        InputLang.getLangList(), langs[pos]);
                sp.edit().putString("language", langs[pos]).apply();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        langToken = InputLang.getIdForLanguage(
                InputLang.getLangList(), sp.getString("language","auto"));

        // Recorder & Whisper
        mRecorder = new Recorder(this);
        mRecorder.setListener(msg -> {
            switch(msg) {
                case Recorder.MSG_RECORDING:
                    runOnUiThread(() -> processingBar.setIndeterminate(false));
                    break;
                case Recorder.MSG_RECORDING_DONE:
                    HapticFeedback.vibrate(this);
                    runOnUiThread(() -> processingBar.setIndeterminate(true));
                    startProcessing(cbTranslate.isChecked()
                            ? Whisper.ACTION_TRANSLATE
                            : Whisper.ACTION_TRANSCRIBE);
                    break;
                case Recorder.MSG_RECORDING_ERROR:
                    if (countDownTimer!=null) countDownTimer.cancel();
                    runOnUiThread(() -> tvStatus.setText(R.string.error_no_input));
                    break;
            }
        });

        initModel();

        // Toggle capture
        findViewById(R.id.btnRecord).setOnClickListener(v -> {
            if (!isRecording) {
                isRecording = true;
                mRecorder.start();
                countDownTimer = new CountDownTimer(30000,1000){
                    @Override public void onTick(long ms){
                        processingBar.setProgress((int)(ms/300));
                    }
                    @Override public void onFinish(){}
                }.start();
            } else {
                isRecording = false;
                mRecorder.stop();
                if (countDownTimer!=null) countDownTimer.cancel();
            }
        });
    }

    private void initModel() {
        mWhisper = new Whisper(this);
        mWhisper.loadModel(modelFile,
                new File(sdcard, VOCAB_FILE), true);
        mWhisper.setListener(new Whisper.WhisperListener(){
            @Override public void onUpdateReceived(String u){}
            @Override public void onResultReceived(WhisperResult r){
                runOnUiThread(() -> processingBar.setIndeterminate(false));
                String out = r.getResult().trim();
                if (r.getLanguage().equals("zh") && cbSimpleChinese.isChecked())
                    out = ZhConverterUtil.toSimple(out);
                tvResult.append(out);
            }
        });
    }

    private void startProcessing(Whisper.Action a) {
        runOnUiThread(() -> {
            processingBar.setProgress(0);
            processingBar.setIndeterminate(true);
        });
        mWhisper.setAction(a);
        mWhisper.setLanguage(langToken);
        mWhisper.start();
    }
}
