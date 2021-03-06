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
import static com.twitteragent.com.twitteragent.Util.URLs.oauth_token;

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
        Log.d(TAG,"authorization "+authorization);

        StringRequest request_token = new StringRequest(Request.Method.GET, URL_request_token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String oauth_token = Str.stringBetween(response.toString(),"oauth_token=","&oauth_token_secret=");
                        Log.d(TAG,"Response oauth_token    "+ oauth_token);
                        URLs.oauth_token = oauth_token;
                        authenticity_token(context);
                        //authenticate(context,oauth_token);
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
                //params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.109 Safari/537.36");
                params.put("oauth_callback", "oob");
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request_token);
    }






    public static void authenticity_token(final Context context){
        final String oauth_tok = oauth_token;
        Log.d(TAG,"Response URL  "+ URLs.URL_authorize+"?oauth_token="+ oauth_token);
        StringRequest authenticity_token = new StringRequest(Request.Method.GET,URLs.URL_authorize+"?oauth_token="+ oauth_token,
        //StringRequest authenticity_token = new StringRequest(Request.Method.GET,"https://api.twitter.com/oauth/authorize?oauth_token=LpNmHQAAAAAA1GhwAAABXM_sgrQ",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String authenticity_token = Str.stringBetween(response,"name=\"authenticity_token\" type=\"hidden\" value=\"","\">");
                        Log.d(TAG,"Response authenticity_token   "+ authenticity_token);
                        //get_pin_code(context,oauth_tok,authenticity_token);
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
                params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.109 Safari/537.36");
                params.put("Accept-Language", "ru");
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(authenticity_token);
    }

    public static void authenticate(final Context context, String oauth_token){
        final String oauth_tok = oauth_token;
        StringRequest authenticate = new StringRequest(Request.Method.GET,URLs.URL_authenticate+"?oauth_token="+oauth_token+"&session[username_or_email]=ibayka@gmail.com"+"&session[password]=Baylkal1234",
                //StringRequest authenticity_token = new StringRequest(Request.Method.GET,"https://api.twitter.com/oauth/authorize?oauth_token=LpNmHQAAAAAA1GhwAAABXM_sgrQ",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d(TAG,"Response authenticity_token "+ response);
                        //String authenticity_token = Str.stringBetween(response,"name=\"authenticity_token\" type=\"hidden\" value=\"","\">");
                        Log.d(TAG,"Response authenticate"+ response);
                        //get_pin_code(context,oauth_tok,authenticity_token);
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
                params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.109 Safari/537.36");
                params.put("Accept-Language", "ru");
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(authenticate);
    }





    public static void get_pin_code(Context context, String oauth_token, String authenticity_token){
        final String this_oauth_token = oauth_token;
        final String this_authenticity_token = authenticity_token;

        StringRequest get_pin_code = new StringRequest(Request.Method.POST,URLs.URL_authorize,
                //StringRequest authenticity_token = new StringRequest(Request.Method.GET,"https://api.twitter.com/oauth/authorize?oauth_token=LpNmHQAAAAAA1GhwAAABXM_sgrQ",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d(TAG,"Response authenticity_token "+ response);
                        //String authenticity_token = Str.stringBetween(response,"name=\"authenticity_token\" type=\"hidden\" value=\"","\">");
                        Log.d(TAG,"Response  length "+ response.length());
                        Log.d(TAG,"Response "+ response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Response That didn't work!");
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("Content-Type", "application/x-www-form-urlencoded");
               // params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/10.0.3071.109 Safari/537.36");
                //params.put("Accept-Language", "ru");
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("session[username_or_email]", URLs.login);
                params.put("session[password]", URLs.pass);
                params.put("oauth_token", this_oauth_token);
                params.put("authenticity_token", this_authenticity_token);
                //params.put("redirect_after_login","https://api.twitter.com/oauth/authorize?oauth_token="+this_oauth_token);

                return params;
            };

        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(get_pin_code);
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
