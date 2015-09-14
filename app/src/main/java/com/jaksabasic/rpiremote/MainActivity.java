package com.jaksabasic.rpiremote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import co.teubi.raspberrypi.io.GPIO;
import co.teubi.raspberrypi.io.GPIOStatus;
import co.teubi.raspberrypi.io.PORTFUNCTION;
import co.teubi.raspberrypi.io.PORTVALUE;

public class MainActivity extends ActionBarActivity implements GPIO.PortUpdateListener,GPIO.ConnectionEventListener {


    TextView isconnected;
    CheckBox cb;
    ToggleButton tb;
    ToggleButton light1;
    ToggleButton light2;
    TextView temp0;
    TextView temp1;


    protected DefaultHttpClient httpClient;
    public GPIO gpioPort;
    String shareHost,sharePort,shareUser,sharePass;
    SharedPreferences sharedPref;

    protected void Initialize() {
        isconnected = (TextView)findViewById(R.id.isconnected);
        cb = (CheckBox)findViewById(R.id.chkIsInput);
        tb = (ToggleButton)findViewById(R.id.btnPort);
        light1 = (ToggleButton)findViewById(R.id.light1);
        light2 = (ToggleButton)findViewById(R.id.light2);
        temp0 = (TextView)findViewById(R.id.temp0);
        temp1 = (TextView)findViewById(R.id.temp1);

        isconnected.setText("connecting...");

        shareHost = sharedPref.getString(getResources().getString(R.string.sharedPreferencesHost), "");
        sharePort = sharedPref.getString(getResources().getString(R.string.sharedPreferencesPort), "8000");
        shareUser = sharedPref.getString(getResources().getString(R.string.sharedPreferencesUser), "");
        sharePass = sharedPref.getString(getResources().getString(R.string.sharedPreferencesPass), "");
        Log.d("Share Host", shareHost);
        Log.d("Share Port",sharePort);
        Log.d("Share User",shareUser);
        Log.d("Share Pass",sharePass);

        gpioPort = new GPIO(
                new GPIO.ConnectionInfo(
                        shareHost,
                        8000,
                        shareUser,
                        sharePass
                )
        );

        // To start monitoring the status of the port
        this.gpioPort.addPortUpdateListener(this);
        (new Thread(this.gpioPort)).start();

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 5000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        httpClient = new DefaultHttpClient(httpParameters);

        temp0.setText(String.format("temp0: %f", getCelciusValue("temp0")));
        temp1.setText(String.format("temp1: %f", getCelciusValue("temp1")));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isconnected.setText(gpioPort.isConnected() ? "Connected" : "Not connected");
        isconnected.setBackgroundColor(gpioPort.isConnected() ? getResources().getColor(R.color.darkgreen) : getResources().getColor(R.color.darkred));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPreferencesWebiopi), Context.MODE_PRIVATE);

        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                        Initialize();
                    }
                };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);

        Initialize();

        cb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(cb.isChecked()) {
                    gpioPort.setFunction(3, PORTFUNCTION.OUTPUT);
                } else {
                    gpioPort.setFunction(3, PORTFUNCTION.INPUT);
                }

            }
        });

        tb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Only change port value if the port is an "output"
                if(!cb.isChecked()) {
                    if(!tb.isChecked()) {
                        gpioPort.setValue(3, 0);
                    } else {
                        gpioPort.setValue(3, 1);
                    }
                }
            }
        });

        light1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Only change port value if the port is an "output"
                if (!light1.isChecked()) {
                    gpioPort.setValue(17, 0);
                } else {
                    gpioPort.setValue(17, 1);
                }
            }
        });


        light2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Only change port value if the port is an "output"
                if (!light2.isChecked()) {
                    gpioPort.setValue(18, 0);
                } else {
                    gpioPort.setValue(18, 1);
                }
            }
        });
    }

    @Override
    public void onPortUpdated(final GPIOStatus stat) {
        runOnUiThread(new Runnable() {
            public void run() {
                // First check if the port is configured
                // as an input or output
                if (stat.ports.get(3).function == PORTFUNCTION.INPUT) {

                    // Check the checkbox
                    ((CheckBox) findViewById(R.id.chkIsInput)).setChecked(true);


                    // If is an Input disable the button
                    ((ToggleButton) findViewById(R.id.btnPort)).setEnabled(false);

                    // Set the checked state based on the current port value
                    ((ToggleButton) findViewById(R.id.btnPort)).setChecked(stat.ports.get(3).value.toBool());
                } else if (stat.ports.get(3).function == PORTFUNCTION.OUTPUT) {

                    // Un-check the checkbox
                    ((CheckBox) findViewById(R.id.chkIsInput)).setChecked(false);


                    // If is an Output enable the button
                    ((ToggleButton) findViewById(R.id.btnPort)).setEnabled(true);

                    // Set the checked state based on the current port value
                    ((ToggleButton) findViewById(R.id.btnPort)).setChecked(stat.ports.get(3).value.toBool());

                } else {

                }

                boolean led17 = stat.ports.get(17).value.toBool();
                if (light1.isChecked() != led17) {
                    light1.setChecked(led17);
                }

                boolean led18 = stat.ports.get(18).value.toBool();
                if (light2.isChecked() != led18) {
                    light2.setChecked(led18);
                }
            }
        });
    }
    @Override
    public void onConnectionFailed(String message) {
        // TODO Auto-generated method stub
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public Double getCelciusValue(String device) {
        Double temp = Double.MIN_VALUE;
        final String port = sharePort.length() > 0 ? sharePort : "8000";
        final String restUrl = String.format("http://%s:%s/devices/%s/sensor/temperature/c", shareHost, port, device);

        try {
            temp = new AsyncTask<Void, Void, Double>() {
                @Override
                protected Double doInBackground(Void... params) {
                    Double temperature = Double.MIN_VALUE;

                    HttpGet httpGet = new HttpGet(restUrl);
                    try {
                        Credentials defaultcreds = new UsernamePasswordCredentials(shareUser, sharePass);
                        httpClient.getCredentialsProvider().setCredentials(new AuthScope(shareHost, Integer.parseInt(port), AuthScope.ANY_REALM), defaultcreds);

                        HttpResponse response = httpClient.execute(httpGet);
                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }

                        String resString = sb.toString();

                        try {
                            temperature = Double.parseDouble(resString);
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        is.close(); // Close the stream
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return temperature;
                }
            }.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return temp;
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

        if (id == R.id.item_refresh) {
            Initialize();
        }

        return super.onOptionsItemSelected(item);
    }
}
