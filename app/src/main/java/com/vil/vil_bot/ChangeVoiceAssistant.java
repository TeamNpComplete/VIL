package com.vil.vil_bot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class ChangeVoiceAssistant extends AppCompatActivity {

    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_voice_assistant);

        aSwitch = findViewById(R.id.switchOption);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
//                     the switch is checked true
                    Toast.makeText(ChangeVoiceAssistant.this, "Switched On", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
//                    the switch is checked false

                    Toast.makeText(ChangeVoiceAssistant.this, "Switched Off", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }
}
