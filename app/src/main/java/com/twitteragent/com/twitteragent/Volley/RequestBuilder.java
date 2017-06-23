package com.twitteragent.com.twitteragent.Volley;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.twitteragent.com.twitteragent.Util.Str;
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

import static com.twitteragent.com.twitteragent.Util.URLs.URL_request_token;

public class RequestBuilder {
    public final static String ENC = "UTF-8";
    public final static String TAG = RequestBuilder.class.getSimpleName();

    public static void get_request_token(final Context context){

        String signature = "";
        Date date = new Date();
        Long timestamp = new Long(date.getTime()/1000);
        final String oauth_timestamp = timestamp.toString();

        byte[] nonceByte = oauth_timestamp.getBytes();
        int nonce =  ((int) (Math.random() * 100000000));
        final String oauth_nonce = String.valueOf(nonce);

        StringBuilder base = new StringBuilder();
        base.append("GET&");
        base.append(URL_request_token);
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
        byte[] result=Base64.encode(digest, Base64.DEFAULT);
        final String oauth_signature =  Base64.encodeToString(nonceByte,Base64.DEFAULT);
        Log.d(TAG,"result oauth_nonce " +oauth_signature);

        StringBuilder params = new StringBuilder();
        params.append("oauth_consumer_key="+URLs.consumerKey+"&");
        params.append("oauth_nonce="+oauth_nonce+"&");
        params.append("oauth_signature_method=HMAC-SHA1&");
        params.append("oauth_timestamp="+oauth_timestamp+"&");
        params.append("oauth_version=1.0");


        try {
            signature = getSignature(URLEncoder.encode(URL_request_token, ENC), URLEncoder.encode(params.toString(), ENC));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"result signature " +signature);

        final String authorization = "OAuth oauth_consumer_key=\""+URLs.consumerKey+"\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\""+oauth_timestamp+"\",oauth_nonce=\""+oauth_nonce+"\",oauth_version=\"1.0\",oauth_signature=\""+signature+"\"";

        StringRequest request_token = new StringRequest(Request.Method.GET, URL_request_token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String params = "?oauth_token="+Str.stringBetween(response.toString(),"oauth_token=","&oauth_token_secret=");
                        Log.d(TAG,"Response oauth_token    "+ params);
                        authenticity_token(context,params);
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
                params.put("Authorization", authorization);
                params.put("oauth_callback", "oob");
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request_token);
    }


    public static void authenticity_token(Context context, String params){
        Log.d(TAG,"Response authenticity_token "+ URLs.URL_authorize+params);
        StringRequest authenticity_token = new StringRequest(Request.Method.GET,URLs.URL_authorize+params,
        //StringRequest authenticity_token = new StringRequest(Request.Method.GET,"https://api.twitter.com/oauth/authorize?oauth_token=LpNmHQAAAAAA1GhwAAABXM_sgrQ",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d(TAG,"Response authenticity_token "+ response);

                        String authenticity_token = Str.stringBetween(response,"name=\"authenticity_token\" type=\"hidden\" value=\"","\">");
                        Log.d(TAG,"Response authenticity_token "+ authenticity_token);
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
                params.put("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
                params.put("Accept-Language", "ru");
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(authenticity_token);
    }





    private static String getSignature(String url, String params)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        String secret = URLs.consumerSecret ;
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
