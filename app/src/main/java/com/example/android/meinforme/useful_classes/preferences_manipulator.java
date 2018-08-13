package com.example.android.meinforme.useful_classes;

import android.content.Context;

public class preferences_manipulator {

    private static String PREFERENCE_NAME = "userLog";
    private static int PREFERENCE_MODE = 0;

    public static boolean getBoolean(Context context, String key, boolean defaultValue){
        return context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).getBoolean(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value){
        context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).edit().putBoolean(key, value).apply();
    }

    public static String getString(Context context, String key, String defaultValue){
        return context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).getString(key, defaultValue);
    }

    public static void putString(Context context, String key, String value){
        context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).edit().putString(key, value).apply();
    }

    public static int getInt(Context context, String key, int defaultValue){
        return context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).getInt(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value){
         context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).edit().putInt(key, value).apply();
    }

    public static void deletAllPreferences(Context context){
        context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).edit().clear().apply();
    }

    public static void deletkey(Context context, String Key){
        context.getSharedPreferences(PREFERENCE_NAME, PREFERENCE_MODE).edit().remove(Key).apply();
    }
}
