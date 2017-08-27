package com.oauth.georgew.pinchtest;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

public class AuthComplete extends AppCompatActivity {

    private static final String TAG = AuthComplete.class.getSimpleName();
    private AuthorizationService authorizationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_complete);

        authorizationService = new AuthorizationService(this);
        Uri redirectUri = getIntent().getData();
        AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException exception = AuthorizationException.fromIntent(getIntent());

        if (response != null) {
            final AuthState authState = new AuthState(response, exception);
            authorizationService.performTokenRequest(response.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException e) {
                            authState.update(tokenResponse, e);
                            if (tokenResponse != null){
                                SharedPreferences authorizationPreferences = getSharedPreferences("auth", MODE_PRIVATE);
                                authorizationPreferences.edit().putString("stateJson", authState.jsonSerializeString()).apply();
                                finish();
                            } else {
                                Log.d(TAG, "RESPONSE FAILED");
                            }
                        }
                    });
        } else {
            Log.d(TAG, "RESPONSE FAILED");
        }

    }
}
