package com.vil.vil_bot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
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

import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    SessionsClient client;
    SessionName sessionName;

    TextView response;
    EditText query;
    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        response = findViewById(R.id.textView);
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
    }

    public void buttonClicked(View view) {
        String msg = query.getText().toString();

        if(msg.trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Enter Query", Toast.LENGTH_LONG).show();
        } else {
            response.setText("");

            RelativeLayout userMsg = findViewById(R.id.bot_reply);
//            userMsg.getLayoutParams().

            QueryInput input = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(msg).setLanguageCode("en")).build();
            new RequestTask(MainActivity.this, sessionName, client, input).execute();
        }
    }

    public void callback(DetectIntentResponse response){
        if(response != null) {
            String botReply = response.getQueryResult().getFulfillmentText();
            Log.d("Bot Reply", botReply);
            query.setText("");
            this.response.setText(botReply);
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

}