package com.example.zhi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.starway.starrobot.commonability.RobotType;
import com.starway.starrobot.commonability.StarCommonAbility;
import com.starway.starrobot.commonability.hardware.EmojiHelper;
import com.starway.starrobot.logability.StarLogAbility;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private String[] mPermissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!requestPremission(mPermissions)) {
            init();
        }
    }

    private void init() {
        initRobotAbility();
    }
    /**
     * 是否需要申请权限，同时会申请权限
     * @param permissions
     * @return
     */
    private boolean requestPremission(String[] permissions) {
        ArrayList<String> needPermission = new ArrayList<>();
        //检查录音权限
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int checkRecordAudioPermission = ContextCompat.checkSelfPermission(this, permission);
            if (checkRecordAudioPermission != PackageManager.PERMISSION_GRANTED) {
                needPermission.add(permission);
            }
        }
        if (needPermission.size() > 0) {
            String[] requestPermissions = new String[needPermission.size()];
            for (int i = 0; i < needPermission.size(); i++) {
                requestPermissions[i] = needPermission.get(i);
            }
            ActivityCompat.requestPermissions(this, requestPermissions, 0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    /**
     * 初始化机器人基础能力
     */
    private void initRobotAbility() {
        //日志初始化
        StarLogAbility.getInstance().initAbility(this);
        StarCommonAbility.getInstance().initAbility(this.getApplicationContext(), RobotType.TYPE_TEACHING, new StarCommonAbility.onResultCallback() {
            @Override
            public void onResult(boolean isSuccess, String hard_code) {
                if (isSuccess) {
                    //硬件和业务状态初始化
                    //这里根据不同的标识返回不同硬件的初始化成功状态
                    switch (hard_code) {
                        case "emoji": //表情硬件初始化成功

                            //设置基础表情
                            EmojiHelper.doEmojiBase();

                            //延迟展示Love表情
                            showEmojiEffectDelay();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    /**
     * 延迟展示机器人表情（Love)
     */
    private void showEmojiEffectDelay() {
        //延迟3秒展示Love表情
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EmojiHelper.doEmojiLove();
            }
        }, 3000);
    }
}