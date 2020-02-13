package com.vil.vil_bot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    SessionsClient client;
    SessionName sessionName;

    EditText query;
    String uuid;
    String langCode;

    RecyclerView recyclerView;
    ArrayList<ModelMessage> modelMessageArrayList = new ArrayList<>();
    AdapterChat adapterChat;

    RippleBackground rippleBackground;
    FloatingActionButton sendButton;
    SQLiteDatabase database;

    boolean isRecording = true;

    Recorder recorder;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menulist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.changeLanguage:
                Intent intent = new Intent(this, ChangeLanguage.class);
                startActivity(intent);
                return true;
            case R.id.aboutUs:
                //about us code here
                return true;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        langCode = i.getStringExtra("langCode");

        if(langCode == null){
            langCode = getSharedPreferences("BOT_CONFIG", MODE_PRIVATE).getString("langCode", null);
            if(langCode == null){
                langCode = "en-IN";
                getSharedPreferences("BOT_CONFIG", MODE_PRIVATE).edit().putString("langCode", langCode).commit();
            }
        } else {
            getSharedPreferences("BOT_CONFIG", MODE_PRIVATE).edit().putString("langCode", langCode).commit();
        }

        Toast.makeText(this, langCode, Toast.LENGTH_SHORT).show();

        rippleBackground = findViewById(R.id.ripple_effect);
        sendButton = findViewById(R.id.send_btn);
        query = findViewById(R.id.edit_query);

        uuid = UUID.randomUUID().toString();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            InputStream stream = getResources().openRawResource(R.raw.agent_credentials);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings settings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

            client = SessionsClient.create(settings);
            sessionName = SessionName.of(projectId, uuid);

        } catch (Exception e) {
            e.printStackTrace();
        }

        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    isRecording = false;
//                    Log.e("onTextChanged", String.valueOf(isRecording));
                    sendButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.send));
                } else {
                    isRecording = true;
//                    Log.e("onTextChanged", String.valueOf(isRecording));
                    sendButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.microphone));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Dexter.withActivity(MainActivity .this).withPermissions( Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked (MultiplePermissionsReport report){
                if (report.areAllPermissionsGranted()) {

                }
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Toast.makeText(MainActivity.this, "Permission Denied !", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                }
            }
            @Override
            public void onPermissionRationaleShouldBeShown(List < PermissionRequest > permissions, PermissionToken token){
            }
        }).onSameThread().check();

        recyclerView = findViewById(R.id.chat_recycler_view);
        adapterChat = new AdapterChat(this, modelMessageArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterChat);

        setListenerToRootView();

        setupRecorder();

        sendButton.setOnTouchListener(touchListener);

        setListenerToRootView();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //--------------
        database = this.openOrCreateDatabase("TeleData",0, null);



        //--------------
        if(!checkForTableExists(database))
            createDatabase();

    }

    public void buttonClicked() {
        String msg = query.getText().toString();

//        rippleBackground.startRippleAnimation();

        msg = msg.trim();

        if(msg.isEmpty()){

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
            String s = response.getQueryResult().getAction();
            Log.d("Bot Reply", s);
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
                if(isRecording) {
                    Toast.makeText(MainActivity.this, "Released !", Toast.LENGTH_SHORT).show();
                    try {
                        recorder.stopRecording();
                        setupRecorder();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                Toast.makeText(MainActivity.this, "Pressed !", Toast.LENGTH_SHORT).show();
                Log.e("onTextChanged", String.valueOf(isRecording));
                if(isRecording) {
                    Log.e("Error Check", "Recording Started");
                    setupRecorder();
                    recorder.startRecording();
                }
                else {
                    Log.e("Error Check", "Button Clicked");
                    buttonClicked();
                }
                query.setText("");
                return true;
            }
            return false;
        }
    };

    static class RequestTask extends AsyncTask<Void, Void, DetectIntentResponse> {

        @SuppressLint("StaticFieldLeak")
        Activity activity;
        SessionName sessionName;
        SessionsClient client;
        QueryInput input;

        RequestTask(Activity activity, SessionName sessionName, SessionsClient client, QueryInput input) {
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
                        AudioFormat.CHANNEL_IN_MONO, 22050
                )
        );
    }

    @NonNull
    private File file() {
        return new File(Environment.getExternalStorageDirectory(), "audioFile.wav");
    }

    public void createDatabase() {
        database.execSQL("CREATE TABLE IF NOT EXISTS SMS(id INTEGER(2) PRIMARY KEY, validity VARCHAR, cost INTEGER(4), no_of_sms INTEGER(3))");
        database.execSQL("INSERT INTO SMS VALUES(1, '10 Days', 12, 120)");
        database.execSQL("INSERT INTO SMS VALUES(2, '28 Days', 26, 250)");
        database.execSQL("INSERT INTO SMS VALUES(3, '10 Days', 36, 350)");

        database.execSQL("CREATE TABLE IF NOT EXISTS Talktime(id INTEGER(2) PRIMARY KEY, validity VARCHAR, cost INTEGER(4), talktime FLOAT)");
        database.execSQL("INSERT INTO Talktime VALUES(1, 'Unrestricted', 10, 7.47)");
        database.execSQL("INSERT INTO Talktime VALUES(2, 'Unrestricted', 20, 14.95)");
        database.execSQL("INSERT INTO Talktime VALUES(3, 'Unrestricted', 30, 22.42)");
        database.execSQL("INSERT INTO Talktime VALUES(4, 'Unrestricted', 50, 39.37)");
        database.execSQL("INSERT INTO Talktime VALUES(5, 'Unrestricted', 100, 81.75)");
        database.execSQL("INSERT INTO Talktime VALUES(6, 'Unrestricted', 1000, 847.46)");
        database.execSQL("INSERT INTO Talktime VALUES(7, 'Unrestricted', 500, 423.75)");
        database.execSQL("INSERT INTO Talktime VALUES(8, 'Unrestricted', 5000, 4237.29)");

        database.execSQL("CREATE TABLE IF NOT EXISTS Netpack(id INTEGER(2) PRIMARY KEY, validity VARCHAR, cost INTEGER(4), data VARCHAR)");
        database.execSQL("INSERT INTO Netpack VALUES(1, '28 Days', 98, '6GB')");
        database.execSQL("INSERT INTO Netpack VALUES(2, '28 Days', 48, '3GB')");
        database.execSQL("INSERT INTO Netpack VALUES(3, '1 Day', 16, '1GB')");

        database.execSQL("CREATE TABLE IF NOT EXISTS Unlimited(id INTEGER(2) PRIMARY KEY, validity VARCHAR, cost INTEGER(4), data VARCHAR)");
        database.execSQL("INSERT INTO Unlimited VALUES(1, '28 Days', 249, '1.5GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(2, '56 Days', 399, '1.5GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(3, '84 Days', 599, '1.5GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(4, '56 Days', 449, '2GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(5, '28 Days', 219, '1GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(6, '84 Days', 699, '2GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(7, '28 Days', 299, '2GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(8, '84 Days', 379, '6GB')");
        database.execSQL("INSERT INTO Unlimited VALUES(9, '365 Days', 1499, '24GB/day')");
        database.execSQL("INSERT INTO Unlimited VALUES(10, '365 Days', 2399, '1.5GB/day')");

        Cursor c = database.rawQuery("SELECT * FROM SMS", null);
        c.moveToFirst();
        while(c.moveToNext()) {
            //Log.d("message",Integer.toString(c.getColumnIndex("cost")));
            Log.e("Cost", c.getString(c.getColumnIndex("cost")));
            Log.e("Validity", c.getString(c.getColumnIndex("validity")));
            Log.e("NOS", c.getString(c.getColumnIndex("no_of_sms")));
            c.moveToNext();
        }
        c.close();
    }


    private boolean checkForTableExists(SQLiteDatabase db){
        String sql = "SELECT  name FROM sqlite_master WHERE type='table' AND name='SMS'";
        Cursor mCursor = db.rawQuery(sql, null);
        if (mCursor.getCount() > 0) {
            return true;
        }
        mCursor.close();
        return false;
    }
}

