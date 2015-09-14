package com.jaksabasic.rpiremote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ja on 5/12/15.
 */
public class SettingsActivity extends ActionBarActivity{

    EditText webiopi_host,webiopi_port,webiopi_user,webiopi_pass;
    Button savaSettings;

    String host,user,port,pass;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_layout);
        sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPreferencesWebiopi), Context.MODE_PRIVATE);

        webiopi_host = (EditText) findViewById(R.id.webiopi_host);
        webiopi_port = (EditText) findViewById(R.id.webiopi_port);
        webiopi_user = (EditText) findViewById(R.id.webiopi_user);
        webiopi_pass = (EditText) findViewById(R.id.webiopi_pass);

        webiopi_host.setText(sharedPref.getString(getResources().getString(R.string.sharedPreferencesHost), ""));
        webiopi_port.setText(sharedPref.getString(getResources().getString(R.string.sharedPreferencesPort), ""));
        webiopi_user.setText(sharedPref.getString(getResources().getString(R.string.sharedPreferencesUser), ""));
        webiopi_pass.setText(sharedPref.getString(getResources().getString(R.string.sharedPreferencesPass), ""));

        savaSettings = (Button) findViewById(R.id.settings_save_button);

        savaSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                host = webiopi_host.getText().toString();
                port = webiopi_port.getText().toString();
                user = webiopi_user.getText().toString();
                pass = webiopi_pass.getText().toString();


                editor = sharedPref.edit();
                editor.putString(getResources().getString(R.string.sharedPreferencesHost), host);
                editor.putString(getResources().getString(R.string.sharedPreferencesPort), port);
                editor.putString(getResources().getString(R.string.sharedPreferencesUser), user);
                editor.putString(getResources().getString(R.string.sharedPreferencesPass), pass);
                editor.apply();
                editor.commit();

                Log.d("SP Host", sharedPref.getString(getResources().getString(R.string.sharedPreferencesWebiopi), "host"));
                //startActivity(new Intent(getBaseContext(), MainActivity.class));
                finish();
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
