package com.example.zhi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.iflytek.aiui.AIUIEvent;
import com.starway.starrobot.aiuiability.AIUIAbility;
import com.starway.starrobot.aiuiability.NLPListener;
import com.starway.starrobot.aiuiability.SpeechHelper;
import com.starway.starrobot.aiuiability.TTS;
import com.starway.starrobot.commonability.RobotType;
import com.starway.starrobot.commonability.StarCommonAbility;
import com.starway.starrobot.commonability.hardware.EmojiHelper;
import com.starway.starrobot.logability.StarLogAbility;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NLPListener,ActivityCompat.OnRequestPermissionsResultCallback{

    private final String[] mPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private TextView txtView;
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = findViewById(R.id.txtArea);

        // 申请权限
        if (!requestPremission(mPermissions)) {
            init();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AIUIAbility.getInstance().release();
    }

    /**
     * NLP 回调
     */
    @Override
    public void onAiuiResponse(String semantic) {
//        if (TextUtils.isEmpty(bean)) {
//            return;
//        }
//        try {
//            JSONObject object = new JSONObject(bean);
//            JSONObject intentObject = object.optJSONObject("intent");
//            if (intentObject == null) return;
//
//            final String text = intentObject.optString("text", "");
//            final String answer = intentObject.optString("answer", "");
//
//            runOnUiThread(() -> {
//                sb.setLength(0);
//                if (!TextUtils.isEmpty(text)) {
//                    sb.append("识别: ").append(text).append("\n");
//                }
//                if (!TextUtils.isEmpty(answer)) {
//                    sb.append("回答: ").append(answer);
//                }
//                txtView.setText(sb.toString());
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Log.i("test", "test:" + semantic);
        if (TextUtils.isEmpty(semantic)) {
            return;
        }

        try {
            JSONObject object = new JSONObject(semantic);
            if (null == object) {
                return;
            }
            JSONObject intentObject = object.optJSONObject("intent");
            if (null == intentObject) {
                return;
            }

            final TextView txtArea = (TextView) findViewById(R.id.txtArea);

            if (intentObject.has("text")) {
                String txt = intentObject.getString("text");

                if (TextUtils.equals(txt, "你是谁")) {
                    txtArea.setText("我是小途，很高兴为您服务。");
                    SpeechHelper.getInstance().speak("我是小途，很高兴为您服务。");
                } else if (TextUtils.equals(txt, "你会干什么")) {
                    txtArea.setText("我可以陪你聊天，为你解答问题，还可以给你唱歌哦。");
                    SpeechHelper.getInstance().speak("我可以陪你聊天，为你解答问题，还可以给你唱歌哦。", new TTS.OnSpeakCallback() {
                        @Override
                        public void onSpeak(String s) {
                            //需要在主线程中进行界面元素的修改
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtArea.setText("您好，请问有什么可以帮您？");
                                }
                            });
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAiuiWakeUp() {
    }

    @Override
    public void onAiuiSleep() {
    }

    @Override
    public void onAiuiEvent(AIUIEvent aiuiEvent) {
    }

    @Override
    public void onError(int i) {
    }

    /**
     * 申请权限
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    /**
     * 初始化逻辑
     */
    private void init() {
        // 初始化机器人能力
        initRobotAbility();

        Log.i("test", "init");
        AIUIAbility.getInstance().initAIUIAbility(this);
        AIUIAbility.getInstance().addNLPListener(this);
        SpeechHelper.getInstance().initSpeech(this);
        //AIUIAbility.getInstance().setAiuiSubType();
        SpeechHelper.getInstance().setVoicer("xiaofeng");
        AIUIAbility.getInstance().start();
    }

    /**
     * 初始化机器人基础能力
     */
    private void initRobotAbility() {
        // 日志初始化
        StarLogAbility.getInstance().initAbility(this);

        StarCommonAbility.getInstance().initAbility(
                this.getApplicationContext(),
                RobotType.TYPE_TEACHING,
                (isSuccess, hard_code) -> {
                    if (isSuccess) {
                        if ("emoji".equals(hard_code)) {
                            EmojiHelper.doEmojiBase();
                            showEmojiEffectDelay();
                        }
                    }
                });
    }

    /**
     * 延迟展示 Love 表情
     */
    private void showEmojiEffectDelay() {
        final Handler handler = new Handler();
        Runnable showLove = new Runnable() {
            @Override
            public void run() {
                EmojiHelper.doEmojiLove();
                handler.postDelayed(this, 2000); // 每2秒刷新一次，保持显示
            }
        };
        handler.postDelayed(showLove, 3000); // 延迟3秒开始
    }
}
