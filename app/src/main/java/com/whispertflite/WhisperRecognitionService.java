package com.whispertflite;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.whispertflite.asr.Recorder;
import com.whispertflite.asr.Whisper;
import com.whispertflite.asr.WhisperResult;
import com.whispertflite.utils.InputLang;

import java.io.File;
import java.util.ArrayList;

public class WhisperRecognitionService extends RecognitionService {
    private static final String TAG = "WhisperSvc";
    private static final String LARGE_MODEL_NAME = "whisper-large-v3.tflite";
    private static final String VOCAB_FILE       = "filters_vocab_multilingual.bin";

    private Callback callbackRef;
    private Whisper mWhisper;
    private Recorder mRecorder;

    @Override
    protected void onStartListening(Intent intent, Callback callback) {
        callbackRef = callback;
        File sd = getExternalFilesDir(null);
        File model = new File(sd,
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("recognitionServiceModelName", LARGE_MODEL_NAME)
        );
        if (!model.exists()) {
            try {
                callback.error(SpeechRecognizer.ERROR_CLIENT);
            } catch (RemoteException e) {
                Log.e(TAG, "Error reporting missing model", e);
            }
            return;
        }

        int langToken = InputLang.getIdForLanguage(
                InputLang.getLangList(),
                intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)
                        .split("[-_]")[0]
        );

        initModel(model, langToken);
        startRecording();
    }

    private void initModel(File modelFile, int langToken) {
        mWhisper = new Whisper(this);
        mWhisper.loadModel(modelFile,
                new File(getExternalFilesDir(null), VOCAB_FILE), true);
        mWhisper.setLanguage(langToken);
        mWhisper.setListener(new Whisper.WhisperListener() {
            @Override public void onUpdateReceived(String msg) {}
            @Override public void onResultReceived(WhisperResult result) {
                ArrayList<String> out = new ArrayList<>();
                String text = result.getResult().trim();
                if ("zh".equals(result.getLanguage())) {
                    text = ZhConverterUtil.toTraditional(text);
                }
                out.add(text);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION, out);
                try {
                    callbackRef.results(bundle);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to send results", e);
                }
            }
        });
    }

    private void startRecording() {
        mRecorder = new Recorder(this);
        mRecorder.setListener(msg -> {
            if (Recorder.MSG_RECORDING_DONE.equals(msg)) {
                startTranscription();
            }
        });
        mRecorder.start();
    }

    private void startTranscription() {
        mWhisper.setAction(Whisper.ACTION_TRANSCRIBE);
        mWhisper.start();
    }

    @Override
    protected void onStopListening(Callback callback) {
        if (mRecorder != null) mRecorder.stop();
    }

    @Override
    protected void onCancel(Callback callback) {
        if (mRecorder != null) mRecorder.stop();
    }

    @Override
    public void onDestroy() {
        if (mWhisper != null) mWhisper.unloadModel();
        super.onDestroy();
    }
}