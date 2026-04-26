package com.lbxq.screen;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import android.view.WindowManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class XscreenModule implements IXposedHookLoadPackage {

    // 包名 -> 中文名称映射
    private static final Map<String, String> PACKAGE_NAME_MAP = new HashMap<>();

    static {
        PACKAGE_NAME_MAP.put("com.tencent.mm", "微信");
        PACKAGE_NAME_MAP.put("com.tencent.mobileqq", "QQ");
        PACKAGE_NAME_MAP.put("com.eg.android.AlipayGphone", "支付宝");
        PACKAGE_NAME_MAP.put("com.ss.android.ugc.aweme", "抖音");
        PACKAGE_NAME_MAP.put("com.sankuai.meituan", "美团");
        PACKAGE_NAME_MAP.put("com.sankuai.meituan.dispatch.crowdsource.delivery", "美团众包");
        PACKAGE_NAME_MAP.put("me.ele.crowdsource", "蜂鸟众包");
    }

    // 存储已记录过的包名（避免同一应用多进程重复输出和Hook）
    private static final Set<String> HOOKED_PACKAGES = new HashSet<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null || lpparam.packageName == null) {
            return;
        }

        String pkg = lpparam.packageName;

        if (!PACKAGE_NAME_MAP.containsKey(pkg)) {
            return;
        }

        String appName = PACKAGE_NAME_MAP.get(pkg);
        String processName = lpparam.processName;

        // 输出启动检测日志（每个进程记录一次）
        String logMsg = String.format("[Xscreen] [INFO] 检测到%s(%s)已启动，测试成功！🎉🎉🎉", appName, pkg);
        XposedBridge.log(logMsg);
        XposedBridge.log(String.format("[Xscreen] [DEBUG] PID=%d, Process=%s",
                android.os.Process.myPid(), processName));

        // 避免重复Hook（同一个应用的多个进程只Hook一次，避免性能开销）
        synchronized (HOOKED_PACKAGES) {
            if (HOOKED_PACKAGES.contains(pkg)) {
                return;
            }
            HOOKED_PACKAGES.add(pkg);
        }

        // 添加解除截图限制的Hook
        hookScreenCapture(lpparam);
    }

    /**
     * Hook 所有限制截图的相关方法
     * 参考：https://github.com/Dev97633/FLAG_SECURE-next
     */
    private void hookScreenCapture(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // 1. Hook Window.setFlags() - 移除 FLAG_SECURE
            XposedHelpers.findAndHookMethod(
                    "android.view.Window",
                    lpparam.classLoader,
                    "setFlags",
                    int.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int flags = (int) param.args[0];
                            // 如果包含了 FLAG_SECURE，则将其清除
                            if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                                flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                                param.args[0] = flags;
                                XposedBridge.log("[Xscreen] [INFO] 检测到%s(%s)已启动，测试成功！🎉🎉🎉");
                            }
                        }
                    }
            );

            // 2. Hook Window.addFlags() - 另一种添加标志位的方式
            XposedHelpers.findAndHookMethod(
                    "android.view.Window",
                    lpparam.classLoader,
                    "addFlags",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int flags = (int) param.args[0];
                            if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                                // 直接跳过这个标志位，不添加到参数中
                                flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                                param.args[0] = flags;
                                XposedBridge.log("[Xscreen] [INFO] 检测到%s(%s)已被提取权限，任务完成✅！");
                            }
                        }
                    }
            );

            // 3. Hook SurfaceView.setSecure() - 禁用 SurfaceView 的安全标志
            try {
                XposedHelpers.findAndHookMethod(
                        "android.view.SurfaceView",
                        lpparam.classLoader,
                        "setSecure",
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                // 强制设置为 false，关闭安全模式
                                param.args[0] = false;
                                XposedBridge.log("[Xscreen] [DEBUG] 已禁用 SurfaceView.setSecure");
                            }
                        }
                );
            } catch (Throwable t) {
                // 部分 Android 版本可能没有这个方法，静默忽略
                XposedBridge.log("[Xscreen] [WARN] SurfaceView.setSecure Hook 失败: " + t.getMessage());
            }

            XposedBridge.log("[Xscreen] [INFO] 解除截图限制 Hook 已注入成功");
        } catch (Throwable t) {
            XposedBridge.log("[Xscreen] [ERROR] 截图限制 Hook 注入失败: " + t.getMessage());
        }
    }
}
