package com.lbxq.screen;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

public class XscreenModule implements IXposedHookLoadPackage {

    private static final Map<String, String> PACKAGE_NAMES = new HashMap<>();

    static {
        PACKAGE_NAMES.put("com.tencent.mm", "微信");
        PACKAGE_NAMES.put("com.tencent.mobileqq", "QQ");
        PACKAGE_NAMES.put("com.eg.android.AlipayGphone", "支付宝");
        PACKAGE_NAMES.put("com.ss.android.ugc.aweme", "抖音");
        PACKAGE_NAMES.put("com.sankuai.meituan", "美团");
        PACKAGE_NAMES.put("com.sankuai.meituan.dispatch.crowdsource.delivery", "美团众包");
        PACKAGE_NAMES.put("me.ele.crowdsource", "蜂鸟众包");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        String pkg = lpparam.packageName;
        if (!PACKAGE_NAMES.containsKey(pkg)) return;

        String appName = PACKAGE_NAMES.get(pkg);
        XposedBridge.log("[Xscreen] 已检测到：" + appName + "（" + pkg + "）启动");

        // 1. Hook setFlags
        hookSetFlags(lpparam, pkg, appName);
        // 2. Hook addFlags
        hookAddFlags(lpparam, pkg, appName);
        // 3. Hook SurfaceView.setSecure
        hookSurfaceViewSecure(lpparam, pkg, appName);
        // 4. Hook View.setSecure (Android 10+)
        hookViewSecure(lpparam, pkg, appName);
    }

    private void hookSetFlags(XC_LoadPackage.LoadPackageParam lpparam, String pkg, String appName) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.view.Window",
                lpparam.classLoader,
                "setFlags",
                int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int flags = (int) param.args[0];
                        if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                            flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                            param.args[0] = flags;
                            XposedBridge.log("[Xscreen] setFlags 已清除 " + appName + " 的 FLAG_SECURE");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("[Xscreen] setFlags Hook 失败: " + t.getMessage());
        }
    }

    private void hookAddFlags(XC_LoadPackage.LoadPackageParam lpparam, String pkg, String appName) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.view.Window",
                lpparam.classLoader,
                "addFlags",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int flags = (int) param.args[0];
                        if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                            flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                            param.args[0] = flags;
                            XposedBridge.log("[Xscreen] addFlags 已清除 " + appName + " 的 FLAG_SECURE");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("[Xscreen] addFlags Hook 失败: " + t.getMessage());
        }
    }

    private void hookSurfaceViewSecure(XC_LoadPackage.LoadPackageParam lpparam, String pkg, String appName) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.view.SurfaceView",
                lpparam.classLoader,
                "setSecure",
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        boolean secure = (boolean) param.args[0];
                        if (secure) {
                            param.args[0] = false;
                            XposedBridge.log("[Xscreen] SurfaceView.setSecure 已拦截（" + appName + "）");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            // 低版本 Android 可能没有该方法，忽略
            XposedBridge.log("[Xscreen] SurfaceView.setSecure Hook 失败（可忽略）: " + t.getMessage());
        }
    }

    private void hookViewSecure(XC_LoadPackage.LoadPackageParam lpparam, String pkg, String appName) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.view.View",
                lpparam.classLoader,
                "setSecure",
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        boolean secure = (boolean) param.args[0];
                        if (secure) {
                            param.args[0] = false;
                            XposedBridge.log("[Xscreen] View.setSecure 已拦截（" + appName + "）");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            // Android 10 以下可能没有该方法
            XposedBridge.log("[Xscreen] View.setSecure Hook 失败（可忽略）: " + t.getMessage());
        }
    }
}
