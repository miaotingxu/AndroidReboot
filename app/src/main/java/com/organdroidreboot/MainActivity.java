package com.organdroidreboot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private AudioManager mAudioManager;
    private TextView tvIncreaseVolume;


    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private DevicePolicyManager policyManager;
    private ComponentName adminReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvIncreaseVolume = findViewById(R.id.tv_increase_volume);

        adminReceiver = new ComponentName(MainActivity.this, ScreenOffAdminReceiver.class);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        policyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int value = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        Log.w("-----------最大音量", max + " max ;  当前音量  : " + value);

    }

    public void reboot1(View view) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                rebootDevices();
            }
        }.start();
    }

    private void rebootDevices() {
        String cmd = "su -c reboot";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error! Fail to reboot.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 增大音量
     *
     * @param view
     */
    public void increaseVolume(View view) {
        int value = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        value++;
        if (value >= maxVolume) {
            value = maxVolume;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, value,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        tvIncreaseVolume.setText(" 增大音量 (当前音量 : " + streamVolume + ")");
    }


    /**
     * 获取屏幕的亮度
     *
     * @param activity
     * @return
     */
    public static int getScreenBrightness(Activity activity) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    public void decreaseVolume(View view) {
        int value = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        value--;
        if (value <= 0) {
            value = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, value,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        tvIncreaseVolume.setText(" 减小音量 (当前音量 : " + streamVolume + ")");
    }

    public void increaseBridget(View view) {
        if (requestWriteSettings()) {
            int screenBrightness = getScreenBrightness(this);
            screenBrightness += 10;
            setBrightness(this, screenBrightness);
            int screenBrightness1 = getScreenBrightness(this);
            Log.w("-------", "改变前亮度 ： " + screenBrightness + " ;   改变后 ： " + screenBrightness1);
        }
    }

    /**
     * 设置亮度等有用到
     */
    private boolean requestWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //大于等于23 请求权限
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 12);
                return false;
            } else {
                return true;
            }
        } else {//小于23直接设置
            return true;
        }
    }

    /**
     * 设置亮度
     *
     * @param activity
     * @param brightness
     */
    public static void setBrightness(Activity activity, int brightness) {
        stopAutoBrightness(activity);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        if (brightness <= 0) {
            brightness = 0;
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            if (brightness >= 255) {
                brightness = 255;
            }
            lp.screenBrightness = brightness / 255f;
        }
        activity.getWindow().setAttributes(lp);
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    /**
     * 停止自动亮度调节
     *
     * @param activity
     */
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    public void decreaseBridget(View view) {
        if (requestWriteSettings()) {
            int screenBrightness = getScreenBrightness(this);
            screenBrightness -= 10;
            setBrightness(this, screenBrightness);
            int screenBrightness1 = getScreenBrightness(this);
            Log.w("-------", " 改变前亮度 ： " + screenBrightness + " ;   减小后 ： " + screenBrightness1);
        }
    }

    public void checkScreenOnOff(View view) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {//如果灭屏
            //相关操作
            showToast("屏幕是息屏");
        } else {
            showToast("屏幕是亮屏");
        }
    }

    /**
     * 如果是系统应用，可用代码打开
     */
    private void openDeviceAdmin() {
        ComponentName componentName = new ComponentName(getPackageName(),
                "com.organdroidreboot.ScreenOffAdminReceiver");
        try {
            DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            Method setActiveAdmin = mDPM.getClass().getDeclaredMethod("setActiveAdmin", ComponentName.class, boolean.class);
            setActiveAdmin.setAccessible(true);
            setActiveAdmin.invoke(mDPM, componentName, true);
        } catch (Exception e) {
        }
    }

    private void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("InvalidWakeLockTag")
    public void checkScreenOn(View view) {
        mWakeLock = mPowerManager.
                newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        mWakeLock.acquire();
        mWakeLock.release();
    }

    public void checkScreenOff(View view) {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
        } else {
            showToast("没有设备管理器权限");
        }
    }

    public void requestDeviceAdmin(View view) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "开启后就可以使用更多功能了...");//显示位置见图二
        startActivityForResult(intent, 0);
    }

    public void reboot2(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.REBOOT) != PackageManager.PERMISSION_GRANTED) {
            Log.w("--------", " 没有权限  ");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.REBOOT
            }, 12);
        } else {
            Log.w("--------", " 有权限   直接重启");
            // 重启
            PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            pManager.reboot("重启");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 12) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 重启
                PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                pManager.reboot("重启");
            } else {
                Log.w("-------", " ----  申请权限失败 ");
            }
        }

    }
}
