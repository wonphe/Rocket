package top.ox16.rocket;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import top.ox16.rocket.utils.ConstantValue;
import top.ox16.rocket.utils.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {

    private WindowManager mWM;
    private Point mScreenSize;
    private View mRocketView;
    private Handler mHandler = new MyHandler(this);
    private WindowManager.LayoutParams mParams;
    private ImageView iv_top;
    private ImageView iv_bottom;
    private SoundPool soundPool;
    private int fireSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenSize = new Point();
        mWM.getDefaultDisplay().getSize(mScreenSize);
        iv_top = (ImageView) findViewById(R.id.iv_top);
        iv_bottom = (ImageView) findViewById(R.id.iv_bottom);
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        fireSound = soundPool.load(this, R.raw.fire, 1);
        // 开启火箭
        showRocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWM != null && mRocketView != null) {
            mWM.removeView(mRocketView);
        }
    }

    /**
     * 开启火箭
     */
    private void showRocket() {
        mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mParams.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT >= 26) {//8.0新特性
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        mParams.setTitle("Toast");
        // 指定Toast的指定位置
        mParams.gravity = Gravity.TOP + Gravity.START;
        // Toast显示效果（布局文件）
        mRocketView = View.inflate(getApplicationContext(), R.layout.rocket_view, null);
        ImageView iv_rocket = (ImageView) mRocketView.findViewById(R.id.iv_rocket);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_rocket.getBackground();
        animationDrawable.start();
        // 根据SharedPreferences中存储的位置更新Toast的x,y值
        mParams.x = SharedPreferencesUtils.getInt(getApplicationContext(),
                ConstantValue.KEY_ROCKET_POSITION_X, 0);
        mParams.y = SharedPreferencesUtils.getInt(getApplicationContext(),
                ConstantValue.KEY_ROCKET_POSITION_Y, 0);
        mWM.addView(mRocketView, mParams);

        mRocketView.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        // 获取偏移位置
                        int offsetX = moveX - startX;
                        int offsetY = moveY - startY;
                        // 更新其实位置
                        mParams.x = mParams.x + offsetX;
                        mParams.y = mParams.y + offsetY;
                        // 容错处理
                        if (mParams.x < 0) {
                            mParams.x = 0;
                        }
                        if (mParams.y < 0) {
                            mParams.y = 0;
                        }
                        if (mParams.x > mScreenSize.x - mRocketView.getWidth()) {
                            mParams.x = mScreenSize.x - mRocketView.getWidth();
                        }
                        if (mParams.y > mScreenSize.y - mRocketView.getHeight() - getStatusBarHeight()) {
                            mParams.y = mScreenSize.y - mRocketView.getHeight() - getStatusBarHeight();
                        }
                        // 重置Toast位置
                        mWM.updateViewLayout(mRocketView, mParams);
                        // 重置起点位置
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        SharedPreferencesUtils.putInt(getApplicationContext(),
                                ConstantValue.KEY_ROCKET_POSITION_X, mParams.x);
                        SharedPreferencesUtils.putInt(getApplicationContext(),
                                ConstantValue.KEY_ROCKET_POSITION_Y, mParams.y);
                        if (mParams.x > mScreenSize.x / 2 - mRocketView.getWidth() &&
                                mParams.x < mScreenSize.x / 2 &&
                                mParams.y > mScreenSize.y - mRocketView.getHeight() * 15 / 10) {
                            // 发射火箭
                            sendRocket();
                            // 开启产生尾气的动画
                            showSmoke();
                            // 播放音效
                            playSound();
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 获状态栏高度
     *
     * @return 状态栏高度
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 发射火箭
     */
    private void sendRocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //向上的移动过程中，一直减少Y轴的大小，直到减少到0为止
                int totalHeight = mScreenSize.y - mRocketView.getHeight() * 15 / 10;
                int step = totalHeight / 10;
                for (int i = 0; i < 11; i++) {
                    int height = totalHeight - i * step < 0 ? 0 : totalHeight - i * step;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = height;
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 烟雾特效
     */
    private void showSmoke() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = 2;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 烟雾特效
     */
    private void playSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = 3;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private static class MyHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity service) {
            mActivity = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mActivity.get();
            switch (msg.what) {
                case 1:
                    activity.mParams.y = (int) msg.obj;
                    activity.mParams.x = activity.mScreenSize.x / 2 - activity.mRocketView.getWidth() / 2;
                    activity.mWM.updateViewLayout(activity.mRocketView, activity.mParams);
                    SharedPreferencesUtils.putInt(activity.getApplicationContext(),
                            ConstantValue.KEY_ROCKET_POSITION_Y, activity.mParams.y);
                    break;
                case 2:
                    activity.iv_top.setVisibility(View.VISIBLE);
                    activity.iv_bottom.setVisibility(View.VISIBLE);
                    activity.iv_top.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.top));
                    activity.iv_bottom.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.bottom));
                    activity.iv_top.setVisibility(View.INVISIBLE);
                    activity.iv_bottom.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    activity.soundPool.play(activity.fireSound, 1.0f, 1.0f, 0, 0, 1.0f);
                    break;
            }
        }
    }
}
