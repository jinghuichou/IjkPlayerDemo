package com.wehaicao.ijkplayerdemo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity implements VideoListener {

    private static final String TAG = "MainActivity";
    private static final int SIZE_DEFAULT = 0;
    private static final int SIZE_4_3 = 1;
    private static final int SIZE_16_9 = 2;
    private int currentSize = SIZE_16_9;

    private CusIjkPlayer player;
    private SeekBar seekBar;
    private TextView tvTime;
    private TextView tvTotalTime;
    private TextView tvScale;
    private RelativeLayout rlPlayer;
    private TextView tvPlayStatus;
    private TextView tvPreTime;
    private ProgressBar progressVolume;
    private ProgressBar progressLight;
    private ProgressBar progressLoading;

    private boolean keepTrue;
    private int screenHeight;
    private int screenWidth;
    private int screenHalfWidth;
    private boolean portrait;
    private boolean sensor_flag = true;
    private boolean stretch_flag = true;
    private long totalDuration;
    private long currentDuration;
    private SensorManager sm;
    private OrientationSensorListener listener;
    private Sensor sensor;
    private SensorManager sm1;
    private OrientationSensorListener2 listener1;
    private Sensor sensor1;
    private WeakHandler handler = new WeakHandler(this);

    /**
     * 播放比例
     */
    @IntDef(value = {SIZE_DEFAULT, SIZE_4_3, SIZE_16_9})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SIZE {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById();

        initViewListener();

        initSenorListener();

        initScreenInfo();

        setScreenRate(currentSize);

        initPlayer();
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        player.setVideoListener(this);
        player.setPath("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        try {
            player.load();
        } catch (IOException e) {
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    float startX = 0;
    float startY = 0;
    float endX = 0;
    float endY = 0;

    /**
     * 初始化控件监听
     */
    private void initViewListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvTime.setText(TimeUtils.getHms(progress / 1000, false));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
            }
        });

        tvScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullChangeScreen();
            }
        });

        tvPlayStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                        tvPlayStatus.setText("播放");
                    } else {
                        player.resume();
                        tvPlayStatus.setText("暂停");
                    }
                }
            }
        });

        player.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    startX = event.getX();
                    startY = event.getY();
                    currentDuration = player.getCurrentDuration();
                    progressVolume.setSecondaryProgress(progressVolume.getProgress());
                    progressLight.setSecondaryProgress(progressLight.getProgress());
                    return true;
                }
                if (MotionEvent.ACTION_MOVE == event.getAction()) {
                    endX = event.getX();
                    endY = event.getY();
                    setMoveVolume();
                    setMoveTime();
                    setMoveLight();
                    return true;
                }
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    tvPreTime.setVisibility(View.GONE);
                    progressVolume.setVisibility(View.GONE);
                    progressLight.setVisibility(View.GONE);
                    initScreenInfo();
                    endX = event.getX();
                    endY = event.getY();
                    setMoveProgress();
                    progressVolume.setSecondaryProgress(progressVolume.getProgress());
                    progressLight.setSecondaryProgress(progressLight.getProgress());
                    return false;
                }
                return false;
            }
        });
    }

    private void setMoveVolume() {
        if (!portrait) {
            if (startX > screenHalfWidth && 50 < startY - endY) {
                progressVolume.setVisibility(View.VISIBLE);
                int dis = (int) ((startY - endY) / 10);
                progressVolume.setProgress(progressVolume.getSecondaryProgress() + dis > progressVolume.getMax() ? progressVolume.getMax() : progressVolume.getSecondaryProgress() + dis);
                int progress = progressVolume.getProgress();
                player.setVolume(progress);
            } else if (startX > screenHalfWidth && 50 < endY - startY) {
                int dis = (int) (endY - startY) / 10;
                progressVolume.setVisibility(View.VISIBLE);
                progressVolume.setProgress(progressVolume.getSecondaryProgress() - dis < 0 ? 0 : progressVolume.getSecondaryProgress() - dis);
                int progress = progressVolume.getProgress();
                player.setVolume(progress);
            }
        }
    }


    private void setMoveProgress() {
        if (Math.abs(endX - startX) > 100 && 50 < Math.abs(startY - endY)) {
            // 快进
            int second = (int) (Math.abs(endX - startX) / 50);
            if (endX > startX) {
                seekBar.setProgress(seekBar.getProgress() + second * 1000);
            } else {
                seekBar.setProgress(seekBar.getProgress() - second * 1000);
            }
            player.seekTo(seekBar.getProgress());
        }
    }

    private void setMoveTime() {
        if (Math.abs(endX - startX) > 100 && 50 < Math.abs(startY - endY)) {
            tvPreTime.setVisibility(View.VISIBLE);
            // 计算快进快退多少秒
            int second = (int) (Math.abs(endX - startX) / 50) * 1000;
            long progress;
            // 快进
            if (endX > startX) {
                progress = second + currentDuration;
            } else {
                progress = currentDuration - second;
            }
            if (progress <= totalDuration && progress > 0) {
                String s = TimeUtils.getHms(progress / 1000, false) + "/" + TimeUtils.getHms(totalDuration / 1000, false);
                tvPreTime.setText(s);
            }
        }
    }

    private void setMoveLight() {
        if (!portrait) {
            if (startX < screenHalfWidth && 50 < startY - endY) {
                progressLight.setVisibility(View.VISIBLE);
                int dis = (int) ((startY - endY) / 10);
                progressLight.setProgress(progressLight.getSecondaryProgress() + dis > progressLight.getMax() ? progressLight.getMax() : progressLight.getSecondaryProgress() + dis);
                int progress = progressLight.getProgress();
                changeAppBrightness(progress);
            } else if (startX < screenHalfWidth && 50 < endY - startY) {
                int dis = (int) (endY - startY) / 10;
                progressLight.setVisibility(View.VISIBLE);
                progressLight.setProgress(progressLight.getSecondaryProgress() - dis < 0 ? 0 : progressLight.getSecondaryProgress() - dis);
                int progress = progressLight.getProgress();
                changeAppBrightness(progress);
            }
        }
    }

    /**
     * 获得系统亮度
     */
    private int getSystemBrightness() {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    private void changeAppBrightness(int brightness) {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }


    private void findViewById() {
        player = findViewById(R.id.player);
        seekBar = findViewById(R.id.seekBar);
        tvTime = findViewById(R.id.tv_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        tvScale = findViewById(R.id.tv_scale);
        rlPlayer = findViewById(R.id.rl_player);
        tvPlayStatus = findViewById(R.id.tv_play_status);
        tvPreTime = findViewById(R.id.tv_pre_time);
        progressLight = findViewById(R.id.progress_light);
        progressVolume = findViewById(R.id.progress_volume);
        progressLoading = findViewById(R.id.progress_loading);
    }

    private void initSenorListener() {
        // 注册重力感应器  屏幕旋转
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new OrientationSensorListener(handler);
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);

        // 根据  旋转之后 点击 符合之后 激活sm
        sm1 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor1 = sm1.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener1 = new OrientationSensorListener2();
        sm1.registerListener(listener1, sensor1, SensorManager.SENSOR_DELAY_UI);

    }


    static class WeakHandler extends Handler {

        private static final int MSG_LISTENER = 10001;

        WeakReference<MainActivity> weakReference;

        public WeakHandler(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = weakReference.get();
            switch (msg.what) {
                case 888:
                    int orientation = msg.arg1;
                    if (orientation > 45 && orientation < 135) {

                    } else if (orientation > 135 && orientation < 225) {

                    } else if (orientation > 225 && orientation < 315) {
                        System.out.println("切换成横屏");
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        activity.sensor_flag = false;
                        activity.stretch_flag = false;

                    } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                        System.out.println("切换成竖屏");
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        activity.sensor_flag = true;
                        activity.stretch_flag = true;

                    }
                    break;
                case 100:
                    if (activity.keepTrue) {
                        Bundle data = msg.getData();
                        int duration = data.getInt("duration");
                        int currentPosition = data.getInt("currentPosition");

                        activity.seekBar.setMax(duration);
                        activity.seekBar.setProgress(currentPosition);
                        activity.tvTime.setText(TimeUtils.getHms(currentPosition / 1000, false));
                    }
                    break;
                case MSG_LISTENER:

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 更新SeekBar
     */
    private void updateSeekBar() {
        //获取总时长
        final int duration = (int) player.getDuration();

        //开启线程发送数据
        new Thread() {
            @Override
            public void run() {
                while (keepTrue) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int currentPosition = (int) player.getCurrentDuration();

                    //发送数据给activity
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPosition);
                    message.setData(bundle);
                    message.what = 100;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }


    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
        seekBar.setSecondaryProgress(i);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        keepTrue = false;
        seekBar.setProgress(seekBar.getMax());
        tvTime.setText(TimeUtils.getHms(seekBar.getMax() / 1000, false));
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        Log.e(TAG, "onError");
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                progressLoading.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                progressLoading.setVisibility(View.INVISIBLE);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        player.start();
        seekBar.setMax((int) player.getDuration());
        progressVolume.setProgress(player.getVolume());
        progressVolume.setMax(player.getMaxVolume());
        progressLight.setProgress(getSystemBrightness());
        progressLight.setMax(255);
        totalDuration = player.getDuration();
        tvTotalTime.setText(TimeUtils.getHms(player.getDuration() / 1000, false));
        keepTrue = true;
        updateSeekBar();
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新获取屏幕宽高
        initScreenInfo();
        // 切换为横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams lp = rlPlayer.getLayoutParams();
            lp.height = screenHeight;
            lp.width = screenWidth;
            rlPlayer.setLayoutParams(lp);
        } else {
            ViewGroup.LayoutParams lp = rlPlayer.getLayoutParams();
            lp.height = screenWidth * 9 / 16;
            lp.width = screenWidth;
            rlPlayer.setLayoutParams(lp);
        }
        setScreenRate(currentSize);
        portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        tvScale.setText(!portrait ? "退出全屏" : "全屏");
        tryFullScreen(!portrait);
    }

    /**
     * 设置播放比例
     *
     * @param rate {@link SIZE}
     */
    public void setScreenRate(@SIZE int rate) {
        int width = 0;
        int height = 0;
        // 横屏
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (rate == SIZE_DEFAULT) {
                width = player.getWidth();
                height = player.getHeight();
            } else if (rate == SIZE_4_3) {
                width = screenHeight / 3 * 4;
                height = screenHeight;
            } else if (rate == SIZE_16_9) {
                width = screenHeight / 9 * 16;
                height = screenHeight;
            }
        } else {
            //竖屏
            if (rate == SIZE_DEFAULT) {
                width = player.getWidth();
                height = player.getHeight();
            } else if (rate == SIZE_4_3) {
                width = screenWidth;
                height = screenWidth * 3 / 4;
            } else if (rate == SIZE_16_9) {
                width = screenWidth;
                height = screenWidth * 9 / 16;
            }
        }
        if (width > 0 && height > 0) {
            ViewGroup.LayoutParams lp = rlPlayer.getLayoutParams();
            lp.width = width;
            lp.height = height;
            rlPlayer.getRootView().setLayoutParams(lp);
        }
    }

    /**
     * 获取屏幕宽高
     */
    private void initScreenInfo() {
        Point outSize = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            getWindowManager().getDefaultDisplay().getSize(outSize);
        } else {
            Display display = getWindowManager().getDefaultDisplay();
            display.getSize(outSize);
        }
        screenWidth = outSize.x;
        screenHalfWidth = screenWidth / 2;
        screenHeight = outSize.y;
    }

    /**
     * 切换横竖屏
     */
    private void fullChangeScreen() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * 设置是否全屏是否隐藏ActionBar
     *
     * @param fullScreen true隐藏
     */
    private void tryFullScreen(boolean fullScreen) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (fullScreen) {
                supportActionBar.hide();
            } else {
                supportActionBar.show();
            }
        }
        setFullScreen(fullScreen);
    }

    /**
     * 设置是否全屏
     *
     * @param fullScreen true全屏
     */
    private void setFullScreen(boolean fullScreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullScreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 重力感应监听者
     */
    public class OrientationSensorListener implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;
        private static final int ORIENTATION_UNKNOWN = -1;

        private Handler rotateHandler;

        private OrientationSensorListener(Handler handler) {
            rotateHandler = handler;
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }

        public void onSensorChanged(SensorEvent event) {
            //只有两个不相同才开始监听行为
            if (sensor_flag != stretch_flag) {
                float[] values = event.values;
                int orientation = ORIENTATION_UNKNOWN;
                float X = -values[_DATA_X];
                float Y = -values[_DATA_Y];
                float Z = -values[_DATA_Z];
                float magnitude = X * X + Y * Y;
                // Don't trust the angle if the magnitude is small compared to the y value
                if (magnitude * 4 >= Z * Z) {
                    //屏幕旋转时
                    float OneEightyOverPi = 57.29577957855f;
                    float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                    orientation = 90 - (int) Math.round(angle);
                    // normalize to 0 - 359 range
                    while (orientation >= 360) {
                        orientation -= 360;
                    }
                    while (orientation < 0) {
                        orientation += 360;
                    }
                }
                int screenchange = 0;
                try {
                    // 获取屏幕旋转的开关
                    screenchange = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                if (rotateHandler != null && screenchange == 1) {
                    rotateHandler.obtainMessage(888, orientation, 0).sendToTarget();
                }
            }
        }
    }


    public class OrientationSensorListener2 implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;
        private static final int ORIENTATION_UNKNOWN = -1;

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }

        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];
            //这一段据说是 android源码里面拿出来的计算 屏幕旋转的 不懂 先留着 万一以后懂了呢
            float magnitude = X * X + Y * Y;
            // Don't trust the angle if the magnitude is small compared to the y value
            if (magnitude * 4 >= Z * Z) {
                //屏幕旋转时
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int) Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }
            if (orientation > 225 && orientation < 315) {  //横屏
                sensor_flag = false;
            } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {  //竖屏
                sensor_flag = true;
            }
            if (stretch_flag == sensor_flag) {  //点击变成横屏  屏幕 也转横屏 激活
                System.out.println("激活");
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
