package com.wehaicao.ijkplayerdemo.helper;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class AudioFocusHelper {
    private static Context mContext;
    private static final String TAG  = "AudioFocusHelper";
    private volatile static AudioFocusHelper instance;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioManager mAudioManager;
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    private static final int AUDIO_FOCUSED = 2;
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;

    public static AudioFocusHelper getInstance(Context context) {
         mContext = context;
         if (instance == null) {
             synchronized (AudioFocusHelper.class) {
                 if (instance == null) {
                    instance = new AudioFocusHelper();
                 }
             }
         }
         return instance;
    }

    private AudioFocusHelper() {
        if (null != mContext) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    public void setAudioFocusChangeListener(AudioManager.OnAudioFocusChangeListener listener) {
        if (null == mAudioManager && null != mContext) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioFocusChangeListener = listener;
        Log.d(TAG, "setAudioFocusChangeListener");
    }

    /**
     * get AudioFocus,播放前调用,需先调用setAudioFocusChangeListener
     * @return true: focus success
     */
    public boolean startFocus() {
        Log.d(TAG, "startFocus");
        if (null != mAudioManager && mAudioFocus != AUDIO_FOCUSED) {
            int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = AUDIO_FOCUSED;
            }
        }
        return mAudioFocus == AUDIO_FOCUSED;
    }

    /**
     * give up AudioFocus，播放完成调用
     * @return true: stop success
     */
    public boolean stopFocus() {
        Log.d(TAG, "stopFocus");
        if (null != mAudioManager && mAudioFocus == AUDIO_FOCUSED) {
            if (mAudioManager.abandonAudioFocus(mAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
        return mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK;
    }

    public void release() {
        mAudioManager = null;
        instance = null;
    }



}
