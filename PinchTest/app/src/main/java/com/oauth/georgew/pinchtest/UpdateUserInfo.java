package com.oauth.georgew.pinchtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class UpdateUserInfo extends AppCompatActivity {

    String user_id;
    String first_name, last_name, responseStr, age, height, email_input;
    boolean gender;
    JSONArray pinches, measurements, weight;
    private static final String TAG = UpdateUserInfo.class.getSimpleName();
    Button edit_info, back_button;
    EditText update_height, update_age, email;
    OkHttpClient client;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    //set up create menu
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //set up items in overflow list
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
        setContentView(R.layout.activity_update_user_info);
        //set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        user_id = getIntent().getStringExtra("user_id");
        Log.d(TAG, user_id);
        //get original user email, height and age and display them
        getUserInfo();

        edit_info = (Button) findViewById(R.id.edit_info);
        back_button = (Button) findViewById(R.id.update_user_back_button);

        //update email height and age info
        edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_input = email.getText().toString();
                age = update_age.getText().toString();
                height = update_height.getText().toString();
                updateUserInDatastore("https://bodyfatpinchtest.appspot.com/user", "{'first_name': '" + first_name + "', 'last_name': '" + last_name + "', 'email': '" + email_input + "', " +
                        "'user': '" + user_id + "', 'age': '" + age + "', 'height': '" + height + "'}");
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                main.putExtra("user_id", user_id);
                Common.makeToast("Information Updated", getApplicationContext());
                startActivity(main);
            }
        });

        //go back to previous screen
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //set up UI with custom fields for edit text
    public void getUserInfo() {
        client = new OkHttpClient();
        String url = "https://bodyfatpinchtest.appspot.com/user/" + user_id;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "FAILURE REQUEST 1");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resp = response.body().string();
                setEditTextViews();
                Log.d(TAG, resp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObject = new JSONObject(resp);
                            Log.d(TAG, jsonObject.toString());
                            email.setHint(jsonObject.getString("email"));
                            update_age.setHint(jsonObject.getString("age").toString());
                            update_height.setHint(jsonObject.getString("height"));
                            first_name = jsonObject.getString("first_name");
                            last_name = jsonObject.getString("last_name");
                            pinches = jsonObject.getJSONArray("pinches");
                            measurements = jsonObject.getJSONArray("measurements");
                            weight = jsonObject.getJSONArray("weight");
                        } catch (JSONException je){
                            je.printStackTrace();
                        }
                    }
                    });
            }
        });
    }

    //make put request to update users info
    public void updateUserInDatastore(String url, String json){

        client = new OkHttpClient();
        //build url
        Log.d(TAG, json);
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

                } else {
                    Log.d(TAG, "BLEW IT " + response.toString());
                }
            }
        });
    }

    //get edit text views set up
    private void setEditTextViews(){
        update_age = (EditText)findViewById(R.id.update_age);
        update_height = (EditText) findViewById(R.id.update_height);
        email = (EditText) findViewById(R.id.email);
    }
}
