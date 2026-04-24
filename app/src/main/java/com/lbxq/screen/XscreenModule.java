package com.lbxq.screen;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XscreenModule implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            String pkg = lpparam.packageName;
            if ("com.tencent.mm".equals(pkg) || "com.eg.android.AlipayGphone".equals(pkg)) {
                // 空逻辑，只让 LSPosed 识别到模块
            }
        } catch (Throwable ignored) {}
    }
}
