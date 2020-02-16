package com.vil.vil_bot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Result;
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
import com.google.gson.JsonElement;
import com.google.protobuf.Field;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.skyfishjy.library.RippleBackground;
import com.vil.vil_bot.adapters.AdapterChat;
import com.vil.vil_bot.models.ModelMessage;
import com.vil.vil_bot.services.VoiceListenerService;
import com.vil.vil_bot.services.VoiceRecogService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {


    protected TextReciever myReceiver;


    public static String langCode = "en-IN";
    public static String host = "http://10.10.40.36:5000";
    static File audioFile = null;

    SessionsClient client;
    SessionName sessionName;

    TextToSpeech textToSpeech;

    EditText query;
    String uuid;

    RecyclerView recyclerView;
    ArrayList<ModelMessage> modelMessageArrayList = new ArrayList<>();
    AdapterChat adapterChat;

    RippleBackground rippleBackground;
    FloatingActionButton sendButton;
    SQLiteDatabase database;
    String responseIntent;
    String assisantStatus = null;
    String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";

    boolean isRecording = true;

    Recorder recorder;
    final int UPI_PAYMENT = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menulist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent;
        switch (item.getItemId()) {
            case R.id.changeLanguage:
                intent = new Intent(this, ChangeLanguage.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.changePreferences:
                intent = new Intent(this, ChangeVoiceAssistant.class);
                startActivity(intent);
                finish();
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
        langCode = i.getStringExtra("LANGUAGE_CODE");
        String queryString = i.getStringExtra("QUERY");
        if(i.hasExtra("ASSISTANT_STATUS")){
            assisantStatus = i.getStringExtra("ASSISTANT_STATUS");
        } else {
            assisantStatus = getSharedPreferences("ASSISTANT_STATUS", MODE_PRIVATE).getString("status", "disabled");
        }

        responseIntent = "";

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

            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR){
                        textToSpeech.setLanguage(Locale.forLanguageTag(langCode));
                    }
                }
            });

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
        adapterChat = new AdapterChat(this, modelMessageArrayList, recyclerView);
        recyclerView.setHasFixedSize(true);
        //above line was added
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterChat);

        setListenerToRootView();

        //setupRecorder();

        sendButton.setOnTouchListener(touchListener);

        setListenerToRootView();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //--------------
        database = this.openOrCreateDatabase("TeleData",0, null);

        //--------------
        if(!checkForTableExists(database))
            createDatabase();

        if(isMyServiceRunning(VoiceRecogService.class)){
            stopService(new Intent(MainActivity.this, VoiceRecogService.class));
            Toast.makeText(this, "Service Stopped !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service is not running !", Toast.LENGTH_SHORT).show();
        }

        if(queryString != null && queryString.length() > 0){
            adapterChat.addItem(new ModelMessage(queryString, "user", "user", null));
            QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(queryString).setLanguageCode("en")).build();
            new RequestTask(MainActivity.this, sessionName, client, input, queryString, 0).execute();
        }

        startService(new Intent(this, VoiceListenerService.class));
    }

    @Override
    protected void onStop() {

        stopService(new Intent(this, VoiceListenerService.class));

        if(assisantStatus != null && assisantStatus.equals("enabled")){
            startService(new Intent(MainActivity.this, VoiceRecogService.class));
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        } else if (assisantStatus != null){
            Toast.makeText(this, "Service Disabled", Toast.LENGTH_SHORT).show();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //stopService(new Intent(MainActivity.this, VoiceRecogService.class));
        super.onDestroy();
    }

    public void buttonClicked() {
        String msg = query.getText().toString().trim();

//        rippleBackground.startRippleAnimation();
        if(!msg.isEmpty()){
            adapterChat.addItem(new ModelMessage(msg, "user", "user", null));

            if(modelMessageArrayList.get(modelMessageArrayList.size()-1).getText().equals("Yes")) {
                int GOOGLE_PAY_REQUEST_CODE = 123;

                Uri uri = new Uri.Builder()
                                .scheme("upi")
                                .authority("pay")
                                .appendQueryParameter("pa", "adityagunjal1365@okicici")
                                .appendQueryParameter("pn", "Aditya Gunjal")
                                //.appendQueryParameter("mc", "your-merchant-code")
                                // .appendQueryParameter("tr", "your-transaction-ref-id")
                                //.appendQueryParameter("tn", "your-transaction-note")
                                .appendQueryParameter("am", "1")
                                .appendQueryParameter("cu", "INR")
                                //.appendQueryParameter("url", "your-transaction-url")
                                .build();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
                this.startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE);
            }
            else {
                QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(msg).setLanguageCode("en")).build();
                new RequestTask(MainActivity.this, sessionName, client, input, msg, 0).execute();
            }
            recyclerView.scrollToPosition(adapterChat.getItemCount() - 1);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123){
            String text = modelMessageArrayList.get(modelMessageArrayList.size() - 2).getText();
            String price = text.substring(text.lastIndexOf(" ") + 2).replaceAll("\\?", "");


            if (data.getStringExtra("response").contains("Status=FAILURE")){
                switch (responseIntent) {
                    case "recharge.phone.upgrade":
                        text = "rechargeunlimitedconfirmationno ₹" + price;
                        break;
                    case "sms-plan-upgrade":
                        text = "rechargesmsconfirmationno ₹" + price;
                        break;
                    case "talktime-plan-upgrade":
                        text = "rechargetalktimeconfirmationno ₹" + price;
                        break;
                    case "data.plan.upgrade":
                        text = "rechargedataconfirmationno ₹" + price;
                        break;
                }
            }else{
                switch (responseIntent) {
                    case "recharge.phone.upgrade":
                        text = "rechargeunlimitedconfirmationyes ₹" + price;
                        break;
                    case "sms-plan-upgrade":
                        text = "rechargesmsconfirmationyes ₹" + price;
                        break;
                    case "talktime-plan-upgrade":
                        text = "rechargetalktimeconfirmationyes ₹" + price;
                        break;
                    case "data.plan.upgrade":
                        text = "rechargedataconfirmationyes " + price;
                        break;
                }

                QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(text).setLanguageCode("en")).build();
                new RequestTask(MainActivity.this, sessionName, client, input, text, 0).execute();
            }
        }


    }

    public void callback(DetectIntentResponse response, String text){
        if(response != null) {
            if(text == null)
                text = response.getQueryResult().getFulfillmentText();

            responseIntent = response.getQueryResult().getIntent().getDisplayName();
            Log.e("Intent", responseIntent);
            if(textToSpeech != null){
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }

            if(responseIntent.equals("recharge.phone.upgrade") || responseIntent.equals("sms-plan-upgrade") ||
            responseIntent.equals("talktime-plan-upgrade") || responseIntent.equals("data.plan.upgrade")) {
                String value = String.valueOf(response.getQueryResult().getParameters());
                Log.e("UI_CHECK", value);
                int index = value.indexOf("1");
                value = value.substring(index+1);
                String[] str = value.split("\n");
                str[0] = str[0].substring(0, str[0].length() - 1).trim();

                if (!str[0].equals("fields"))
                    adapterChat.addItem(new ModelMessage(text, responseIntent, "bot", str[0]));
                else
                    adapterChat.addItem(new ModelMessage(text, responseIntent, "bot", null));
            }
            else
                adapterChat.addItem(new ModelMessage(text, responseIntent, "bot", null));
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
                        //recorder.stopRecording();
                        new SpeechToTextTask(MainActivity.this,sessionName, client).execute();
                        //setupRecorder();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                Toast.makeText(MainActivity.this, "Pressed !", Toast.LENGTH_SHORT).show();
                if(isRecording) {
//                    Log.e("Error Check", "Recording Started");
                    //setupRecorder();
                    //recorder.startRecording();
                }
                else {
//                    Log.e("Error Check", "Button Clicked");
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
        String text;
        int requestType;

        RequestTask(Activity activity, SessionName sessionName, SessionsClient client, QueryInput input, String text, int requestType) {
            this.activity = activity;
            this.sessionName = sessionName;
            this.client = client;
            this.input = input;
            this.requestType = requestType;
            this.text = text;
        }

        @Override
        protected DetectIntentResponse doInBackground(Void... voids) {

            String langCode = ((MainActivity)activity).langCode;
            if(!langCode.equals("en-IN")){
                String result = translate(text, langCode, "en-IN");
                Log.e("Result", result);
                input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(result).setLanguageCode("en")).build();
                Log.e("Lang_Check", result);
            }

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

            String langCode = ((MainActivity)activity).langCode;
            if(!langCode.equals("en-IN")){
                TranslateTask task = new TranslateTask(activity, sessionName, client, input, detectIntentResponse.getQueryResult().getFulfillmentText(), requestType);
                task.setResponse(detectIntentResponse);
                task.execute();
            } else {
                ((MainActivity)activity).callback(detectIntentResponse, null);
            }
            Log.e("Lang_Check", "Translated Again");
        }


        public String translate(String text, String srcLang, String targetLang){

            String result = "";

            try {
                URL url = new URL(MainActivity.host + "/translate");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("content-type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json; utf-8");
                connection.setDoOutput(true);
                JSONObject body = new JSONObject();
                body.put("text", text);
                body.put("source_lang", srcLang);
                body.put("target_lang", targetLang);

                OutputStream os = connection.getOutputStream();
                byte[] data = body.toString().getBytes();
                os.write(data, 0, data.length);

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), "utf-8"));

                StringBuilder resp = new StringBuilder();
                String responseLine = null;

                while((responseLine = br.readLine()) != null){
                    resp.append(responseLine.trim());
                }

                JSONObject response = new JSONObject(resp.toString());
                //JSONObject text = new JSONObject(response.getString("response").substring(5));

                result = response.getString("response");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
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

//    private void setupRecorder() {
//        recorder = OmRecorder.wav(
//                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
//                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
//                        //animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
//                    }
//                }), file());
//
//
//    }

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
        audioFile = new File(getExternalFilesDir("temp"), "audioFile.wav");
        return audioFile;
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
        do {
            //Log.d("message",Integer.toString(c.getColumnIndex("cost")));
            Log.e("Cost", c.getString(c.getColumnIndex("cost")));
            Log.e("Validity", c.getString(c.getColumnIndex("validity")));
            Log.e("NOS", c.getString(c.getColumnIndex("no_of_sms")));
        }while(c.moveToNext());
        c.close();
    }

    static class TranslateTask extends AsyncTask<Void, Void, String> {

        Activity activity;
        SessionName sessionName;
        SessionsClient client;
        QueryInput input;
        String text;
        int requestType;
        DetectIntentResponse response;

        public TranslateTask(Activity activity, SessionName sessionName, SessionsClient client, QueryInput input, String text, int requestType) {
            this.activity = activity;
            this.sessionName = sessionName;
            this.client = client;
            this.input = input;
            this.text = text;
            this.requestType = requestType;
        }

        public void setResponse(DetectIntentResponse response) {
            this.response = response;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String langCode = ((MainActivity) activity).langCode;
            if(text != null && text.length() > 0){
                String result = new RequestTask(activity, sessionName, client, input, text, requestType).translate(text, "en-IN", langCode);
                return result;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null)
                ((MainActivity)activity).callback(response, s);
        }
    }
    static class SpeechToTextTask extends AsyncTask<Void, Void, JSONObject>{

        Activity activity;
        SessionName sessionName;
        SessionsClient client;

        public SpeechToTextTask(Activity activity, SessionName sessionName, SessionsClient client) {
            this.activity = activity;
            this.sessionName = sessionName;
            this.client = client;

        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String result = "";
            try {
                String url = MainActivity.host + "/speech-to-text?lang=" + ((MainActivity)activity).langCode;
                File file = MainActivity.audioFile;
                try {
                    HttpClient httpclient = new DefaultHttpClient();

                    HttpPost httppost = new HttpPost(url);

                    InputStreamEntity reqEntity = new InputStreamEntity(
                            new FileInputStream(file), -1);
                    reqEntity.setContentType("binary/octet-stream");
                    reqEntity.setChunked(true); // Send in multiple parts if needed
                    httppost.setEntity(reqEntity);
                    HttpResponse response = httpclient.execute(httppost);

                    return new JSONObject(EntityUtils.toString(response.getEntity()));
                } catch (Exception e) {
                    // show error
                    e.printStackTrace();
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject resp) {

            try {
//                Toast.makeText(activity, resp.getString("english"), Toast.LENGTH_SHORT).show();
//                Log.e("STOP", resp.getString("english"));
//                ((MainActivity) activity).adapterChat.addItem(new ModelMessage(resp.getString("source_lang"), "user", "user"));
//                QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(resp.getString("source_lang")).setLanguageCode("en")).build();
//                new RequestTask(activity, sessionName, client, input, resp.getString("source_lang"),0).execute();
                ((MainActivity)activity).query.setText(resp.getString("source_lang"));

            } catch (Exception e) {
                if(e instanceof NullPointerException){
                    ((MainActivity) activity).adapterChat.addItem(new ModelMessage("Couldn't hear you, try again !", "bot", "bot", null));
                }
                e.printStackTrace();
            }
        }
    }


    private boolean checkForTableExists(SQLiteDatabase db){
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='SMS'";
        Cursor mCursor = db.rawQuery(sql, null);
        if (mCursor.getCount() > 0) {
            return true;
        }
        mCursor.close();
        return false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public class TextReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String text = b.getString("text");
            adapterChat.addItem(new ModelMessage(text, "user", "user", null));
            QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(text).setLanguageCode("en")).build();
            new RequestTask(MainActivity.this, sessionName, client, input, text, 0).execute();
        }
    }

    @Override
    public void onResume(){
        myReceiver = new TextReciever();
        final IntentFilter intentFilter = new IntentFilter("YourAction");
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);
        super.onResume();
    }

    @Override
    public void onPause(){
        if(myReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        myReceiver = null;
        super.onPause();
    }

}

