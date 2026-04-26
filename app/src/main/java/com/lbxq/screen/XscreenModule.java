package com.lbxq.screen;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

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

    // 存储已记录过的包名（避免同一应用多进程重复输出）
    private static final Set<String> LOGGED_PACKAGES = new HashSet<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null || lpparam.packageName == null) {
            return;
        }

        String pkg = lpparam.packageName;

        if (!PACKAGE_NAME_MAP.containsKey(pkg)) {
            return;
        }

        synchronized (LOGGED_PACKAGES) {
            if (LOGGED_PACKAGES.contains(pkg)) {
                return;
            }
            LOGGED_PACKAGES.add(pkg);
        }

        String appName = PACKAGE_NAME_MAP.get(pkg);
        // 模仿 HMA-OSS 风格的日志格式：[模块名] [级别] 内容
        String logMsg = String.format("[Xscreen] [INFO] 检测到%s(%s)已启动，测试成功！🎉🎉🎉",
                appName, pkg);
        XposedBridge.log(logMsg);

        // 可选：额外输出一次包含进程信息的调试日志（也可以去掉）
        // XposedBridge.log(String.format("[Xscreen] [DEBUG] PID=%d, Process=%s",
        //         android.os.Process.myPid(), lpparam.processName));
    }
}
