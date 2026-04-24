package com.lbxq.screen;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定布局文件（项目里必须有activity_main.xml，没有的话我给你极简版）
        setContentView(R.layout.activity_main);
    }
}
