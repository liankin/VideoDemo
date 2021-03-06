package com.admin.videodemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Camera;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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
import com.mabeijianxi.smallvideorecord2.model.MediaRecorderConfig;
import com.mabeijianxi.smallvideorecord2.model.OnlyCompressOverBean;
import com.mabeijianxi.smallvideorecord2.model.VBRMode;

import java.io.File;
import java.util.List;

/**
 * 使用FFMpeg实现视频录制与压缩
 */
public class ActFFMpegVideoRecordCompress extends AppCompatActivity {

    private ScrollView sv;
    private Button bt_start;
    private TextView tv_size;
    private Button bt_choose;
    private EditText et_width;
    private EditText et_height;
    private EditText et_maxtime;
    private Spinner spinner_record;
    private EditText et_maxframerate;
    private final int CHOOSE_CODE = 0x000520;
    private RadioGroup rg_aspiration;
    private ProgressDialog mProgressDialog;
    private LinearLayout ll_only_compress;
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
    private EditText et_bitrate;

    private EditText et_only_scale;
    private EditText et_mintime;
    private Spinner spinner_need_full;

    private static File recordDir;//录制文件
    private static File compressDir;//压缩文件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_ffmpeg_video_record_compress);
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

        rg_aspiration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_recorder:
                        sv.setVisibility(View.VISIBLE);
                        ll_only_compress.setVisibility(View.GONE);
                        break;
                    case R.id.rb_local:
                        sv.setVisibility(View.GONE);
                        ll_only_compress.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

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
        rg_aspiration = (RadioGroup) findViewById(R.id.rg_aspiration);
        sv = (ScrollView) findViewById(R.id.sv);
        bt_choose = (Button) findViewById(R.id.bt_choose);
        ll_only_compress = (LinearLayout) findViewById(R.id.ll_only_compress);

        tv_size = (TextView) findViewById(R.id.tv_size);
        et_width = (EditText) findViewById(R.id.et_width);
        et_height = (EditText) findViewById(R.id.et_height);
        et_maxframerate = (EditText) findViewById(R.id.et_maxframerate);
        et_bitrate = (EditText) findViewById(R.id.et_record_bitrate);
        et_maxtime = (EditText) findViewById(R.id.et_maxtime);
        et_mintime = (EditText) findViewById(R.id.et_mintime);
        et_only_framerate = (EditText) findViewById(R.id.et_only_framerate);
        et_only_scale = (EditText) findViewById(R.id.et_only_scale);


        spinner_record = (Spinner) findViewById(R.id.spinner_record);
        spinner_need_full = (Spinner) findViewById(R.id.spinner_need_full);


        i_only_compress = findViewById(R.id.i_only_compress);
        rg_only_compress_mode = (RadioGroup) i_only_compress.findViewById(R.id.rg_mode);
        ll_only_compress_crf = (LinearLayout) i_only_compress.findViewById(R.id.ll_crf);
        et_only_compress_crfSize = (EditText) i_only_compress.findViewById(R.id.et_crfSize);
        ll_only_compress_bitrate = (LinearLayout) i_only_compress.findViewById(R.id.ll_bitrate);
        et_only_compress_maxbitrate = (EditText) i_only_compress.findViewById(R.id.et_maxbitrate);
        tv_only_compress_maxbitrate = (TextView) i_only_compress.findViewById(R.id.tv_maxbitrate);
        et_only_compress_bitrate = (EditText) i_only_compress.findViewById(R.id.et_bitrate);

        spinner_only_compress = (Spinner) findViewById(R.id.spinner_only_compress);

        bt_start = (Button) findViewById(R.id.bt_start);


    }

    /**
     * 选择本地视频，为了方便我采取了系统的API，所以也许在一些定制机上会取不到视频地址，
     * 所以选择手机里视频的代码根据自己业务写为妙。
     *
     * @param v
     */
    public void choose(View v) {

//        Intent it = new Intent(Intent.ACTION_GET_CONTENT,
//                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//
//        it.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
//        startActivityForResult(it, CHOOSE_CODE);

        String videoFilePath = recordDir.getAbsolutePath() + "/videorecord1519898039101/1519898039101.mp4";
        compressVideo(videoFilePath,"");

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
        //FFMpegUtils.captureThumbnails("/storage/emulated/0/DCIM/mabeijianxi/1496455533800/1496455533800.mp4", "/storage/emulated/0/DCIM/mabeijianxi/1496455533800/1496455533800.jpg", "1");

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

    /**
     * 压缩指定视频文件
     * @param videoFilePath
     */
    private void compressVideo(String videoFilePath,String compressFilePath){

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
                Intent intent = new Intent(ActFFMpegVideoRecordCompress.this, ActSendVideo.class);
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

//    /**
//     * 获取系统时间- 视频保存的时间
//     *
//     * @return
//     */
//    public static String getCurrentDate() {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        Date curDate = new Date(System.currentTimeMillis());
//        String date = formatter.format(curDate);
//        return date;
//    }

}
