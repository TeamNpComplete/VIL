package com.vil.vil_bot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.skyfishjy.library.RippleBackground;
import com.vil.vil_bot.adapters.AdapterChat;
import com.vil.vil_bot.models.ModelMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class MainActivity extends AppCompatActivity {

    SessionsClient client;
    SessionName sessionName;

    EditText query;
    FloatingActionButton sendButton;
    String uuid;
    boolean isRecording = false;

    RecyclerView recyclerView;
    ArrayList<ModelMessage> modelMessageArrayList = new ArrayList<>();
    AdapterChat adapterChat;

    RippleBackground rippleBackground;

    Recorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rippleBackground = findViewById(R.id.ripple_effect);

        Dexter.withActivity(MainActivity.this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.areAllPermissionsGranted()){

                        }

                        if(report.isAnyPermissionPermanentlyDenied()){
                            Toast.makeText(MainActivity.this, "Permission Denied !", Toast.LENGTH_SHORT).show();
                            MainActivity.this.finish();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .onSameThread()
                .check();

        query = findViewById(R.id.edit_query);
        sendButton = findViewById(R.id.send_btn);

        uuid = UUID.randomUUID().toString();

        try{
            InputStream stream = getResources().openRawResource(R.raw.agent_credentials);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            String projectId = ((ServiceAccountCredentials)credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings settings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

            client = SessionsClient.create(settings);
            sessionName = SessionName.of(projectId, uuid);

        } catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.chat_recycler_view);
        adapterChat = new AdapterChat(this, modelMessageArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterChat);

        sendButton.setOnTouchListener(touchListener);

        setupRecorder();

        setListenerToRootView();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public void buttonClicked(View view) {
        String msg = query.getText().toString();

        rippleBackground.startRippleAnimation();

        if(msg.trim().isEmpty()){

        } else {
            adapterChat.addItem(new ModelMessage(msg, "user", "user"));
            QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(msg).setLanguageCode("en")).build();
            new RequestTask(MainActivity.this, sessionName, client, input).execute();

            recyclerView.scrollToPosition(adapterChat.getItemCount() - 1);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }catch (NullPointerException e){

            }
        }
    }

    public void callback(DetectIntentResponse response){
        if(response != null) {
            String botReply = response.getQueryResult().getFulfillmentText();
            Log.d("Bot Reply", botReply);
            query.setText("");
            adapterChat.addItem(new ModelMessage(botReply, "bot", "bot"));

            recyclerView.scrollToPosition(adapterChat.getItemCount() - 1);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }catch (NullPointerException e){

            }
        } else {
            Log.d("Bot Reply", "Null");
        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_UP){
                Toast.makeText(MainActivity.this, "Released !", Toast.LENGTH_SHORT).show();
                if(isRecording){
                    try {
                        recorder.stopRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isRecording = false;
                }
                return true;
            }
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                Toast.makeText(MainActivity.this, "Pressed !", Toast.LENGTH_SHORT).show();
                recorder.startRecording();
                isRecording = true;
                return true;
            }
            return false;
        }
    };


    public class RequestTask extends AsyncTask<Void, Void, DetectIntentResponse> {

        Activity activity;
        SessionName sessionName;
        SessionsClient client;
        QueryInput input;

        public RequestTask(Activity activity, SessionName sessionName, SessionsClient client, QueryInput input) {
            this.activity = activity;
            this.sessionName = sessionName;
            this.client = client;
            this.input = input;
        }

        @Override
        protected DetectIntentResponse doInBackground(Void... voids) {
            try{
                DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder().setSession(sessionName.toString())
                        .setQueryInput(input).build();

                return client.detectIntent(detectIntentRequest);
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(DetectIntentResponse detectIntentResponse) {
            ((MainActivity)activity).callback(detectIntentResponse);
        }
    }

    boolean isOpened = false;

    public void setListenerToRootView() {
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) {
                    
                    if (isOpened == false) {
                        recyclerView.scrollToPosition(adapterChat.getItemCount() - 1);
                    }
                    isOpened = true;
                } else if (isOpened == true) {
                    isOpened = false;
                    recyclerView.scrollToPosition(adapterChat.getItemCount() - 1);
                }
            }
        });
    }

    private void setupRecorder() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        //animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }

    @NonNull
    private File file() {
        return new File(Environment.getExternalStorageDirectory(), "kailashdabhi.wav");
    }
}