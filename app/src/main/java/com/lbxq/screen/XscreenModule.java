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

    // 包名 -> 中文名称映射
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
        if (!PACKAGE_NAMES.containsKey(pkg)) {
            return;
        }
        String appName = PACKAGE_NAMES.get(pkg);
        XposedBridge.log("[Xscreen] 已检测到: " + appName + " (" + pkg + ") 启动");

        // 1. Hook setFlags (同时处理 flags 和 mask)
        XposedHelpers.findAndHookMethod(
            "android.view.Window",
            lpparam.classLoader,
            "setFlags",
            int.class, int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    int flags = (int) param.args[0];
                    int mask = (int) param.args[1];
                    boolean modified = false;

                    // 如果 flags 中包含 FLAG_SECURE，则清除
                    if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                        flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                        param.args[0] = flags;
                        modified = true;
                    }
                    // 确保 mask 包含 FLAG_SECURE，以便清除生效
                    if ((mask & WindowManager.LayoutParams.FLAG_SECURE) == 0) {
                        mask |= WindowManager.LayoutParams.FLAG_SECURE;
                        param.args[1] = mask;
                        modified = true;
                    }
                    if (modified) {
                        XposedBridge.log("[Xscreen] 已解除 " + appName + " 的截图限制 (setFlags)");
                    }
                }
            }
        );

        // 2. Hook addFlags (只传入一个 flags 参数)
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
                        XposedBridge.log("[Xscreen] 已解除 " + appName + " 的截图限制 (addFlags)");
                    }
                }
            }
        );
    }
}
