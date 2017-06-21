package com.twitteragent.com.twitteragent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.twitteragent.com.twitteragent.Volley.Singleton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    public static final int requestInternetPermissions = 100;
    public final static String TAG = MainActivity.class.getSimpleName();
    private RequestQueue queue;
    private String url = "https://api.twitter.com/oauth/request_token";
    private String secret = "Qu17wtaFirzvnCHT1YpwWbNzJcS6XpcrbZiTkdStE3j6VSNT8n";
    String ENC = "UTF-8";
    String signature = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Singleton.getInstance(this).getRequestQueue();

        Date date = new Date();
        Long timestamp = new Long(date.getTime()/1000);
        final String oauth_timestamp = timestamp.toString();
        //final String oauth_timestamp = "1498002538";
        Log.d(TAG," timestamp " + oauth_timestamp);


        byte[] nonceByte = oauth_timestamp.getBytes();
        int nonce =  ((int) (Math.random() * 100000000));
        final String oauth_nonce = String.valueOf(nonce);
        //final String oauth_nonce =  "muigmM";
        Log.d(TAG," oauth_nonce " + oauth_nonce);


        StringBuilder base = new StringBuilder();
        base.append("GET&");
        base.append(url);
        base.append("&");

        System.out.println(" base  " + base);
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecretKeySpec sec = new SecretKeySpec(secret.getBytes(), mac.getAlgorithm());
        try {
            mac.init(sec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] digest = mac.doFinal(base.toString().getBytes());
        byte[] result=Base64.encode(digest, Base64.DEFAULT);
        final String oauth_signature =  Base64.encodeToString(nonceByte,Base64.DEFAULT);
        Log.d(TAG,"result oauth_nonce " +oauth_signature);


        StringBuilder params = new StringBuilder();
        params.append("oauth_consumer_key=fLYk3sCLZUbcSalUvSTio5MjF&");
        params.append("oauth_nonce="+oauth_nonce+"&");
        params.append("oauth_signature_method=HMAC-SHA1&");
        params.append("oauth_timestamp="+oauth_timestamp+"&");
        params.append("oauth_version=1.0");

        try {
            signature = getSignature(URLEncoder.encode(url, ENC), URLEncoder.encode(params.toString(), ENC));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


            Log.d(TAG,"OAuth oauth_consumer_key=\"fLYk3sCLZUbcSalUvSTio5MjF\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\""+oauth_timestamp+"\",oauth_nonce=\""+oauth_nonce+"\",oauth_version=\"1.0\",oauth_signature=\""+signature+"\"");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, requestInternetPermissions);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
            final TextView mTextView = (TextView) findViewById(R.id.text);
            url = "https://api.twitter.com/oauth/request_token";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mTextView.setText("Response is: " + response);
                            Log.d(TAG,"Response "+response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mTextView.setText("That didn't work!");
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    params.put("Authorization", "OAuth oauth_consumer_key=\"fLYk3sCLZUbcSalUvSTio5MjF\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\""+oauth_timestamp+"\",oauth_nonce=\""+oauth_nonce+"\",oauth_version=\"1.0\",oauth_signature=\""+signature+"\"");
                    return params;
                }
            };
            queue.add(stringRequest);
        }
    }

    private static String getSignature(String url, String params)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        String secret = "Qu17wtaFirzvnCHT1YpwWbNzJcS6XpcrbZiTkdStE3j6VSNT8n";
        final String HMAC_SHA1 = "HMAC-SHA1";
        final String ENC = "UTF-8";
        StringBuilder base = new StringBuilder();
        base.append("GET&");
        base.append(url);
        base.append("&");
        base.append(params);
        byte[] keyBytes = (secret + "&").getBytes(ENC);
        SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(key);
        byte[] signaturebytes = mac.doFinal(base.toString().getBytes(ENC));
        String encoded = Base64.encodeToString(signaturebytes, Base64.DEFAULT).trim();
        return URLEncoder.encode(encoded, ENC);
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

    private static String generateNonce() throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException
    {
        String dateTimeString = Long.toString(new Date().getTime());
        byte[] nonceByte = dateTimeString.getBytes();
        return Base64.encodeToString(nonceByte,Base64.DEFAULT);
    }

}
