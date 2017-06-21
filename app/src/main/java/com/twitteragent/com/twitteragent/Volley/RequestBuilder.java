package com.twitteragent.com.twitteragent.Volley;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.twitteragent.com.twitteragent.Util.URLs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RequestBuilder {
    public final static String ENC = "UTF-8";
    public final static String TAG = RequestBuilder.class.getSimpleName();

    public static void get_request_token(Context context){
        Date date = new Date();
        Long timestamp = new Long(date.getTime()/1000);
        final String oauth_timestamp = timestamp.toString();

        byte[] nonceByte = oauth_timestamp.getBytes();
        int nonce =  ((int) (Math.random() * 100000000));
        final String oauth_nonce = String.valueOf(nonce);

        StringBuilder base = new StringBuilder();
        base.append("GET&");
        base.append(URLs.URL_request_token);
        base.append("&");
        System.out.println(" base  " + base);

        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecretKeySpec sec = new SecretKeySpec(URLs.consumerSecret.getBytes(), mac.getAlgorithm());
        try {
            mac.init(sec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] digest = mac.doFinal(base.toString().getBytes());
        byte[] result= Base64.encode(digest, Base64.DEFAULT);
        final String signature =  Base64.encodeToString(nonceByte,Base64.DEFAULT);

        StringBuilder params = new StringBuilder();
        params.append("oauth_consumer_key=fLYk3sCLZUbcSalUvSTio5MjF&");
        params.append("oauth_nonce="+oauth_nonce+"&");
        params.append("oauth_signature_method=HMAC-SHA1&");
        params.append("oauth_timestamp="+oauth_timestamp+"&");
        params.append("oauth_version=1.0");

        try {
            String request_token = getSignature(URLEncoder.encode(URLs.URL_request_token, ENC), URLEncoder.encode(params.toString(), ENC));
            Log.d(TAG,"result oauth_nonce " +request_token);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLs.URL_request_token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,"Response "+response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Response That didn't work!");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth oauth_consumer_key=\""+URLs.consumerKey+"\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\""+oauth_timestamp+"\",oauth_nonce=\""+oauth_nonce+"\",oauth_version=\"1.0\",oauth_signature=\""+signature+"\"");
                return params;
            }
        };
        RequestQueue queue = Singleton.getInstance(context).getRequestQueue();
        queue.add(stringRequest);
    }

    private static String getSignature(String url, String params)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        String secret = URLs.consumerSecret;
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
        Log.d(TAG,"encoded "+encoded);
        return URLEncoder.encode(encoded, ENC);
    }




}
