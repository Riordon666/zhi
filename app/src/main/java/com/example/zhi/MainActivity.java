package com.example.zhi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
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

public class MainActivity extends AppCompatActivity implements NLPListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private final String[] mPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private TextView txtView;
    private StringBuilder sb = new StringBuilder();
    private volatile boolean aiuiStarted = false;
    private volatile boolean aiuiReady = false;

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
    public void onAiuiResponse(String bean) {
        if (TextUtils.isEmpty(bean)) {
            return;
        }
        try {
            JSONObject object = new JSONObject(bean);
            JSONObject intentObject = object.optJSONObject("intent");
            if (intentObject == null) return;

            final String text = intentObject.optString("text", "");
            final String answer = intentObject.optString("answer", "");

            runOnUiThread(() -> {
                sb.setLength(0);
                if (!TextUtils.isEmpty(text)) {
                    sb.append("识别: ").append(text).append("\n");
                }
                if (!TextUtils.isEmpty(answer)) {
                    sb.append("回答: ").append(answer);
                }
                txtView.setText(sb.toString());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Log.i("test", "test:" + semantic);
//        if (TextUtils.isEmpty(semantic)) {
//            return;
//        }
//
//        try {
//            JSONObject object = new JSONObject(semantic);
//            if (null == object) {
//                return;
//            }
//            JSONObject intentObject = object.optJSONObject("intent");
//            if (null == intentObject) {
//                return;
//            }
//
//            final TextView txtArea = (TextView) findViewById(R.id.txtArea);
//
//            if (intentObject.has("text")) {
//                String txt = intentObject.getString("text");
//
//                if (TextUtils.equals(txt, "你是谁")) {
//                    txtArea.setText("我是小途，很高兴为您服务。");
//                    SpeechHelper.getInstance().speak("我是小途，很高兴为您服务。");
//                } else if (TextUtils.equals(txt, "你会干什么")) {
//                    txtArea.setText("我可以陪你聊天，为你解答问题，还可以给你唱歌哦。");
//                    SpeechHelper.getInstance().speak("我可以陪你聊天，为你解答问题，还可以给你唱歌哦。", new TTS.OnSpeakCallback() {
//                        @Override
//                        public void onSpeak(String s) {
//                            //需要在主线程中进行界面元素的修改
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    txtArea.setText("您好，请问有什么可以帮您？");
//                                }
//                            });
//                        }
//                    });
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onAiuiWakeUp() {
    }

    @Override
    public void onAiuiSleep() {
    }

    @Override
    public void onAiuiEvent(AIUIEvent aiuiEvent) {
        if (aiuiEvent == null) return;
        try {
            if (aiuiEvent.eventType == AIUIConstant.EVENT_STATE) {
                int state = aiuiEvent.arg1;
                // READY 或 WORKING 认为就绪
                boolean ready = (state == AIUIConstant.STATE_READY || state == AIUIConstant.STATE_WORKING);
                aiuiReady = ready;
                Log.i("AIUI", "状态变更: " + state + ", ready=" + ready);
                if (state == AIUIConstant.STATE_READY && !aiuiStarted) {
                    // 避免休眠影响
                    AIUIAbility.getInstance().resetSleep();
                    AIUIAbility.getInstance().setSleepEnable(false);
                    AIUIAbility.getInstance().start();
                    aiuiStarted = true;
                    Log.i("AIUI", "在 READY 后调用 AIUIAbility.start() 启动会话");
                }
            } else if (aiuiEvent.eventType == AIUIConstant.EVENT_RESULT) {
                // 回退解析：直接从 info 中尝试提取识别文本或答案
                String info = aiuiEvent.info;
                if (!TextUtils.isEmpty(info)) {
                    try {
                        JSONObject obj = new JSONObject(info);
                        JSONObject intent = obj.optJSONObject("intent");
                        if (intent != null) {
                            final String text = intent.optString("text", "");
                            final String answer = intent.optString("answer", "");
                            if (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(answer)) {
                                runOnUiThread(() -> {
                                    sb.setLength(0);
                                    if (!TextUtils.isEmpty(text)) sb.append("识别: ").append(text).append("\n");
                                    if (!TextUtils.isEmpty(answer)) sb.append("回答: ").append(answer);
                                    txtView.setText(sb.toString());
                                });
                            }
                        }
                    } catch (Throwable ignore) {}
                }
            } else if (aiuiEvent.eventType == AIUIConstant.EVENT_ERROR) {
                aiuiReady = false;
                Log.e("AIUI", "错误事件: " + aiuiEvent.arg1 + ", info=" + aiuiEvent.info);
            }
        } catch (Throwable t) {
            Log.e("AIUI", "onAiuiEvent 处理异常", t);
        }
    }

    @Override
    public void onError(int i) {
    }

    /**
     * 申请权限
     */
    private boolean requestPremission(String[] permissions) {
        ArrayList<String> needPermission = new ArrayList<>();
        for (String permission : permissions) {
            int check = ContextCompat.checkSelfPermission(this, permission);
            if (check != PackageManager.PERMISSION_GRANTED) {
                needPermission.add(permission);
            }
        }
        if (needPermission.size() > 0) {
            String[] requestPermissions = new String[needPermission.size()];
            requestPermissions = needPermission.toArray(requestPermissions);
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
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            init();
        } else {
            Toast.makeText(this, "请授予所有权限才能使用该应用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化逻辑
     */
    private boolean aiuiInited = false;

    private void init() {
        // 1️⃣ 初始化机器人能力
        initRobotAbility();

        // 0️⃣ 确保存储目录存在（/sdcard/msc 与 /sdcard/AIUI）
        ensureAiuiStorageDirs();

        // 2️⃣ 初始化 AIUI 能力
        AIUIAbility.getInstance().initAIUIAbility(this);
        // 2.1 绑定 NLP 监听，接收 AIUI 状态/结果
        AIUIAbility.getInstance().addNLPListener(this);

        // 5️⃣ 标记 AIUI 已初始化
        aiuiInited = true;

        // 6️⃣ 等待 READY 后再启动会话，避免早期调度错误

        // 7️⃣ 常规初始化语音
        SpeechHelper.getInstance().initSpeech(this);
        SpeechHelper.getInstance().setVoicer("xiaofeng");
    }

    private void ensureAiuiStorageDirs() {
        try {
            File sdRoot = Environment.getExternalStorageDirectory();
            File msc = new File(sdRoot, "msc");
            File aiui = new File(sdRoot, "AIUI");
            if (!msc.exists()) msc.mkdirs();
            if (!aiui.exists()) aiui.mkdirs();
        } catch (Throwable t) {
            Log.w("AIUI", "创建AIUI目录失败", t);
        }
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

    /**
     * AIUI 启动重试
     */
    private void startAIUIWithRetry(final int retryCount) {
        // 已改为单次延时启动，保留方法空实现以兼容调用
        new Handler().postDelayed(() -> AIUIAbility.getInstance().start(), 1500);
    }

    // 如需读取 aiui.cfg，可复用 assets 读取逻辑；当前保留在 Ability 内部


}
