package com.gmail.aaron.camerarecord.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaronsmith on 24/10/2017.
 */

public class PermissionsUtil {

    public PermissionsListener permissionsListener;
    private Activity mActivity;
    private List<String> permissionList;
    private boolean isFinish = false;//是否结束程序，主要是启动时拒接权限时使用
    private static PermissionsUtil permissionsUtil;

    private PermissionsUtil() {
    }

    public static PermissionsUtil getInstance() {
        synchronized (PermissionsUtil.class) {
            if (permissionsUtil == null) {
                permissionsUtil = new PermissionsUtil();
            }
        }
        return permissionsUtil;
    }

    public void checkPermissions(Activity activity, final PermissionsListener permissionsListener, final String... permission) {
        this.permissionsListener = permissionsListener;
        mActivity = activity;
        for (int i = 0; i < permission.length; i++) {
            if (permissionList == null) {
                permissionList = new ArrayList<>();
            }
            permissionList.add(permission[i]);
        }
        Dexter.withActivity(activity)
                .withPermissions(permission).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
//                for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
//                    Log.e("response", "同意权限");
//                    permissionsListener.agreePermission();
//
//                }

                for (int i = 0; i < report.getGrantedPermissionResponses().size(); i++) {
                    if ((i + 1) == report.getGrantedPermissionResponses().size()) {//多个权限的时候，全部选择完毕再继续
                        permissionsListener.agreePermission();
                    }
                }

                for (int i = 0; i < report.getDeniedPermissionResponses().size(); i++) {
                    if ((i + 1) == report.getDeniedPermissionResponses().size()) {//多个权限的时候，全部选择完毕再继续
                        permissionsListener.disagreePermission();
                        showForbidPermissionDialog("\n在设置>应用>斑马邦>权限中开启，以正常使用该功能。");
                    }
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }

        }).check();
    }

    public interface PermissionsListener {
        void agreePermission();

        void disagreePermission();
    }


    private void showForbidPermissionDialog(String message) {
        if (permissionList.size() > 0) {
//            for (int i = 0; i < permissionList.size(); i++) {
//                if (permissionList.get(i).equals(Manifest.permission.READ_PHONE_STATE)) {
//                    isFinish = true;
//                    break;
//                }
//            }
        }
        if (mActivity != null) {
            new AlertDialog.Builder(mActivity).setMessage(message)
                    .setTitle("申请权限")
                    .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isFinish) {
                                mActivity.finish();
                            }
                        }
                    })
                    .setNegativeButton(null, null)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (isFinish) {
                                mActivity.finish();
                            }
                        }
                    })
                    .create()
                    .show();
        }
    }
}
