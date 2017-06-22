package com.twitteragent.com.twitteragent.Util;


import android.util.Log;

public class Str {

    public static String stringBetween(String resource, String start, String end){
        int pos1 = resource.indexOf(start);
            pos1 += start.length();

        int pos2 = resource.indexOf(end);

        Log.d(""," "+pos1+" "+pos2);
        return resource.substring(pos1,pos2);
    }
}
