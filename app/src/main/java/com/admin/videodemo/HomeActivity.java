package com.admin.videodemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.tv_media_recorder, R.id.tv_ffmpeg_record_compress_video, R.id.tv_ffmpeg_record_video, R.id.tv_ffmpeg_compress_video})
    public void onViewClicked(View view) {
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
                intent = new Intent(HomeActivity.this, ActFFMpegVideoRecordCompress.class);
                startActivity(intent);
                break;
        }
    }
}
