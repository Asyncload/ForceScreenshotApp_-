package com.example.forcescreenshot;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;

import java.io.IOException;

// 通过系统命令实现后台截图（root设备专用）
public class ScreenshotService extends AccessibilityService {
    private static final String TAG = "ScreenshotService";

    @Override
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {
        // 监听系统事件，触发截图
        takeScreenshot();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "服务中断");
    }

    private void takeScreenshot() {
        try {
            // 直接调用系统命令进行截图（root设备专用）
            Process process = Runtime.getRuntime().exec("screencap /sdcard/screenshot.png");
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Log.d(TAG, "截图成功");
            } else {
                Log.e(TAG, "截图失败，错误码: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "截图异常: " + e.getMessage());
        }
    }
}
