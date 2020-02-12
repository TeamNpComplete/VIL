package com.vil.vil_bot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ChangeLanguage extends AppCompatActivity {


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        Intent intent = new Intent(this,MainActivity.class);
        String str = new String();
        switch(view.getId()) {
            case R.id.english:
                if (checked)
                    str="en";
                    intent.putExtra("langCode",str);
                    startActivity(intent);
                    finish();
                    break;
            case R.id.hindi:
                if (checked)
                    break;
            case R.id.marathi:
                if (checked)
                    break;
            case R.id.gujarati:
                if (checked)
                    break;
            case R.id.kannada:
                if (checked)
                    break;
            case R.id.punjabi:
                if (checked)
                    break;
            case R.id.malyalam:
                if (checked)
                    break;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);
    }
}
