package com.vil.vil_bot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import ai.api.AIServiceContext;
import ai.api.AIServiceContextBuilder;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;


public class MainActivity extends AppCompatActivity {
    AIDataService aiDataService;
    AIServiceContext aiServiceContext;
    AIRequest aiRequest;
    TextView t;
    EditText e;
    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t = findViewById(R.id.textView);
        e = findViewById(R.id.edit_query);

        uuid = UUID.randomUUID().toString();

        final AIConfiguration config = new AIConfiguration("555542b49b8b456eb79774954ae64f1e",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(this, config);
        aiServiceContext = new AIServiceContextBuilder().buildFromSessionId(uuid);
        aiRequest = new AIRequest();
    }

    public void callback(AIResponse aiResponse){
        if(aiResponse != null){
            String botReply = aiResponse.getResult().getFulfillment().getSpeech();
            Log.d("Bot Reply", botReply);
            t.setText(botReply);
        } else {
            Log.d("debuger", "Bot Reply : Null");
            t.setText("Something ain't right here");
        }
    }

    public void buttonClicked(View view) {
        String msg = e.getText().toString();

        if(msg.trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Enter Query", Toast.LENGTH_LONG).show();
        } else {
            t.setText("");
            aiRequest.setQuery(msg);
            RequestTask requestTask = new RequestTask(MainActivity.this, aiDataService, aiServiceContext);
            requestTask.execute(aiRequest);
        }
    }


    public class RequestTask extends AsyncTask<AIRequest, Void, AIResponse>{
        Activity activity;
        AIDataService aiDataService;
        AIServiceContext aiServiceContext;

        public RequestTask(Activity activity, AIDataService aiDataService, AIServiceContext aiServiceContext) {
            this.activity = activity;
            this.aiDataService = aiDataService;
            this.aiServiceContext = aiServiceContext;
        }

        @Override
        protected AIResponse doInBackground(AIRequest... aiRequests) {
            final AIRequest request = aiRequests[0];

            try{
                return aiDataService.request(request, aiServiceContext);
            } catch (AIServiceException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(AIResponse aiResponse) {
            ((MainActivity)activity).callback(aiResponse);
        }
    }
}