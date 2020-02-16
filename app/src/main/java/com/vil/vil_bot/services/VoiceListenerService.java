package com.vil.vil_bot.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vil.vil_bot.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class VoiceListenerService extends Service {

    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;

    boolean isRecording = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechListener());

        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.forLanguageTag(MainActivity.langCode));
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000);

        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        Log.e("CHECK_LANG", "OnCreate");
        Toast.makeText(this, "OnCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

        Log.e("CHECK_LANG", "OnStart");
        Toast.makeText(this, "OnStart", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mSpeechRecognizer != null)
            mSpeechRecognizer.destroy();
    }

    protected class SpeechListener implements RecognitionListener {
        @Override
        public void onBeginningOfSpeech() {
            //recorder.startRecording();
            Log.e("CHECK_LANG", "Started Recording");
            //Toast.makeText(this, "OnCreate", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
//            try {
//                recorder.stopRecording();
                Log.e("CHECK_LANG", "Stopped Recording");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.e("CHECK_LANG", "Ready for Speech");
            //Toast.makeText(this, "OnCreate", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {

            Log.e("CHECK_LANG", "OnResults");
            Intent intent = new Intent();
            intent.setAction("textrecieved");
            intent.putExtra("text", results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
            sendBroadcast(intent);

            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

            //recorder.startRecording();
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

    }

}
