package com.oauth.georgew.pinchtest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    String CLIENT_ID;
    private static final String TAG = MainActivity.class.getSimpleName();
    private AuthorizationService authorizationService;
    private AuthState authState;
    private OkHttpClient client;
    Button get_started;
    String gender, user_id, first_name, last_name, email;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        //set up authorization
        CLIENT_ID = getString(R.string.CLIENT_ID);
        SharedPreferences authPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        authorizationService = new AuthorizationService(this);

        get_started = (Button) findViewById(R.id.get_started);
        //make original get request for oauth
        get_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeGetRequestToGoogle();
            }
        });
    }

    String responseStr;

    //make post to create account
    public void postUserInfoToApi(){
        client = new OkHttpClient();
        final String url = "https://bodyfatpinchtest.appspot.com";
        final String json = "{'first_name': '" + first_name + "', 'last_name': '" + last_name + "', 'email': '" + email + "', 'user': '" + user_id + "', 'gender': '" + gender + "'}";
        //build url
        Log.d(TAG, json);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "FAILURE REQUEST 2");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //set up test
                if (response.isSuccessful()) {
                    responseStr = response.body().string();
                    //Log.d(TAG, response.toString());
                    //Log.d(TAG, responseStr);
                    if (responseStr.equals("User already exists")){
                        //Log.d(TAG, "move to menu");
                        Intent menu = new Intent(getApplicationContext(), Menu.class);
                        menu.putExtra("first_name", first_name);
                        menu.putExtra("last_name", last_name);
                        menu.putExtra("email", email);
                        menu.putExtra("user_id", user_id);
                        menu.putExtra("gender", gender);
                        startActivity(menu);
                    } else {
                        //Log.d(TAG, "move to edit user data");
                        Intent new_user = new Intent(getApplicationContext(), NewUser.class);
                        new_user.putExtra("first_name", first_name);
                        new_user.putExtra("last_name", last_name);
                        new_user.putExtra("email", email);
                        new_user.putExtra("user_id", user_id);
                        new_user.putExtra("gender", gender);
                        startActivity(new_user);
                    }

                } else {
                    Log.d(TAG, "BLEW IT " + response.toString());
                }
            }
        });
    }

    //make oauth request
    public void makeGetRequestToGoogle() {
        try {
            authState.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ae) {
                    if (ae == null) {
                        client = new OkHttpClient();
                        Log.d(TAG, accessToken);
                        HttpUrl url = HttpUrl.parse("https://www.googleapis.com/plus/v1/people/me");
                        url = url.newBuilder().addQueryParameter("key", getString(R.string.API_KEY)).build();
                        Request request = new Request.Builder()
                                .url(url)
                                .addHeader("Authorization", "Bearer " + accessToken)
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
                                try {
                                    JSONObject jsonObject = new JSONObject(resp);
                                    gender = jsonObject.getString("gender");
                                    user_id = jsonObject.getString("id");
                                    JSONArray emails = jsonObject.getJSONArray("emails");
                                    email = emails.getJSONObject(0).getString("value");
                                    JSONObject name = jsonObject.getJSONObject("name");
                                    last_name = name.getString("familyName");
                                    first_name = name.getString("givenName");
                                    //set up test
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            postUserInfoToApi();
                                        }
                                    });
                                } catch (JSONException je) {
                                    je.printStackTrace();
                                }

                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //check for auth
    @Override
    protected void onStart(){
        authState = getOrCreateAuthState();
        super.onStart();
    }

    //check and get auth state
    AuthState getOrCreateAuthState(){
        AuthState auth = null;
        SharedPreferences authorizationPreference = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authorizationPreference.getString("stateJson", null);

        //if stateJson exists set auth to it
        if(stateJson != null){
            try{
                auth = AuthState.jsonDeserialize(stateJson);
            } catch (JSONException je){
                je.printStackTrace();
                return null;
            }
        }
        //if AuthState exists already and has an access token then return else create an AuthState and get a new token
        if(auth != null && auth.getAccessToken() != null){
            //Log.d(TAG, auth.getAccessToken());
            return auth;
        } else {
            createAuthState();
            return null;
        }
    }

    //if auth state needs to be created then set it up with the profile, email and openid permissions in oauth
    void createAuthState(){
        Uri authEndPoint = new Uri.Builder().scheme("https").authority("accounts.google.com").path("o/oauth2/v2/auth").build();
        Uri tokenEndPoint = new Uri.Builder().scheme("https").authority("www.googleapis.com").path("/oauth2/v4/token").build();
        Uri redirect = new Uri.Builder().scheme("com.oauth.georgew.pinchtest").path("path").build();

        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(authEndPoint, tokenEndPoint, null);
        AuthorizationRequest req = new AuthorizationRequest.Builder(config, CLIENT_ID, ResponseTypeValues.CODE, redirect)
                .setScopes("email profile openid").build();
        //.setLoginHint("jdoe@user.example.com").build();

        Intent authComplete = new Intent(this, AuthComplete.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, req.hashCode(), authComplete, 0);
        authorizationService.performAuthorizationRequest(req, pendingIntent);
    }
}
