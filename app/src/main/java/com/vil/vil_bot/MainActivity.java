package com.vil.vil_bot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


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
    FloatingActionButton speakButton;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menulist,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.changeLanguage:
                Intent intent = new Intent(this,ChangeLanguage.class);
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

        rippleBackground = findViewById(R.id.ripple_effect);
        speakButton = findViewById(R.id.speak_button);
        query = findViewById(R.id.edit_query);

        uuid = UUID.randomUUID().toString();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()!=0){
                    //speakButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.bot_image));
                    speakButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sendbutton));
//                    speakButton.setVisibility(View.INVISIBLE);
//                    sendButton.setVisibility(View.VISIBLE);
                }else{

                    speakButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.microphone));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
    }

    public void buttonClicked(View view) {
        String msg = query.getText().toString();

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