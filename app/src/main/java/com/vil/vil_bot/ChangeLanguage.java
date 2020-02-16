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
                    str="en-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.hindi:
                if (checked)
                    str="hi-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.marathi:
                if (checked)
                    str="mr-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.gujarati:
                if (checked)
                    str="gu-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.kannada:
                if (checked)
                    str="kn-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.punjabi:
                if (checked)
                    str="pa-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.malyalam:
                if (checked)
                    str="ml-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.bengali:
                if (checked)
                    str="bn-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.tamil:
                if (checked)
                    str="tm-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;
            case R.id.telugu:
                if (checked)
                    str="te-IN";
                intent.putExtra("LANGUAGE_CODE",str);
                startActivity(intent);
                finish();
                break;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);
    }
}
