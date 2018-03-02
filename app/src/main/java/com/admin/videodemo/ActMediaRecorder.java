package com.admin.videodemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActMediaRecorder extends AppCompatActivity implements SurfaceHolder.Callback {

    @BindView(R.id.surfaceview)
    SurfaceView surfaceview;
    @BindView(R.id.imageview)
    ImageView imageview;
    @BindView(R.id.btnStartRecord)
    Button btnStartRecord;
    @BindView(R.id.btnPlayVideo)
    Button btnPlayVideo;
    @BindView(R.id.btnStopPlayVideo)
    Button btnStopPlayVideo;
    @BindView(R.id.btnCompressVideo)
    Button btnCompressVideo;
    @BindView(R.id.tv_show_time)
    TextView tvShowTime;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    private static final int REQUEST_EXTERNAL_STORAGE = 21;
    private final static int RESULT_PERMISSION = 1001;
    private final static int RESULT_SUCCESS = 3;

    private File recordDir;
    private File compressDir;
    private boolean isPlaying = false;//是否正在播放录像
    private boolean isRecording = false;//是否正在录像
    private MediaPlayer mediaPlayer;//多媒体播放器
    private int videoTime = 0;
    private MediaRecorder mRecorder;//多媒体录音
    private Camera mCamera;//相机
    private String videoFilePath;//视频保存路径
    private SurfaceHolder mSurfaceHolder;
    private Handler handler = new Handler();
    private boolean isHandlerRun = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isHandlerRun) {
                videoTime++;
                tvShowTime.setText(videoTime + "");
                handler.postDelayed(this, 1000);//休眠1秒
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        setContentView(R.layout.activity_act_media_recorder);
        ButterKnife.bind(this);

        SurfaceHolder holder = surfaceview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//缓冲区

        verifyStoragePermissions();
        recordDir = new File(getVideoRecordDir());
        compressDir = new File(getVideoCompressDir());
    }

    @OnClick({R.id.btnStartRecord, R.id.btnPlayVideo, R.id.btnStopPlayVideo, R.id.btnCompressVideo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnStartRecord:
                startStopRecord();
                break;
            case R.id.btnPlayVideo:
                playVideo();
                break;
            case R.id.btnStopPlayVideo:
                if (mediaPlayer != null) {
                    isHandlerRun = false;
                    handler.removeCallbacks(runnable);
                    tvShowTime.setText("0");
                    isPlaying = false;
                    mediaPlayer.stop();//停止媒体播放器
                    mediaPlayer.reset();//重置媒体播放器
                    mediaPlayer.release();//释放资源
                    mediaPlayer = null;
                    imageview.setVisibility(View.VISIBLE);
                    btnPlayVideo.setText("播放");
                    videoTime = 0;
                }
                break;
            case R.id.btnCompressVideo:
                String savePath = compressDir + "/" + getDate() + ".mp4";
                compressVideo(videoFilePath,savePath);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if (!mStartedFlg) {
            mImageView.setVisibility(View.GONE);
        }*/
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceview = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mCamera != null) {
            mCamera.release();
            mCamera.release();
            mCamera = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 开始/结束录制
     */
    private void startStopRecord() {
        if (isPlaying) {
            if (mediaPlayer != null) {
                isPlaying = false;
                mediaPlayer.stop();//停止媒体播放器
                mediaPlayer.reset();//重置媒体播放器
                mediaPlayer.release();//释放资源
                mediaPlayer = null;

                isHandlerRun = false;
                handler.removeCallbacks(runnable);
                tvShowTime.setText("0");
                btnPlayVideo.setText("播放");
                videoTime = 0;
            }
        }
        //如果正在录像
        if (!isRecording) {
            isHandlerRun = true;
            videoTime = 0;
            handler.postDelayed(runnable, 1000);
            imageview.setVisibility(View.GONE);
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
            }
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (mCamera != null) {
                mCamera.setDisplayOrientation(90);
                mCamera.unlock();
                mRecorder.setCamera(mCamera);
            }
            try {
                // 这两项需要放在setOutputFormat之前,设置音频和视频的来源
                mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);//摄录像机
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//相机

                // Set output file format
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//输出格式 mp4

                // 这两项需要放在setOutputFormat之后  设置编码器
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//音频编码格式
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);//视频编码格式

                mRecorder.setVideoSize(640, 480);//视频分辨率
                mRecorder.setVideoFrameRate(30);//帧速率
                mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);//视频清晰度
                mRecorder.setOrientationHint(90);//输出视频播放的方向提示
                //设置记录会话的最大持续时间（毫秒）
                mRecorder.setMaxDuration(30 * 1000);
                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());//预览显示的控件

                if (recordDir != null) {
                    videoFilePath = recordDir + "/" + getDate() + ".mp4";
                    mRecorder.setOutputFile(videoFilePath);//输出文件路径
                    mRecorder.prepare();//准备
                    mRecorder.start();//开始
                    isRecording = true;//录像开始
                    btnStartRecord.setText("结束录制");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //停止
        else {
            if (isRecording) {
                handler.removeCallbacks(runnable);
                isHandlerRun = false;
                mRecorder.stop();//停止
                mRecorder.reset();//重置，设置为空闲状态
                mRecorder.release();//释放
                mRecorder = null;
                btnStartRecord.setText("开始录制");
                videoTime = 0;
                //释放相机
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
            }
            isRecording = false;
        }
    }

    /**
     * 播放录像、暂停
     */
    private void playVideo() {
        if (isRecording) {
            Toast.makeText(ActMediaRecorder.this, "正在录制，请结束录制再播放",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (!isPlaying) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                mediaPlayer.reset();
                if (videoFilePath == null) {
                    Toast.makeText(ActMediaRecorder.this, "暂无视频资源", Toast.LENGTH_SHORT).show();
                } else {
                    imageview.setVisibility(View.GONE);
                    Uri uri = Uri.parse(videoFilePath);
                    mediaPlayer = MediaPlayer.create(ActMediaRecorder.this, uri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDisplay(mSurfaceHolder);//设置显示的控件
                    try {
                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();*/
                    videoTime = 0;
                    isHandlerRun = true;
                    handler.postDelayed(runnable, 1000);
                    isPlaying = true;
                    mediaPlayer.start();
                    btnPlayVideo.setText("暂停");
                    //监听播放器是否播放结束
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            imageview.setVisibility(View.VISIBLE);
                            btnPlayVideo.setText("播放");
                            isHandlerRun = false;
                            isPlaying = false;
                            handler.removeCallbacks(runnable);
                            Toast.makeText(ActMediaRecorder.this, "播放完毕",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isHandlerRun = false;
                    btnPlayVideo.setText("继续播放");
                } else {
                    mediaPlayer.start();
                    isHandlerRun = true;
                    btnPlayVideo.setText("暂停");
                }
            }
        }
    }

    /**
     * 获取系统时间-视频保存的时间
     *
     * @return
     */
    public static String getDate() {
//        Calendar mCalendar = Calendar.getInstance();
//        int year = mCalendar.get(Calendar.YEAR);
//        int month = mCalendar.get(Calendar.MONTH);
//        int day = mCalendar.get(Calendar.DATE);
//        int hour = mCalendar.get(Calendar.HOUR);
//        int minute = mCalendar.get(Calendar.MINUTE);
//        int second = mCalendar.get(Calendar.SECOND);
//        String date = "" + year +  (month + 1)  + day   + hour  + minute  + second;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String date = formatter.format(curDate);
        return date;
    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        // 判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
            return sdDir.toString();
        }
        return null;
    }

    public String getVideoRecordDir(){
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

    public String getVideoCompressDir(){
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
     * 压缩视频
     */
    private void compressVideo(final String filePath, final String savePath) {
        final String imagePath = recordDir + "/image.png";

    }

    /**
     * 请求权限
     */
    public void verifyStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isGranted = true;
            for (int i = 0; i < PERMISSIONS_STORAGE.length; i++) {
                int j = ContextCompat.checkSelfPermission(ActMediaRecorder.this, PERMISSIONS_STORAGE[i]);
                if (j != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            if (!isGranted) {
                //权限还没有授予，进行申请权限
                startRequestPermission();
            } else {
                //权限授予
            }
        } else {

        }
    }

    /**
     * 开始提交请求权限
     */
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(ActMediaRecorder.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }

    /**
     * 用户权限 申请 的回调方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RESULT_SUCCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults.length != 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                        boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                        if (!b) {
                            // 用户还是想用我的 APP 的
                            // 提示用户去应用设置界面手动开启权限
                            showDialogTipUserGoToAppSettting();
                        } else {
                            //    finish();
                        }
                    } else {
                        // Picker.from(ActEditAgencyInfo.this).count(1).enableCamera(true).setEngine(new GlideEngine()).forResult(RESULT_LICENSE);
                        //      Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    /**
     * 提示用户去应用设置界面手动开启权限
     */
    private void showDialogTipUserGoToAppSettting() {

        AlertDialog dialog = new AlertDialog.Builder(ActMediaRecorder.this)
                .setTitle("权限不可用")
                .setMessage("请在-设置-应用管理中，允许使用存储权限")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
    }

    /**
     * 跳转到当前应用的设置界面
     */
    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, RESULT_PERMISSION);
    }


}

