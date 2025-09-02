package com.whispertflite;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.whispertflite.asr.Recorder;
import com.whispertflite.asr.Whisper;
import com.whispertflite.asr.WhisperResult;
import com.whispertflite.utils.HapticFeedback;
import com.whispertflite.utils.InputLang;

import java.io.File;
import java.util.ArrayList;

public class WhisperRecognizeActivity extends AppCompatActivity {
    private static final String TAG = "WhisperRecognizeActivity";
    private static final String MODEL_PREF_KEY = "modelName";

    private ImageButton btnRecord;
    private boolean isRecording = false;
    private ImageButton btnCancel;
    private ImageButton btnModeAuto;
    private ProgressBar processingBar;
    private Recorder mRecorder;
    private Whisper mWhisper;
    private File sdcardDataFolder;
    private File selectedTfliteFile;
    private SharedPreferences sp;
    private boolean modeAuto;
    private CountDownTimer countDownTimer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sdcardDataFolder = getExternalFilesDir(null);
        selectedTfliteFile = new File(
                sdcardDataFolder,
                sp.getString(MODEL_PREF_KEY, MainActivity.LARGE_MODEL_NAME)
        );
        if (!selectedTfliteFile.exists()) {
            Intent intent = new Intent(this, DownloadActivity.class)
                    .addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String targetLang = getIntent().getStringExtra(RecognizerIntent.EXTRA_LANGUAGE);
        String langCode = sp.getString("language", "auto");
        int langToken = InputLang.getIdForLanguage(
                InputLang.getLangList(), langCode
        );
        Log.d(TAG, "default langToken " + langToken);
        if (targetLang != null) {
            langCode = targetLang.split("[-_]")[0].toLowerCase();
            langToken = InputLang.getIdForLanguage(
                    InputLang.getLangList(), langCode
            );
            Log.d(TAG, "Listening in " + langCode);
        }

        initModel(selectedTfliteFile, langToken);
        setContentView(R.layout.activity_recognize);

        // Configure window
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;

        btnCancel = findViewById(R.id.btnCancel);
        btnRecord = findViewById(R.id.btnRecord);
        btnModeAuto = findViewById(R.id.btnModeAuto);
        processingBar = findViewById(R.id.processing_bar);

        modeAuto = sp.getBoolean("imeModeAuto", false);
        btnModeAuto.setImageResource(
                modeAuto ? R.drawable.ic_auto_on_36dp : R.drawable.ic_auto_off_36dp
        );

        mRecorder = new Recorder(this);
        mRecorder.setListener(message -> {
            runOnUiThread(() -> {
                switch (message) {
                    case Recorder.MSG_RECORDING:
                        btnRecord.setBackgroundResource(
                                R.drawable.rounded_button_background_pressed
                        );
                        break;
                    case Recorder.MSG_RECORDING_DONE:
                        HapticFeedback.vibrate(this);
                        btnRecord.setBackgroundResource(
                                R.drawable.rounded_button_background
                        );
                        startTranscription();
                        break;
                    case Recorder.MSG_RECORDING_ERROR:
                        if (countDownTimer != null) countDownTimer.cancel();
                        HapticFeedback.vibrate(this);
                        btnRecord.setBackgroundResource(
                                R.drawable.rounded_button_background
                        );
                        processingBar.setProgress(0);
                        Toast.makeText(
                                this,
                                R.string.error_no_input,
                                Toast.LENGTH_SHORT
                        ).show();
                        break;
                }
            });
        });

        if (modeAuto) {
            btnRecord.setVisibility(View.GONE);
            HapticFeedback.vibrate(this);
            startRecording();
            processingBar.setProgress(100);
            countDownTimer = new CountDownTimer(30000, 1000) {
                @Override public void onTick(long l) {
                    processingBar.setProgress((int) (l / 300));
                }
                @Override public void onFinish() {}
            }.start();
        }

        btnModeAuto.setOnClickListener(v -> {
            modeAuto = !modeAuto;
            sp.edit().putBoolean("imeModeAuto", modeAuto).apply();
            btnRecord.setVisibility(modeAuto ? View.GONE : View.VISIBLE);
            btnModeAuto.setImageResource(
                    modeAuto ? R.drawable.ic_auto_on_36dp : R.drawable.ic_auto_off_36dp
            );
            if (mWhisper != null) stopTranscription();
            setResult(RESULT_CANCELED);
            finish();
        });

        btnRecord.setOnClickListener(v -> {
            if (!isRecording) {
                isRecording = true;
                HapticFeedback.vibrate(this);
                startRecording();
                processingBar.setProgress(100);
            } else {
                isRecording = false;
                if (mRecorder.isInProgress()) mRecorder.stop();
                btnRecord.setBackgroundResource(
                        R.drawable.rounded_button_background
                );
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (mWhisper != null) stopTranscription();
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void startRecording() {
        mRecorder.start();
    }

    private void initModel(File modelFile, int langToken) {
        boolean isMultilingual = !modelFile.getName().endsWith(".en.tflite");
        String vocabName = isMultilingual
                ? "filters_vocab_multilingual.bin"
                : "filters_vocab_en.bin";
        File vocab = new File(sdcardDataFolder, vocabName);
        mWhisper = new Whisper(this);
        mWhisper.loadModel(modelFile, vocab, isMultilingual);
        mWhisper.setLanguage(langToken);
        mWhisper.setListener(new Whisper.WhisperListener() {
            @Override public void onUpdateReceived(String msg) {}
            @Override public void onResultReceived(WhisperResult res) {
                runOnUiThread(() -> processingBar.setIndeterminate(false));
                String out = res.getResult().trim();
                if (res.getLanguage().equals("zh")) {
                    boolean simple = sp.getBoolean("simpleChinese", false);
                    out = simple
                            ? ZhConverterUtil.toSimple(out)
                            : ZhConverterUtil.toTraditional(out);
                }
                sendResult(out);
            }
        });
    }

    private void sendResult(String result) {
        Intent i = new Intent();
        i.putStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS,
                new ArrayList<String>() {{ add(result); }}
        );
        i.putExtra(
                RecognizerIntent.EXTRA_CONFIDENCE_SCORES,
                new float[] {1.0f}
        );
        setResult(RESULT_OK, i);
        finish();
    }

    private void startTranscription() {
        if (countDownTimer != null) countDownTimer.cancel();
        processingBar.setProgress(0);
        processingBar.setIndeterminate(true);
        mWhisper.setAction(Whisper.ACTION_TRANSCRIBE);
        mWhisper.start();
    }

    private void stopTranscription() {
        processingBar.setIndeterminate(false);
        mWhisper.stop();
    }

    @Override public void onDestroy() {
        if (mWhisper != null) mWhisper.unloadModel();
        if (mRecorder != null && mRecorder.isInProgress())
            mRecorder.stop();
        super.onDestroy();
    }
}