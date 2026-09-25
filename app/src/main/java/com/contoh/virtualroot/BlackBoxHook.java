package com.contoh.virtualroot;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Method;

public class BlackBoxHook {

    private static final String TAG = "BlackBoxHook";
    private static boolean applied = false;

    public static void apply(Context ctx) {
        if (applied) return;
        try {
            StorageSandbox.get(ctx);
            GGHelper.get(ctx).setup();

            try {
                Class<?> coreClass = Class.forName("top.niunaijun.blackbox.BlackBoxCore");
                Method getMethod = coreClass.getMethod("get");
                Object core = getMethod.invoke(null);
                if (core != null) {
                    File sandboxData = new File(StorageSandbox.get(ctx).getRoot(), "bb_data");
                    if (!sandboxData.exists()) sandboxData.mkdirs();
                    Log.i(TAG, "BlackBox data dir: " + sandboxData.getAbsolutePath());
                }
            } catch (ClassNotFoundException e) {
                Log.w(TAG, "BlackBoxCore not found");
            } catch (Exception e) {
                Log.w(TAG, "BlackBox hook partial: " + e.getMessage());
            }

            applied = true;
            Log.i(TAG, "BlackBoxHook applied");
        } catch (Exception e) {
            Log.e(TAG, "BlackBoxHook failed: " + e.getMessage());
        }
    }

    public static boolean isApplied() { return applied; }
}
