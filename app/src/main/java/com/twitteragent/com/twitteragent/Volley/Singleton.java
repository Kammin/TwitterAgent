package com.twitteragent.com.twitteragent.Volley;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class Singleton {

    private static Singleton instance;
    private RequestQueue requestQueue;
    private Context context;

    private Singleton (Context context){
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        instance = this;
    }

    public static synchronized Singleton getInstance(Context context){
        if(instance==null)
            instance = new Singleton(context);
        return instance;
    }

    public RequestQueue getRequestQueue(){
        return requestQueue;

    }
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
