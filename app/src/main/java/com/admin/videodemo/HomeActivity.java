package com.admin.videodemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.tv_media_recorder)
    TextView tvMediaRecorder;
    @BindView(R.id.tv_ffmpeg_record_video)
    TextView tvFfmpegRecordVideo;
    @BindView(R.id.tv_ffmpeg_compress_video)
    TextView tvFfmpegCompressVideo;
    @BindView(R.id.tv_ffmpeg_record_compress_video)
    TextView tvFfmpegRecordCompressVideo;

    private final int PERMISSION_REQUEST_CODE = 0x001;
    private static final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean isPermissionGranded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        permissionCheck();
    }

    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean permissionState = true;
            for (String permission : permissionManifest) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionState = false;
                }
            }
            if (!permissionState) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
            } else {
                isPermissionGranded = true;
            }
        } else {
            isPermissionGranded = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (Manifest.permission.CAMERA.equals(permissions[i])) {
                        isPermissionGranded = true;
                    } else if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {

                    }
                }
            }
        }
    }

    @OnClick({R.id.tv_media_recorder, R.id.tv_ffmpeg_record_compress_video, R.id.tv_ffmpeg_record_video, R.id.tv_ffmpeg_compress_video})
    public void onViewClicked(View view) {
//        if(!isPermissionGranded){
//            Toast.makeText(HomeActivity.this,"请授权！！！",Toast.LENGTH_LONG).show();
//            return;
//        }
        Intent intent = null;
        switch (view.getId()) {
            case R.id.tv_media_recorder:
                intent = new Intent(HomeActivity.this, ActMediaRecorder.class);
                startActivity(intent);
                break;
            case R.id.tv_ffmpeg_record_compress_video:
                intent = new Intent(HomeActivity.this, ActFFMpegVideoRecordCompress.class);
                startActivity(intent);
                break;
            case R.id.tv_ffmpeg_record_video:
                intent = new Intent(HomeActivity.this, ActFFMpegRecordVideo.class);
                startActivity(intent);
                break;
            case R.id.tv_ffmpeg_compress_video:
                intent = new Intent(HomeActivity.this, ActFFMpegCompressVideo.class);
                startActivity(intent);
                break;
        }
    }
}
