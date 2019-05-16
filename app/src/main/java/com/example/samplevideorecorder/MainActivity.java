package com.example.samplevideorecorder;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    final private static String path = "/sdcard/recorded_video.mp4";
    MediaRecorder recorder;
    SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SurfaceView 는 서피스홀더에 의해 제어
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);  // 카메라 미리보기
        holder = surfaceView.getHolder();

        Button recordBtn = (Button) findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                }
                recorder = new MediaRecorder();

                // MediaRecorder 객체에 필요한 정보 설정
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

                recorder.setOutputFile(path);
                recorder.setPreviewDisplay(holder.getSurface());  // MediaRecorder에 미리보기 디스플레이로 SurfaceView 설정
                try {
                    recorder.prepare();
                    recorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        Button recordStopBtn = (Button) findViewById(R.id.recordStopBtn);
        recordStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder == null)
                    return;

                // 녹화 중지 후 리소스 해제
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;

                // 미디어 앨범에 저장하기위해 내용 제공자 사용
                ContentValues values = new ContentValues(10);

                values.put(MediaStore.MediaColumns.TITLE, "RecordedVideo");
                values.put(MediaStore.Audio.Media.ALBUM, "Video Album");
                values.put(MediaStore.Audio.Media.ARTIST, "Mike");
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Video");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Audio.Media.DATA, path);

                Uri videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                if (videoUri == null) {
                    Log.d("SampleVideoRecorder", "Video insert failed.");
                    return;
                }

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));
            }
        });
    }
}
