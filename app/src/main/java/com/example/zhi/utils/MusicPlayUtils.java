package com.example.zhi.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

public class MusicPlayUtils {

    private Context mContext;

    private MediaPlayer mMediaPlayer;

    /**
     * 监听播放完成
     */
    public interface onPlayCompletedCallback {
        void onCompleted();
    }

    public MusicPlayUtils(Context ctx){
        mContext = ctx;
        mMediaPlayer = new MediaPlayer();
    }

    /**
     * 判断是否正在播放
     * @return
     */
    public boolean isPlaying() {
        try {
            return mMediaPlayer.isPlaying();
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    /**
     * 暂停播放
     */
    public void pause(){
        if(null != mMediaPlayer && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    /**
     * 继续播放
     */
    public void resume(){
        if(null != mMediaPlayer){
            mMediaPlayer.start();
        }
    }

    /**
     * 播放
     * @param fileName 音频文件地址
     * @param mCallback 播完的回调
     * @return
     * @throws JSONException
     */
    public void playLocalMedia(String fileName, final onPlayCompletedCallback mCallback) {
        if (null == mMediaPlayer) {
            return;
        }
        if(mMediaPlayer.isPlaying()){
            Log.e("MediaPlayer", "MediaPlayer is busying now!");
            return;
        }
        try {
            AssetManager am = mContext.getAssets();
            mMediaPlayer.setDataSource(am.openFd(fileName));
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mCallback.onCompleted();
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
