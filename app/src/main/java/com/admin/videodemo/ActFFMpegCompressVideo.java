package com.admin.videodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mabeijianxi.smallvideorecord2.JianXiCamera;
import com.mabeijianxi.smallvideorecord2.LocalMediaCompress;
import com.mabeijianxi.smallvideorecord2.MediaRecorderActivity;
import com.mabeijianxi.smallvideorecord2.StringUtils;
import com.mabeijianxi.smallvideorecord2.model.AutoVBRMode;
import com.mabeijianxi.smallvideorecord2.model.BaseMediaBitrateConfig;
import com.mabeijianxi.smallvideorecord2.model.CBRMode;
import com.mabeijianxi.smallvideorecord2.model.LocalMediaConfig;
import com.mabeijianxi.smallvideorecord2.model.OnlyCompressOverBean;
import com.mabeijianxi.smallvideorecord2.model.VBRMode;

import java.io.File;

/**
 * 使用FFMpeg实现视频压缩
 */
public class ActFFMpegCompressVideo extends AppCompatActivity{

    private final int PERMISSION_REQUEST_CODE = 0x001;
    private Button bt_choose;
    private final int CHOOSE_CODE = 0x000520;
    private ProgressDialog mProgressDialog;
    private View i_only_compress;
    private RadioGroup rg_only_compress_mode;
    private LinearLayout ll_only_compress_crf;
    private EditText et_only_compress_crfSize;
    private LinearLayout ll_only_compress_bitrate;
    private EditText et_only_compress_maxbitrate;
    private TextView tv_only_compress_maxbitrate;
    private EditText et_only_compress_bitrate;
    private Spinner spinner_only_compress;
    private EditText et_only_framerate;

    private static final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private EditText et_only_scale;
    private static File recordDir;//录制文件
    private static File compressDir;//压缩文件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_ffmpeg_compress_video);
        initSmallVideo();
        initView();
        initEvent();
    }

    private void initEvent() {
        rg_only_compress_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_auto:
                        ll_only_compress_crf.setVisibility(View.VISIBLE);
                        ll_only_compress_bitrate.setVisibility(View.GONE);
                        break;
                    case R.id.rb_vbr:
                        ll_only_compress_crf.setVisibility(View.GONE);
                        ll_only_compress_bitrate.setVisibility(View.VISIBLE);
                        tv_only_compress_maxbitrate.setVisibility(View.VISIBLE);
                        et_only_compress_maxbitrate.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_cbr:
                        ll_only_compress_crf.setVisibility(View.GONE);
                        ll_only_compress_bitrate.setVisibility(View.VISIBLE);
                        tv_only_compress_maxbitrate.setVisibility(View.GONE);
                        et_only_compress_maxbitrate.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    private void initView() {
        bt_choose = (Button) findViewById(R.id.bt_choose);//
        et_only_framerate = (EditText) findViewById(R.id.et_only_framerate);//
        et_only_scale = (EditText) findViewById(R.id.et_only_scale);//

        i_only_compress = findViewById(R.id.i_only_compress);//
        rg_only_compress_mode = (RadioGroup) i_only_compress.findViewById(R.id.rg_mode);
        ll_only_compress_crf = (LinearLayout) i_only_compress.findViewById(R.id.ll_crf);
        et_only_compress_crfSize = (EditText) i_only_compress.findViewById(R.id.et_crfSize);
        ll_only_compress_bitrate = (LinearLayout) i_only_compress.findViewById(R.id.ll_bitrate);
        et_only_compress_maxbitrate = (EditText) i_only_compress.findViewById(R.id.et_maxbitrate);
        tv_only_compress_maxbitrate = (TextView) i_only_compress.findViewById(R.id.tv_maxbitrate);
        et_only_compress_bitrate = (EditText) i_only_compress.findViewById(R.id.et_bitrate);

        spinner_only_compress = (Spinner) findViewById(R.id.spinner_only_compress);//
    }

    /**
     * 选择本地视频，为了方便我采取了系统的API，所以也许在一些定制机上会取不到视频地址，
     * 所以选择手机里视频的代码根据自己业务写为妙。
     *
     * @param v
     */
    public void choose(View v) {
        String videoFilePath = recordDir.getAbsolutePath() + "/videorecord1519898039101/1519898039101.mp4";
        compressVideo(videoFilePath);
    }

    /**
     * 压缩指定视频文件
     * @param videoFilePath
     */
    private void compressVideo(String videoFilePath){

        File file = new File(videoFilePath);
        if(!file.exists()){
            Toast.makeText(this, "视频文件不存在，无法执行压缩操作", Toast.LENGTH_SHORT).show();
            return;
        }
        BaseMediaBitrateConfig compressMode = null;

        int compressModeCheckedId = rg_only_compress_mode.getCheckedRadioButtonId();

        if (compressModeCheckedId == R.id.rb_cbr) {
            String bitrate = et_only_compress_bitrate.getText().toString();
            if (checkStrEmpty(bitrate, "请输入压缩额定码率")) {
                return;
            }
            compressMode = new CBRMode(166, Integer.valueOf(bitrate));
        } else if (compressModeCheckedId == R.id.rb_auto) {
            String crfSize = et_only_compress_crfSize.getText().toString();
            if (TextUtils.isEmpty(crfSize)) {
                compressMode = new AutoVBRMode();
            } else {
                compressMode = new AutoVBRMode(Integer.valueOf(crfSize));
            }
        } else if (compressModeCheckedId == R.id.rb_vbr) {
            String maxBitrate = et_only_compress_maxbitrate.getText().toString();
            String bitrate = et_only_compress_bitrate.getText().toString();

            if (checkStrEmpty(maxBitrate, "请输入压缩最大码率") || checkStrEmpty(bitrate, "请输入压缩额定码率")) {
                return;
            }
            compressMode = new VBRMode(Integer.valueOf(maxBitrate), Integer.valueOf(bitrate));
        } else {
            compressMode = new AutoVBRMode();
        }

        if (!spinner_only_compress.getSelectedItem().toString().equals("none")) {
            compressMode.setVelocity(spinner_only_compress.getSelectedItem().toString());
        }

        String sRate = et_only_framerate.getText().toString();
        String scale = et_only_scale.getText().toString();
        int iRate = 0;
        float fScale = 0;
        if (!TextUtils.isEmpty(sRate)) {
            iRate = Integer.valueOf(sRate);
        }
        if (!TextUtils.isEmpty(scale)) {
            fScale = Float.valueOf(scale);
        }
        LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
        final LocalMediaConfig config = buidler
                .setVideoPath(videoFilePath)
                .captureThumbnailsTime(1)
                .doH264Compress(compressMode)
                .setFramerate(iRate)
                .setScale(fScale)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress("", "压缩中...", -1);
                    }
                });
                OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                    }
                });
                Intent intent = new Intent(ActFFMpegCompressVideo.this, ActSendVideo.class);
                intent.putExtra(MediaRecorderActivity.VIDEO_URI, onlyCompressOverBean.getVideoPath());
                intent.putExtra(MediaRecorderActivity.VIDEO_SCREENSHOT, onlyCompressOverBean.getPicPath());
                startActivity(intent);
            }
        }).start();
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
        compressDir = new File(getVideoCompressDir());
        JianXiCamera.setVideoCachePath(recordDir.getAbsolutePath());
        JianXiCamera.setVideoCompressPath(compressDir.getAbsolutePath());
        // 初始化拍摄
        JianXiCamera.initialize(false, null);
    }

    private void showProgress(String title, String message, int theme) {
        if (mProgressDialog == null) {
            if (theme > 0)
                mProgressDialog = new ProgressDialog(this, theme);
            else
                mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!StringUtils.isEmpty(title))
            mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
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
     * 获取压缩视频的路径
     * @return
     */
    public static String getVideoCompressDir(){
        String dirPath = getSDPath();
        dirPath += "/" + "videorecordcompress";
        File file = new File(dirPath);
        if(!file.exists()){
            file.mkdir();
        }
        dirPath +="/"+"videocompress";
        file = new File(dirPath);
        if(!file.exists()){
            file.mkdir();
        }
        return dirPath;
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

