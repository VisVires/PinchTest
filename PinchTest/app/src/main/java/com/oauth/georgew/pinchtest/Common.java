package com.oauth.georgew.pinchtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by georgew on 6/8/17.
 */

public class Common {
    //log out of user account with toast
    public static void logOut(Context context){
        makeToast("Logout", context);
        clearSharedPreference(context);
    }

    //clear shared preference to get rid of auth settings to logout
    public static void clearSharedPreference(Context context) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.clear();
        editor.commit();
    }

    //make toast pop up with format String
    public static void makeToast(String input, Context context){
        Toast.makeText(context, input, Toast.LENGTH_SHORT).show();
    }

    //https://stackoverflow.com/questions/22186778/using-math-round-to-round-to-one-decimal-place
    //round function to one or several decimal places
    public static Double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    //calculate lean body mass using body fat and weight
    public static String calcLeanBodyMass(Double bodyfat, Double weight_output){
        return round((weight_output - (weight_output * (bodyfat/100))),1).toString();
    }

    //calculate fat masss using bodyfat and weight
    public static String calcFatMass(Double bodyfat, Double weight_output){
        return round((weight_output * (bodyfat/100)),1).toString();
    }

    //calculate BMI based on height and weight
    public static Double calcBMI(int height, Double weight){
        return round((weight * 703)/(Math.pow(height,2)),1);
    }

    //output height in string form with feet and inches
    public static String heightOutput(int height){
        int feet = height/12;
        int inches = height%12;
        return (feet + "\"" + inches + "'");
    }

    //delete user with delete request
    public static void deleteUser(String user) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bodyfatpinchtest.appspot.com/user/" + user;
        Request request = new Request.Builder()
                .url(url)
                .delete()
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
                Log.d(TAG, resp);
            }
        });
    }

    //go to update user info function using overflow menu item
    public static void goToUpdateUserInfo(String user_id, Context context){
        Intent update_user = new Intent(context.getApplicationContext(), UpdateUserInfo.class);
        update_user.putExtra("user_id", user_id);
        context.startActivity(update_user);
    }
}
