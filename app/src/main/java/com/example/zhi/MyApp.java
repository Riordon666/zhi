package com.example.zhi;

import android.app.Application;

import com.starway.starrobot.commonability.RobotType;
import com.starway.starrobot.commonability.StarCommonAbility;
import com.starway.starrobot.commonability.hardware.CenterLightHelper;
import com.starway.starrobot.commonability.hardware.EmojiHelper;
import com.starway.starrobot.commonability.hardware.GPIOHelper;
import com.starway.starrobot.logability.StarLogAbility;
import com.starway.starrobot.logability.log.PartCode;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        StarLogAbility.getInstance().initAbility(this);
        //基础能力初始化
        StarCommonAbility.getInstance().initAbility(this,
                RobotType.TYPE_TEACHING, new StarCommonAbility.onResultCallback() {
                    @Override
                    public void onResult(boolean isSuccess, String hardCode) {
                        if (isSuccess) {
                            //硬件和业务状态初始化
                            switch (hardCode) {
                                case PartCode.HARDWARE_PARTCODE.CODE_EMOJI:
                                    //设置初始表情
                                    EmojiHelper.doEmojiBase();
                                    break;
                                case PartCode.HARDWARE_PARTCODE.CODE_GPIO:
                                    //默认加载的时候，将拾音方向设置为默认正前方的0度。
                                    GPIOHelper.getInstance().setMainMic(0);
                                    break;
                                case PartCode.HARDWARE_PARTCODE.CODE_CENTER_LIGHT:
                                    //关闭腹部灯带
                                    CenterLightHelper.takeCenterLightOff();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                });
    }
}
