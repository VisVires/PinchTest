package com.oauth.georgew.pinchtest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Menu extends AppCompatActivity {

    private static final String TAG = Menu.class.getSimpleName();
    Button bodyFatButton, circumButton, updateBodyFat;
    private OkHttpClient client;
    TextView greeting, test, age_output, height_output, weight_output, body_fat_output, bmi_output;
    String gender, user_id, first_name, last_name, age, height;
    int height_in_inches;
    boolean bodyfat_exists;
    Double weight, bodyFat, bodyDensity;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    //create a menu for overflow
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //set onclick items for overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_user: {
                Common.goToUpdateUserInfo(user_id, this);
                return true;
            }
            case R.id.action_sign_out: {
                Common.logOut(this);
                this.finishAffinity();
                return true;
            }
            case R.id.delete_profile: {
                Common.deleteUser(user_id);
                Common.makeToast("Deleted User", this);
                Intent home = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(home);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //set up toolbar menu
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        greeting = (TextView) findViewById(R.id.greeting);
        user_id = getIntent().getStringExtra("user_id");

        //set up circumference button to go to measurements activity
        circumButton = (Button) findViewById(R.id.circum_button);
        circumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getCircum = new Intent(getApplicationContext(), Circumference.class);
                getCircum.putExtra("user_id", user_id);
                startActivity(getCircum);
            }
        });

        //set up body fat button to go to add body fat activity
        bodyFatButton = (Button) findViewById(R.id.calc_button);
        bodyFatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getBodyFat = new Intent(getApplicationContext(), Pinches.class);
                getBodyFat.putExtra("user_id", user_id);
                startActivity(getBodyFat);
            }
        });

        //set up update body fat button only available if bodyfat exists
        updateBodyFat = (Button) findViewById(R.id.mod_button);
        if (bodyfat_exists){
            updateBodyFat.setEnabled(true);
        }

        //set up click listener to go to update pinches page
        updateBodyFat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent update_pinches = new Intent(getApplicationContext(), UpdatePinches.class);
                update_pinches.putExtra("user_id", user_id);
                startActivity(update_pinches);
            }
        });
    }


    //make get request
    @Override
    protected void onStart(){
        makeGetRequest("https://bodyfatpinchtest.appspot.com/user/" + user_id);
        super.onStart();
    }

    //make original get request to user
    public void makeGetRequest(String url) {
        client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "FAILURE REQUEST");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resp = response.body().string();
                    //set up test

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                //prep textviews for new values
                                setUpTextViews();

                                JSONObject jsonObject = new JSONObject(resp);
                                //set greeting
                                first_name = jsonObject.getString("first_name");
                                last_name = jsonObject.getString("last_name");
                                greeting.setText("Welcome Back " + first_name + " " + last_name + "!");

                                //set age
                                age = jsonObject.getString("age");
                                if (age != null) {
                                    age_output.setText(age);
                                }

                                //set height
                                height = jsonObject.getString("height");
                                if(height != null) {
                                    height_in_inches = Integer.parseInt(height);
                                    height_output.setText(Common.heightOutput(height_in_inches));
                                }

                                //set up body fat
                                JSONArray pinches = jsonObject.getJSONArray("pinches");
                                if(pinches.length() > 0) {
                                    bodyfat_exists = true;
                                    bodyFat = pinches.getJSONObject(pinches.length()-1).getDouble("body_fat_measure");
                                    bodyDensity = pinches.getJSONObject(pinches.length()-1).getDouble("body_density_measure");
                                    bodyFat = Common.round(bodyFat,2);
                                    body_fat_output.setText(bodyFat.toString() + "%");
                                    bodyDensity = Common.round(bodyDensity,2);
                                } else {
                                    updateBodyFat.setEnabled(false);
                                }

                                //set up weight
                                JSONArray weightArray = jsonObject.getJSONArray("weight");
                                if (weightArray.length() > 0) {
                                    weight = weightArray.getDouble(weightArray.length()-1);
                                    weight_output.setText(weight.toString() + " lbs");
                                    Double bmi = Common.calcBMI(height_in_inches, weight);
                                    bmi_output.setText(bmi.toString());
                                }

                                Log.d(TAG, resp);
                                //Log.d(TAG, bodyFat.toString());
                                //Log.d(TAG, bodyDensity.toString());
                            } catch (JSONException je){
                                je.printStackTrace();
                            }
                        }
                    });
            }
        });
    }

    //set up all text views
    private void setUpTextViews(){
        age_output = (TextView) findViewById(R.id.age);
        height_output = (TextView) findViewById(R.id.height);
        weight_output = (TextView) findViewById(R.id.weight);
        body_fat_output = (TextView) findViewById(R.id.body_fat);
        bmi_output = (TextView) findViewById(R.id.bmi);
    }
}
