package com.example.ff.nwhack2015;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.james.nwhack2015.Avatar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MojioInterface extends Activity {
    private String accessToken;

    private Data data;

    private ProgressBar healthBar;
    private ProgressBar expBar;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojio_interface);

        Avatar me = new Avatar("Fred");

        healthBar = (ProgressBar) findViewById(R.id.healthbar);
        expBar = (ProgressBar) findViewById(R.id.expbar);
        healthBar.setMax(me.getMaxHealth());
        expBar.setMax(me.getMaxExp());
        healthBar.setProgress(me.getHealth());
        expBar.setProgress(me.getExp());

        mHandler = new Handler();
        data = new Data();

    }
    Runnable APIGetter = new Runnable() {
        @Override
        public void run() {
            HttpGetTask httpGetTask = new HttpGetTask(new ArrayList<NameValuePair>(0), accessToken) {
                @Override
                public void DoWithJSON(JSONObject obj) {
                    //Do whatever with your trips JSON
                    JSONArray tripArray = new JSONArray();
                    try {
                        tripArray = obj.getJSONArray("Data");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //
                    for (int i = 0; i < tripArray.length(); i++) {
                        JSONObject trip = new JSONObject();
                        try {
                            trip = tripArray.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String timeString = new String();
                        try {
                            timeString = trip.getString("Time");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Time tripTime = new Time();
                        if (!tripTime.parse3339(timeString)) {

                        }
                        if (data.getLastTimeChecked().before(tripTime)) {
                            double efficency = -1;
                            double distance = -1;
                            try {
                                efficency = trip.getDouble("FuelEfficiency");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                distance = trip.getDouble("Distance");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            data.setAverageEfficiency(efficency);
                            data.setLastEfficiency(efficency);
                            data.setBestEfficiency(efficency);
                            data.setLastTripDistance(distance);
                            data.setLongestTripDistance(distance);
                            data.increaseTotalDistance(distance);
                            data.increaseNumberOfTrips();
                            data.setLastTimeChecked(tripTime);

                        }
                    }
                }
            };
            httpGetTask.execute("https://api.moj.io/v1/Trips?limit=10&offset=0&sortBy=StartTime&desc=false&criteria=");

            mHandler.postDelayed(APIGetter,5000);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        accessToken = extras.getString("access_token");
        APIGetter.run();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mojio_interface, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayJSON(View view) {
        System.out.println(accessToken);
//        HttpGetTask httpGetTask = new HttpGetTask(new ArrayList<NameValuePair>(0), accessToken) {
//            @Override
//            public void DoWithJSON(JSONObject obj) {
//            }
//        };
//        httpGetTask.execute("https://api.moj.io/v1/Trips?limit=10&offset=0&sortBy=StartTime&desc=false&criteria=");
    }
}
