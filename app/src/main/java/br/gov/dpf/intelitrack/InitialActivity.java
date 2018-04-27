package br.gov.dpf.intelitrack;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;


public class InitialActivity extends AppCompatActivity
{
    public static final String PREF_KEY_FIRST_START = "com.heinrichreimersoftware.materialintro.demo.PREF_KEY_FIRST_START";
    public static final int REQUEST_CODE_INTRO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        boolean firstStart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_FIRST_START, true);

        if (firstStart)
        {
            Intent intent = new Intent(this, FirstStartActivity.class);
            startActivityForResult(intent, REQUEST_CODE_INTRO);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent(InitialActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
