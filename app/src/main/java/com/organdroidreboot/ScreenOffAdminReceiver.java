package com.organdroidreboot;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @Description:
 * @Author: miao
 * @CreateDate: 2020-03-31 16:38
 * @UpdateUser:
 * @TODO:
 */
public class ScreenOffAdminReceiver extends DeviceAdminReceiver {

    private void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "设备管理器权限可用");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "设备管理器不可使用");
    }
}
