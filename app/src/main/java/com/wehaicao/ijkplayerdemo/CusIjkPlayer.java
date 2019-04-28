package com.wehaicao.ijkplayerdemo;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class CusIjkPlayer extends FrameLayout {

    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private IMediaPlayer mMedialPlayer = null;
    /**
     * 视频文件地址
     */
    private String mPath;
    /**
     * 视频请求header
     */
    private Map<String, String> mHeader;
    private SurfaceView mSurfaceView;
    private Context mContext;
    private boolean mEnableMediaCodec;
    private AudioManager mAudioManager;
    private AudioFocusHelper mAudioFousHelper;
    private VideoListener mListener;

    public CusIjkPlayer(Context context) {
        this(context, null);
    }

    public CusIjkPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CusIjkPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setBackgroundColor(Color.BLACK);
        createSurfaceView();
        mAudioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioFousHelper = AudioFocusHelper.getInstance(mContext);
    }

    private void createSurfaceView() {
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMedialPlayer != null) {
                    mMedialPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(mSurfaceView, 0, layoutParams);
    }

    private IMediaPlayer createPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "skip_loop_filter", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "http-detect-range-support", 48);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "min-frames", 100);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        ijkMediaPlayer.setVolume(1.0f, 1.0f);
        setEnableMediaCodec(ijkMediaPlayer, mEnableMediaCodec);
        return ijkMediaPlayer;
    }

    /**
     * 设置是否开启硬解码
     */
    private void setEnableMediaCodec(IjkMediaPlayer ijkMediaPlayer, boolean isEnable) {
        int value = isEnable ? 1 : 0;
        // 开启硬解码
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", value);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", value);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", value);
    }

    public void setEnableMediaCodec(boolean mEnableMediaCodec) {
        this.mEnableMediaCodec = mEnableMediaCodec;
    }

    private void setListener(IMediaPlayer player) {
        if (mListener != null) {
            player.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
                    mListener.onBufferingUpdate(iMediaPlayer, i);
                }
            });
            player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
                    mListener.onCompletion(iMediaPlayer);
                }
            });
            player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                    return mListener.onError(iMediaPlayer, i, i1);
                }
            });
            player.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                    return mListener.onInfo(iMediaPlayer, i, i1);
                }
            });
            player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    mListener.onPrepared(iMediaPlayer);
                }
            });
            player.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
                    mListener.onVideoSizeChanged(iMediaPlayer, i, i1, i2, i3);
                }
            });
            player.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                    mListener.onSeekComplete(iMediaPlayer);
                }
            });
        }
    }

    public void setVideoListener(final VideoListener videoListener) {
        mListener = videoListener;
    }

    public void setPath(String path) {
        setPath(path, null);
    }

    public void setPath(String path, Map<String, String> header) {
        mPath = path;
        mHeader = header;
    }

    public void load() throws IOException {
        if (mMedialPlayer != null) {
            mMedialPlayer.stop();
            mMedialPlayer.release();
        }
        mMedialPlayer = createPlayer();
        setListener(mMedialPlayer);
        mMedialPlayer.setDisplay(mSurfaceView.getHolder());
        mMedialPlayer.setDataSource(mContext, Uri.parse(mPath), mHeader);
        mMedialPlayer.prepareAsync();
    }

    public void start() {
        if (mMedialPlayer != null) {
            mMedialPlayer.start();
            mAudioFousHelper.startFocus();
        }
    }

    public void release() {
        if (mMedialPlayer != null) {
            mMedialPlayer.reset();
            mMedialPlayer.release();
            mMedialPlayer = null;
            mAudioFousHelper.stopFocus();
        }
    }

    public void pause() {
        if (mMedialPlayer != null) {
            mMedialPlayer.pause();
        }
    }

    public void stop() {
        if (mMedialPlayer != null) {
            mMedialPlayer.stop();
            mAudioFousHelper.stopFocus();
        }
    }

    public void resume() {
        if (mMedialPlayer != null) {
            mMedialPlayer.start();
        }
    }

    public void reset() {
        if (mMedialPlayer != null) {
            mMedialPlayer.reset();
            mAudioFousHelper.stopFocus();
        }
    }

    public long getDuration() {
        if (mMedialPlayer != null) {
            return mMedialPlayer.getDuration();
        } else {
            return 0;
        }
    }

    public void seekTo(long l) {
        if (mMedialPlayer != null) {
            mMedialPlayer.seekTo(l);
        }
    }

    public boolean isPlaying() {
        if (mMedialPlayer != null) {
            return mMedialPlayer.isPlaying();
        }
        return false;
    }

    public long getCurrentDuration() {
        if (mMedialPlayer != null) {
            return mMedialPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume / 3, AudioManager.FLAG_PLAY_SOUND);
    }

    public int getVolume() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentVolume * 3;
    }

    public int getMaxVolume() {
        int currentVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return currentVolume * 3;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
