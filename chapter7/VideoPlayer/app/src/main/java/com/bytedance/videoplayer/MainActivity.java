package com.bytedance.videoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private boolean portrait;
    private static final int REQUEST_VIDEO_CODE=1001;
    private static final int REQUEST_PERMISSION=1;
    private Button pick_btn;
    private Button playPause_btn;
    private Uri uri;
    //所需权限
    private String[] mPermissionsArrays = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView=findViewById(R.id.videoView);
        pick_btn=findViewById(R.id.pick_btn);
        playPause_btn=findViewById(R.id.play_pause_btn);
        videoView.setVideoPath(getVideoPath(R.raw.bytedance));//默认播放视频源
        videoView.setMediaController(new MediaController(this));
        //提供外界应用的隐式Intent
        Intent intent = getIntent();
        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)) {
            uri = intent.getData();
            videoView.setVideoURI(uri);
        }
        //选择一个本地视频
        pick_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkPermissionAllGranted(mPermissionsArrays)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(mPermissionsArrays,REQUEST_PERMISSION);
                    }
                }
                Intent pickIntent = new Intent();
                pickIntent.setType("video/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(pickIntent, null), REQUEST_VIDEO_CODE);
            }
        });
        //开始和暂停按钮
        playPause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoView.isPlaying())
                {
                    videoView.pause();
                    playPause_btn.setText(R.string.play);
                }
                else{
                    videoView.start();
                    playPause_btn.setText(R.string.pause);
                }
            }
        });

        //获取切换屏幕显示模式前的播放进度和播放状态
        if(savedInstanceState!=null){
            int schedule=savedInstanceState.getInt("schedule");
            boolean isPlaying=savedInstanceState.getBoolean("isPlaying");
            if(isPlaying){
                videoView.seekTo(schedule);
            }
            else{
                videoView.seekTo(schedule);
                videoView.pause();
            }
        }
    }

    //检查是否有相应权限
    private boolean checkPermissionAllGranted(String[] permissions) {
        // 6.0以下不需要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        playPause_btn.setText(R.string.play);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_VIDEO_CODE && resultCode== RESULT_OK ){
            uri=data.getData();
            videoView.setVideoURI(uri);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        tryFullScreen(!portrait);
    }

    //横屏全屏显示
    private void tryFullScreen(boolean fullScreen) {
        if (MainActivity.this instanceof AppCompatActivity) {
            ActionBar supportActionBar =  MainActivity.this.getSupportActionBar();
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        }
        setFullScreen(fullScreen);
    }

    private void setFullScreen(boolean fullScreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullScreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 记录当前播放进度和播放状态
        outState.putInt("schedule", videoView.getCurrentPosition());
        outState.putBoolean("isPlaying",videoView.isPlaying());
    }

    private String getVideoPath(int resId) {
        return "android.resource://" + this.getPackageName() + "/" + resId;
    }
}
