package com.vil.vil_bot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.skyfishjy.library.RippleBackground;
import com.vil.vil_bot.adapters.AdapterChat;
import com.vil.vil_bot.models.ModelMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    SessionsClient client;
    SessionName sessionName;

    EditText query;
    String uuid;

    RecyclerView recyclerView;
    ArrayList<ModelMessage> modelMessageArrayList = new ArrayList<>();
    AdapterChat adapterChat;

    RippleBackground rippleBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rippleBackground = findViewById(R.id.ripple_effect);
        query = findViewById(R.id.edit_query);

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

        setListenerToRootView();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        SQLiteDatabase database = this.openOrCreateDatabase("TeleData",0, null);

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
        while(c != null) {
            Log.e("Cost", c.getString(c.getColumnIndex("cost")));
            Log.e("Validity", c.getString(c.getColumnIndex("validity")));
            Log.e("NOS", c.getString(c.getColumnIndex("no_of_sms")));
            c.moveToNext();
        }
    }

    public void buttonClicked(View view) {
        String msg = query.getText().toString();
        query.setText("");

        rippleBackground.startRippleAnimation();

        if(msg.trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Enter Query", Toast.LENGTH_LONG).show();
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
}