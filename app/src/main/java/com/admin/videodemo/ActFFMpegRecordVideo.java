package com.admin.videodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.hardware.Camera;

import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mabeijianxi.smallvideorecord2.JianXiCamera;
import com.mabeijianxi.smallvideorecord2.MediaRecorderActivity;
import com.mabeijianxi.smallvideorecord2.model.AutoVBRMode;
import com.mabeijianxi.smallvideorecord2.model.BaseMediaBitrateConfig;
import com.mabeijianxi.smallvideorecord2.model.MediaRecorderConfig;

import java.io.File;
import java.util.List;

/**
 * 使用FFMpeg实现视频录制
 */
public class ActFFMpegRecordVideo extends AppCompatActivity{

    private Button bt_start;
    private TextView tv_size;
    private EditText et_width;
    private EditText et_height;
    private EditText et_maxtime;
    private Spinner spinner_record;
    private EditText et_maxframerate;
    private ProgressDialog mProgressDialog;
    private EditText et_bitrate;

    private EditText et_mintime;
    private Spinner spinner_need_full;
    private static File recordDir;//录制文件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_ffmpeg_record_video);
        initSmallVideo();
        initView();
        initEvent();
        setSupportCameraSize();
    }

    private void setSupportCameraSize() {
        Camera back = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        List<Camera.Size> backSizeList = back.getParameters().getSupportedPreviewSizes();
        StringBuilder str = new StringBuilder();
        str.append("经过检查您的摄像头，如使用后置摄像头您可以输入的高度有：");
        for (Camera.Size bSize : backSizeList) {
            str.append(bSize.height + "、");
        }
        back.release();
        Camera front = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        List<Camera.Size> frontSizeList = front.getParameters().getSupportedPreviewSizes();
        str.append("如使用前置摄像头您可以输入的高度有：");
        for (Camera.Size fSize : frontSizeList) {
            str.append(fSize.height + "、");
        }
        front.release();
        tv_size.setText(str);
    }

    private void initEvent() {
        spinner_need_full.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( ((TextView)view).getText().toString().equals("false")){
                    et_width.setVisibility(View.VISIBLE);
                }else {
                    et_width.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initView() {
        tv_size = (TextView) findViewById(R.id.tv_size);
        et_width = (EditText) findViewById(R.id.et_width);
        et_height = (EditText) findViewById(R.id.et_height);
        et_maxframerate = (EditText) findViewById(R.id.et_maxframerate);
        et_bitrate = (EditText) findViewById(R.id.et_record_bitrate);
        et_maxtime = (EditText) findViewById(R.id.et_maxtime);
        et_mintime = (EditText) findViewById(R.id.et_mintime);
        spinner_record = (Spinner) findViewById(R.id.spinner_record);
        spinner_need_full = (Spinner) findViewById(R.id.spinner_need_full);
        bt_start = (Button) findViewById(R.id.bt_start);
    }

    public void go(View c) {
        String width = et_width.getText().toString();
        String height = et_height.getText().toString();
        String maxFramerate = et_maxframerate.getText().toString();
        String bitrate = et_bitrate.getText().toString();
        String maxTime = et_maxtime.getText().toString();
        String minTime = et_mintime.getText().toString();
        String s = spinner_need_full.getSelectedItem().toString();
        boolean needFull = Boolean.parseBoolean(s);

        BaseMediaBitrateConfig recordMode;
        BaseMediaBitrateConfig compressMode = null;

        recordMode = new AutoVBRMode();

        if (!spinner_record.getSelectedItem().toString().equals("none")) {
            recordMode.setVelocity(spinner_record.getSelectedItem().toString());
        }

        if(!needFull&&checkStrEmpty(width, "请输入宽度")){
            return;
        }
        if (
                checkStrEmpty(height, "请输入高度")
                        || checkStrEmpty(maxFramerate, "请输入最高帧率")
                        || checkStrEmpty(maxTime, "请输入最大录制时间")
                        || checkStrEmpty(minTime, "请输小最大录制时间")
                        || checkStrEmpty(bitrate, "请输入比特率")
                ) {
            return;
        }

        MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                .fullScreen(needFull)
                .smallVideoWidth(needFull?0:Integer.valueOf(width))
                .smallVideoHeight(Integer.valueOf(height))
                .recordTimeMax(Integer.valueOf(maxTime))
                .recordTimeMin(Integer.valueOf(minTime))
                .maxFrameRate(Integer.valueOf(maxFramerate))
                .videoBitrate(Integer.valueOf(bitrate))
                .captureThumbnailsTime(1)
                .build();
        MediaRecorderActivity.goSmallVideoRecorder(this, ActSendVideo.class.getName(), config);

    }

    private boolean checkStrEmpty(String str, String display) {
        if (TextUtils.isEmpty(str)) {
            Toast.makeText(this, display, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static void initSmallVideo() {
        recordDir = new File(getVideoRecordDir());
        JianXiCamera.setVideoCachePath(recordDir.getAbsolutePath());
        // 初始化拍摄
        JianXiCamera.initialize(false, null);
    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public static String getSDPath() {
        File sdDir = null;
        // 判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
            return sdDir.toString();
        }
        return null;
    }

    /**
     * 获取录制视频的路径
     * @return
     */
    public static String getVideoRecordDir(){
        String dirPath = getSDPath();
        dirPath += "/" + "videorecordcompress";
        File file = new File(dirPath);
        if(!file.exists()){
            file.mkdir();
        }
        dirPath +="/"+"videorecord";
        file = new File(dirPath);
        if(!file.exists()){
            file.mkdir();
        }
        return dirPath;
    }

}

