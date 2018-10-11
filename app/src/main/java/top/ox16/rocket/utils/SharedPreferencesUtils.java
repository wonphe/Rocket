package top.ox16.rocket.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    private static final String SP_NAME = "config";
    private static SharedPreferences sp;

    /**
     * 存储Boolean类型内容
     *
     * @param context 上下文
     * @param key     存储节点名称
     * @param value   存储节点的值（Boolean）
     */
    public static void putBoolean(Context context, String key, boolean value) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        sp.edit().putBoolean(key, value).apply();
    }

    /**
     * 读取Boolean类型内容
     *
     * @param context  上下文
     * @param key      存储节点名称
     * @param defValue 默认存储节点的值（Boolean）
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key, defValue);
    }

    /**
     * 存储String类型内容
     *
     * @param context 上下文
     * @param key     存储节点名称
     * @param value   存储节点的值（String）
     */
    public static void putString(Context context, String key, String value) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        sp.edit().putString(key, value).apply();
    }

    /**
     * 读取String类型内容
     *
     * @param context  上下文
     * @param key      存储节点名称
     * @param defValue 默认存储节点的值（String）
     */
    public static String getString(Context context, String key, String defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp.getString(key, defValue);
    }

    /**
     * 从sp中移除指定节点
     *
     * @param context 上下文
     * @param key     需要移除的节点名称
     */
    public static void remove(Context context, String key) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        sp.edit().remove(key).apply();
    }

    /**
     * 读取Int类型内容
     *
     * @param context  上下文
     * @param key      存储节点名称
     * @param defValue 默认存储节点的值（Int）
     */
    public static int getInt(Context context, String key, int defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp.getInt(key, defValue);
    }

    /**
     * 存储Int类型内容
     *
     * @param context 上下文
     * @param key     存储节点名称
     * @param value   存储节点的值（Int）
     */
    public static void putInt(Context context, String key, int value) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        sp.edit().putInt(key, value).apply();
    }
}
