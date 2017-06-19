package com.twitteragent.com.twitteragent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.twitteragent.com.twitteragent.Volley.Singleton;

public class MainActivity extends AppCompatActivity {
    public static final int requestInternetPermissions = 100;
    public final static String TAG = MainActivity.class.getSimpleName();
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Singleton.getInstance(this).getRequestQueue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, requestInternetPermissions);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){;
            final TextView mTextView = (TextView) findViewById(R.id.text);
            String url = "http://www.google.com";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mTextView.setText("Response is: " + response.substring(0, 500));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mTextView.setText("That didn't work!");
                }
            });
            queue.add(stringRequest);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == requestInternetPermissions)
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Log.d(TAG, "GRANTED");
                if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                    Log.d(TAG, "DENIED");
            }
    }


}
