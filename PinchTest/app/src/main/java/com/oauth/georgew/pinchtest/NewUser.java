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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewUser extends AppCompatActivity {

    String first_name, last_name, email, user_id, gender;
    String height, age;
    TextView greeting;
    EditText age_input, height_input;
    Button input_user;
    private OkHttpClient client;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    String responseStr;
    private static final String TAG = NewUser.class.getSimpleName();

/*
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

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
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        first_name = getIntent().getStringExtra("first_name");
        last_name = getIntent().getStringExtra("last_name");
        email = getIntent().getStringExtra("email");
        user_id = getIntent().getStringExtra("user_id");
        gender = getIntent().getStringExtra("gender");

        input_user = (Button)findViewById(R.id.new_user_button);
        input_user.setEnabled(false);

        age_input = (EditText)findViewById(R.id.age_input);
        age_input.addTextChangedListener(textWatcher);

        height_input = (EditText)findViewById(R.id.height_input);
        height_input.addTextChangedListener(textWatcher);

        greeting = (TextView)findViewById(R.id.new_user_greet);
        greeting.setText("Welcome " + first_name + " " + last_name + "! We need some more information before we can get started");

        input_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                age = age_input.getText().toString();
                height = height_input.getText().toString();
                putNewUserInDatastore();
            }
        });
    }


    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if(age_input.getText().toString().length() == 0 || height_input.getText().toString().length() == 0)
            {
                input_user.setEnabled(false);
            } else {
                input_user.setEnabled(true);
            }
        }
    };

    //make put request to add the age and height to the new user
    public void putNewUserInDatastore(){

        client = new OkHttpClient();
        final String url = "https://bodyfatpinchtest.appspot.com/user";
        final String json = "{'first_name': '" + first_name + "', 'last_name': '" + last_name + "', 'email': '" + email + "', 'user': '" + user_id + "', 'gender': '" +
                gender + "', 'age': '" + age + "', 'height': '" + height + "'}";
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
                    Log.d(TAG, response.toString());
                    Log.d(TAG, responseStr);
                    Intent menu = new Intent(getApplicationContext(), Menu.class);
                    menu.putExtra("user_id", user_id);
                    startActivity(menu);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Common.makeToast("User Added", getApplicationContext());
                        }
                    });
                } else {
                    Log.d(TAG, "BLEW IT " + response.toString());
                }
            }
        });
    }
}
