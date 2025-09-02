package com.whispertflite;

import android.inputmethodservice.InputMethodService;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import com.whispertflite.asr.Recorder;
import com.whispertflite.asr.Whisper;
import com.whispertflite.asr.WhisperResult;
import com.whispertflite.utils.HapticFeedback;

import java.io.File;

public class WhisperInputMethodService extends InputMethodService {
    private static final String LARGE_MODEL_NAME = "whisper-large-v3.tflite";
    private static final String VOCAB_FILE       = "filters_vocab_multilingual.bin";

    private ImageButton btnRecord;
    private Recorder mRecorder;
    private Whisper mWhisper;
    private ProgressBar processingBar;
    private TextView tvStatus;
    private boolean isRecording = false;
    private CountDownTimer countDownTimer;
    private Handler handler = new Handler();

    @Override
    public View onCreateInputView() {
        View v = getLayoutInflater().inflate(R.layout.voice_service, null);
        btnRecord     = v.findViewById(R.id.btnRecord);
        processingBar = v.findViewById(R.id.processing_bar);
        tvStatus      = v.findViewById(R.id.tv_status);

        File model = new File(getExternalFilesDir(null),
                getSharedPreferences("default", MODE_PRIVATE)
                        .getString("modelName", LARGE_MODEL_NAME));
        if (!model.exists()) {
            switchToPreviousInputMethod();
            startActivity(new Intent(this, DownloadActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return v;
        }

        mRecorder = new Recorder(this);
        mRecorder.setListener(msg -> {
            if (Recorder.MSG_RECORDING_DONE.equals(msg)) {
                handler.post(() -> {
                    HapticFeedback.vibrate(this);
                    startTranscription();
                });
            }
        });

        btnRecord.setOnClickListener(ok -> {
            if (!isRecording) {
                isRecording = true;
                mRecorder.start();
                processingBar.setProgress(100);
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

        mWhisper = new Whisper(this);
        mWhisper.loadModel(model,
                new File(getExternalFilesDir(null), VOCAB_FILE), true);
        mWhisper.setListener(new Whisper.WhisperListener(){
            @Override public void onUpdateReceived(String u){}
            @Override public void onResultReceived(WhisperResult r){
                handler.post(() -> processingBar.setIndeterminate(false));
                String out = r.getResult().trim();
                getCurrentInputConnection().commitText(out + " ",1);
                if (isRecording) {
                    isRecording = false;
                    switchToPreviousInputMethod();
                }
            }
        });

        return v;
    }

    private void startTranscription() {
        if (countDownTimer!=null) countDownTimer.cancel();
        processingBar.setProgress(0);
        processingBar.setIndeterminate(true);
        mWhisper.setAction(Whisper.ACTION_TRANSCRIBE);
        mWhisper.start();
    }

    @Override public void onDestroy() {
        if (mWhisper!=null) mWhisper.unloadModel();
        if (mRecorder!=null && mRecorder.isInProgress()) mRecorder.stop();
        super.onDestroy();
    }
}
