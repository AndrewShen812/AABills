package com.shenyong.aabills.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.PixelCopy;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sddy.baseui.BaseActivity;
import com.shenyong.aabills.R;
import com.shenyong.aabills.UserManager;
import com.shenyong.aabills.ui.user.UserLoginActivity;
import com.shenyong.aabills.utils.AppUtils;

public class StartupActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        showFullScreen();
        UserManager.INSTANCE.autoLogin();
        TextView tvVersion = findViewById(R.id.tv_start_up_version);
        tvVersion.setText(getString(R.string.fmt_version, AppUtils.getVersionName(), AppUtils.getVersionCode()));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
            } else {
                showMain();
            }
        } else {
            showMain();
        }
    }

    private void showMain() {
//        View root = findViewById(R.id.cl_start_up);
//        Bitmap image = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.RGB_565);
//        int[] pos = new int[2];
//        root.getLocationInWindow(pos);
//        Rect srcRect = new Rect(pos[0], pos[1], pos[0] + root.getWidth(), pos[1] + root.getHeight());
//        PixelCopy.request(getWindow(), srcRect, image, {result ->
//        if (result != PixelCopy.SUCCESS) {
//            //                            Toast.makeText(this@MainActivity, "保存截图失败", Toast.LENGTH_SHORT).show()
//            saveUnderAndroidO(root)
//            return@request
//        }
//        val dir = cacheDir()
//        val fullName = "${dir.path}${File.separator}share_img.jpg"
//
//        val fos = FileOutputStream(fullName)
//        image.compress(Bitmap.CompressFormat.JPEG, 100, fos)
//        fos.flush()
//        fos.close()
//        Toast.makeText(this@MainActivity, "已保存截图", Toast.LENGTH_SHORT).show()
//                    }, Handler(Looper.getMainLooper()))

        findViewById(R.id.ll_start_up_name).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(MainActivity.class);
                finish();
            }
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        showMain();
    }
}
