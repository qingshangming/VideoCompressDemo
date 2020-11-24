package com.tool.videocompressdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.vincent.videocompressor.VideoCompress;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * User: yjm
 * Email:1056540798@qq.com
 * Date: 2020/11/24
 * Annotation:
 **/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnShot, btn_compress;
    ImageView btn_iv;
    TextView tv_shot_address, tv_shot_out_address, tv_proess, tv_issuc, tv_inputSize, tv_outSize, tv_indicator;
    String TAG = "i_ver";
    String inputPath;
    String outPath;
    double inputSize = 0, outSize = 0;
    private long startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnShot = findViewById(R.id.btn_shot);
        btnShot.setOnClickListener(this);
        tv_shot_address = findViewById(R.id.tv_shot_address);
        tv_shot_out_address = findViewById(R.id.tv_shot_out_address);
        tv_proess = findViewById(R.id.tv_proess);
        tv_issuc = findViewById(R.id.tv_issuc);
        btn_iv = findViewById(R.id.btn_iv);
        tv_inputSize = findViewById(R.id.tv_inputSize);
        tv_outSize = findViewById(R.id.tv_outSize);
        btn_compress = findViewById(R.id.btn_compress);
        tv_indicator = (TextView) findViewById(R.id.tv_indicator);
        String[] perStr = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO};


        btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureSelector.create(MainActivity.this).externalPictureVideo(inputPath);//预览视频
            }
        });

        btn_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //开始压缩
                outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + File.separator + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date()) + ".mp4";
                VideoCompress.compressVideoLow(inputPath, outPath, new VideoCompress.CompressListener() {
                    @Override
                    public void onStart() {
                        tv_indicator.setText("压缩中..." + "\n"
                                + "开始时间: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
//                        tv_proess.setVisibility(View.VISIBLE);
                        startTime = System.currentTimeMillis();
                        Util.writeFile(MainActivity.this, "Start at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
                    }

                    @Override
                    public void onSuccess() {
                        String previous = tv_indicator.getText().toString();
                        tv_indicator.setText(previous + "\n"
                                + "压缩成功!" + "\n"
                                + "结束时间: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));

                        tv_shot_out_address.setText(outPath);
                        tv_issuc.setText("成功");
                        endTime = System.currentTimeMillis();
                        try {
                            tv_outSize.setText("压缩后大小：" + Util.getFileSize(new File(outPath)) + "M");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
          /*              Util.writeFile(MainActivity.this, "End at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
                        Util.writeFile(MainActivity.this, "Total: " + ((endTime - startTime) / 1000) + "s" + "\n");
                        Util.writeFile(MainActivity.this);*/

                    }

                    @Override
                    public void onFail() {
                        tv_issuc.setText("失败");
                        tv_indicator.setText("Compress Failed!");
                        endTime = System.currentTimeMillis();
                        Util.writeFile(MainActivity.this, "Failed Compress!!!" + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                    }

                    @Override
                    public void onProgress(float percent) {
                        tv_proess.setText(String.valueOf(percent) + "%");

                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        //拍摄
//
        PictureSelector.create(MainActivity.this)
                .openCamera(PictureMimeType.ofVideo())// 单独拍照，也可录像或也可音频 看你传入的类型是图片or视频
                .theme(R.style.picture_default_style)// 主题样式设置 具体参考 values/styles
                //.setOutputCameraPath()// 自定义相机输出目录，只针对Android Q以下，例如 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +  File.separator + "Camera" + File.separator;
                .minSelectNum(1)// 最小选择数量
                .closeAndroidQChangeWH(true)//如果图片有旋转角度则对换宽高，默认为true
                .closeAndroidQChangeVideoWH(true)// 如果视频有旋转角度则对换宽高，默认false
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选
                .isPreviewVideo(false)// 是否可预览视频
                .isEnablePreviewAudio(true) // 是否可播放音频
//                .isCompress(true)// 是否压缩
                .videoQuality(1)// 视频录制质量 0 or 1
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {// 图片选择结果回调
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                for (LocalMedia media : selectList) {
                    inputPath = media.getRealPath();
                    Log.i(TAG, "绝对路径:" + media.getRealPath());
                    Log.i(TAG, "Android Q 特有Path:" + media.getAndroidQToPath());
                    Log.i(TAG, "宽高: " + media.getWidth() + "x" + media.getHeight());
                    Log.i(TAG, "Size: " + media.getSize());
                    inputSize = (media.getSize() / 1024) / 1024;
                }
                if (inputSize > 0) {
                    tv_inputSize.setText("拍摄大小：" + inputSize + "M");
                }
                if (inputPath != null) {
                    tv_shot_address.setText("拍摄路径：" + inputPath);
                    Glide.with(this)
                            .load(inputPath)
                            .centerCrop()
//                            .placeholder(R.color.app_color_f6)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(btn_iv);
                }

            }
        }
    }

    private Locale getLocale() {
        Configuration config = getResources().getConfiguration();
        Locale sysLocale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sysLocale = getSystemLocale(config);
        } else {
            sysLocale = getSystemLocaleLegacy(config);
        }

        return sysLocale;
    }

    @SuppressWarnings("deprecation")
    public static Locale getSystemLocaleLegacy(Configuration config) {
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemLocale(Configuration config) {
        return config.getLocales().get(0);
    }
}