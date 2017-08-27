package com.oauth.georgew.pinchtest;

import android.app.LauncherActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdatePinches extends AppCompatActivity {

    String user_id;
    Button update_button, back_button;
    OkHttpClient client;
    ListView pinch_list;
    private static final String TAG = UpdatePinches.class.getSimpleName();

    //build menu
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //add options to overflow menu
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
        setContentView(R.layout.activity_update_pinches);
        //set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        user_id = getIntent().getStringExtra("user_id");

        back_button = (Button) findViewById(R.id.update_pinches_back_button);
        pinch_list = (ListView) findViewById(R.id.pinch_list);

        //get list of pinches
        getPinches();

        //go back to main screen
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menu = new Intent(getApplicationContext(), Menu.class);
                menu.putExtra("user_id", user_id);
                startActivity(menu);
            }
        });

        //set up on click listener for individual items in the list
        pinch_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> obj = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                String pinch = (String) obj.get("pinch_id");
                Intent editPinches = new Intent(getApplicationContext(), PinchEdits.class);
                editPinches.putExtra("pinch_id", pinch);
                editPinches.putExtra("user_id", user_id);
                startActivity(editPinches);
            }
        });

    }

    //get pinch list and populate list with measurements
    private void getPinches(){
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
                //Log.d(TAG, resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    JSONArray pinchArray = jsonObject.getJSONArray("pinches");
                    //Log.d(TAG, pinchArray.toString());
                    List<Map<String, String>> pinches = new ArrayList<Map<String, String>>();
                    //set up list items
                    for (int i = 0; i < pinchArray.length(); i++) {
                        HashMap<String, String> myMap = new HashMap<String, String>();
                        myMap.put("pinch_id", pinchArray.getJSONObject(i).getString("pinch_id"));
                        myMap.put("date", pinchArray.getJSONObject(i).getString("pinch_test_date"));
                        myMap.put("bicep", pinchArray.getJSONObject(i).getString("bicep_pinch"));
                        myMap.put("tricep", pinchArray.getJSONObject(i).getString("tricep_pinch"));
                        myMap.put("subscap", pinchArray.getJSONObject(i).getString("subscapular_pinch"));
                        myMap.put("supra", pinchArray.getJSONObject(i).getString("suprailiac_pinch"));
                        Double bfat = pinchArray.getJSONObject(i).getDouble("body_fat_measure");
                        myMap.put("bfat", Common.round(bfat,2).toString());
                        pinches.add(myMap);
                    }
                    //fill in adapter
                    final SimpleAdapter pinchAdapter = new SimpleAdapter(
                            UpdatePinches.this,
                            pinches,
                            R.layout.pinchlist,
                            new String[]{"pinch_id", "date", "bicep", "tricep", "subscap", "supra", "bfat"},
                            new int[]{R.id.pinch_id, R.id.dates, R.id.biceps, R.id.triceps, R.id.subscaps, R.id.suprailiacs, R.id.bodyfats});
                    //populate adapter
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pinch_list.setAdapter(pinchAdapter);
                        }
                    });
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        });
    }
}
