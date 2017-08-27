package com.oauth.georgew.pinchtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PinchEdits extends AppCompatActivity {

    Button edit_button, back_to_update_button;
    String pinch_id, pinch_test_date, original_bicep, original_tricep, original_subscap, original_suprailiac, responseStr, bicep_input, tricep_input, subscap_input, suprailiac_input, user_id;
    OkHttpClient client;
    EditText bicep, tricep, subscap, suprailiac;
    TextView date;
    private static final String TAG = PinchEdits.class.getSimpleName();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    //set up overflow menu
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //set up options menu properly
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
        setContentView(R.layout.activity_pinch_edits);
        //set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        user_id = getIntent().getStringExtra("user_id");

        back_to_update_button = (Button) findViewById(R.id.pinch_edit_back_button);
        edit_button = (Button) findViewById(R.id.update_body_fat_button);

        //get pinch id
        pinch_id = getIntent().getStringExtra("pinch_id");
        //set up all edit texts
        setUpEditTexts();

        //get pinches and place them in selectable, scrollable list
        getPinches();
        //Log.d(TAG, pinch_id);

        //turn off edit button
        edit_button.setEnabled(false);

        //make it so edit button is not active until all items are filled out
        bicep.addTextChangedListener(textWatcher);
        tricep.addTextChangedListener(textWatcher);
        subscap.addTextChangedListener(textWatcher);
        suprailiac.addTextChangedListener(textWatcher);

        //go back to menu or main screen
        back_to_update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent menu = new Intent(getApplicationContext(), Menu.class);
                 menu.putExtra("user_id", user_id);
                 startActivity(menu);
            }
        });

        //get information from edit text fields then add to json to make put request to pinch field then update and move to menu
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bicep_input = bicep.getText().toString();
                tricep_input = tricep.getText().toString();
                subscap_input = subscap.getText().toString();
                suprailiac_input = suprailiac.getText().toString();
                updatePinchInDatastore("https://bodyfatpinchtest.appspot.com/pinchtest/" + pinch_id,
                        "{'user': '" + user_id + "', 'bicep': '" + bicep_input + "', 'tricep': '" + tricep_input + "', 'subscapular': '" + subscap_input + "', " +
                        "'suprailiac': '" + suprailiac_input + "'}");
                Common.makeToast("Data Updated", getApplicationContext());
               Intent menu = new Intent(getApplicationContext(), Menu.class);
               menu.putExtra("user_id", user_id);
               startActivity(menu);
            }
        });
    }

    //watch edit text fields to ensure they are properly filled out
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if(bicep.getText().toString().length() == 0 || tricep.getText().toString().length() == 0 || subscap.getText().toString().length() == 0 || suprailiac.getText().toString().length() == 0)
            {
                edit_button.setEnabled(false);
            } else {
                edit_button.setEnabled(true);
            }
        }
    };

    //get pinches to fill out edittext fields
    private void getPinches(){
        client = new OkHttpClient();
        String url = "https://bodyfatpinchtest.appspot.com/pinchtest/" + pinch_id;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "FAILURE REQUEST 1");
                e.printStackTrace();
            }
            //set up edit text fields
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d(TAG, resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    pinch_test_date = jsonObject.getString("pinch_test_date");
                    original_bicep = jsonObject.getString("bicep_pinch");
                    original_tricep = jsonObject.getString("tricep_pinch");
                    original_subscap = jsonObject.getString("subscapular_pinch");
                    original_suprailiac = jsonObject.getString("suprailiac_pinch");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            date.setText(pinch_test_date);
                            bicep.setHint(original_bicep);
                            tricep.setHint(original_tricep);
                            subscap.setHint(original_subscap);
                            suprailiac.setHint(original_suprailiac);
                        }
                    });
                } catch (JSONException je){
                    je.printStackTrace();
                }
            }
        });
    }

    //make put request to update pinches for user
    public void updatePinchInDatastore(String url, String json){

        client = new OkHttpClient();
        //build url
        //Log.d(TAG, json);
        //Log.d(TAG, url);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "FAILURE REQUEST");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //set up test
                if (response.isSuccessful()) {
                    responseStr = response.body().string();
                    Log.d(TAG, responseStr);
                } else {
                    Log.d(TAG, "BLEW IT " + response.toString());
                }
            }
        });
    }


    //set up edit text fields
    private void setUpEditTexts(){
        date = (TextView)findViewById(R.id.date);
        bicep = (EditText)findViewById(R.id.bicep_edit);
        tricep = (EditText)findViewById(R.id.tricep_edit);
        subscap = (EditText)findViewById(R.id.subscap_edit);
        suprailiac = (EditText)findViewById(R.id.suprailiac_edit);
    }
}
