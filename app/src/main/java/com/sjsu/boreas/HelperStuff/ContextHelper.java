package com.sjsu.boreas.HelperStuff;

import android.content.Context;
import android.util.Log;

public class ContextHelper {

    private Context applicationContext = null;
    private static ContextHelper contextHelper = null;

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "--------Context Helper-- ";

    public static ContextHelper getContextHelper(Context context){
        Log.e(TAG, SUB_TAG+"get context helper instance");
        if(contextHelper == null){
            contextHelper = new ContextHelper(context);
            return contextHelper;
        }//Only the main activity should give context
        //  everyother class should pass null
        else if(context == null){
            return contextHelper;
        }
        return null;
    }

    public ContextHelper(Context context){
        Log.e(TAG, SUB_TAG+"Constructor for context helper");
        this.applicationContext = context;
    }

    public Context getApplicationContext() {
        Log.e(TAG, SUB_TAG+"Getting the application context");
        return applicationContext;
    }
}
