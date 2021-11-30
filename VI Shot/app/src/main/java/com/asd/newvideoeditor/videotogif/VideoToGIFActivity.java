package com.asd.newvideoeditor.videotogif;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.asd.newvideoeditor.GIFPreviewActivity;
import com.asd.newvideoeditor.R;
import com.asd.newvideoeditor.VideoPlayerState;
import com.asd.newvideoeditor.VideoSliceSeekBar;
import com.asd.newvideoeditor.VideoSliceSeekBar.SeekBarChangeListener;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.util.concurrent.TimeUnit;

@SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
public class VideoToGIFActivity extends AppCompatActivity {
    static final boolean i = true;
    public static String outputPath;
    public static Bitmap thumb;
    File a;
    String b;
    String c = null;
    Boolean d = Boolean.valueOf(false);
    String e = "00";
    ImageView f;
    public FFmpeg fFmpeg;
    VideoSliceSeekBar g;
    OnClickListener h = new OnClickListener() {
        @Override public void onClick(View view) {
            if (VideoToGIFActivity.this.d.booleanValue()) {
                VideoToGIFActivity.this.f.setBackgroundResource(R.drawable.play2);
                VideoToGIFActivity.this.d = Boolean.valueOf(false);
            } else {
                VideoToGIFActivity.this.f.setBackgroundResource(R.drawable.pause2);
                VideoToGIFActivity.this.d = Boolean.valueOf(VideoToGIFActivity.i);
            }
            VideoToGIFActivity.this.e();
        }
    };
    private PowerManager j;

    public TextView k;

    public TextView l;
    private TextView m;

    public TextView n;

    public VideoPlayerState o = new VideoPlayerState();
    private a p = new a();

    public VideoView q;
    private WakeLock r;
    private InterstitialAd s;

    private class a extends Handler {
        private boolean b;
        private Runnable c;

        private a() {
            this.b = false;
            this.c = new Runnable() {
                public void run() {
                    a.this.a();
                }
            };
        }


        public void a() {
            if (!this.b) {
                this.b = VideoToGIFActivity.i;
                sendEmptyMessage(0);
            }
        }

        @Override public void handleMessage(Message message) {
            this.b = false;
            VideoToGIFActivity.this.g.videoPlayingProgress(VideoToGIFActivity.this.q.getCurrentPosition());
            if (!VideoToGIFActivity.this.q.isPlaying() || VideoToGIFActivity.this.q.getCurrentPosition() >= VideoToGIFActivity.this.g.getRightProgress()) {
                if (VideoToGIFActivity.this.q.isPlaying()) {
                    VideoToGIFActivity.this.q.pause();
                    VideoToGIFActivity.this.d = Boolean.valueOf(false);
                    VideoToGIFActivity.this.q.seekTo(100);
                    VideoToGIFActivity.this.f.setBackgroundResource(R.drawable.play2);
                }
                VideoToGIFActivity.this.g.setSliceBlocked(false);
                VideoToGIFActivity.this.g.removeVideoStatusThumb();
                return;
            }
            postDelayed(this.c, 50);
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView( R.layout.videotogifactivity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText("Video To GIF");
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (i || supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(i);
            supportActionBar.setDisplayShowTitleEnabled(false);
            this.fFmpeg = FFmpeg.getInstance(this);
            f();
            StringBuilder sb = new StringBuilder();
            sb.append("VID_GIF-");
            sb.append(System.currentTimeMillis() / 1000);
            String sb2 = sb.toString();
            StringBuilder sb3 = new StringBuilder();
            sb3.append(Environment.getExternalStorageDirectory().getAbsoluteFile());
            sb3.append("/");
            sb3.append(getResources().getString(R.string.MainFolderName));
            sb3.append("/");
            sb3.append(getResources().getString(R.string.VideoToGIF));
            sb3.append("/");
            this.a = new File(sb3.toString());
            if (!this.a.exists()) {
                this.a.mkdirs();
            }
            StringBuilder sb4 = new StringBuilder(String.valueOf(this.a.getAbsolutePath()));
            sb4.append("/");
            sb4.append(sb2);
            outputPath = sb4.toString();
            this.k = (TextView) findViewById(R.id.left_pointer);
            this.l = (TextView) findViewById(R.id.right_pointer);
            this.f = (ImageView) findViewById(R.id.buttonply);
            this.g = (VideoSliceSeekBar) findViewById(R.id.seek_bar);
            this.q = (VideoView) findViewById(R.id.videoView1);
            this.m = (TextView) findViewById(R.id.Filename);
            this.n = (TextView) findViewById(R.id.dur);
            this.j = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.r = this.j.newWakeLock(6, "My Tag");
            Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
            if (lastNonConfigurationInstance != null) {
                this.o = (VideoPlayerState) lastNonConfigurationInstance;
            } else {
                Bundle extras = getIntent().getExtras();
                this.o.setFilename(extras.getString("videoPath"));
                this.c = extras.getString("videoPath");
                thumb = ThumbnailUtils.createVideoThumbnail(this.o.getFilename(), 1);
            }
            this.m.setText(new File(this.c).getName());
            d();
            this.q.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    VideoToGIFActivity.this.d = Boolean.valueOf(false);
                    VideoToGIFActivity.this.f.setBackgroundResource(R.drawable.play2);
                }
            });
            this.f.setOnClickListener(this.h);
            this.s = new InterstitialAd(this);
            this.s.setAdUnitId(getString(R.string.InterstitialAd));
            this.s.setAdListener(new AdListener() {
                @Override public void onAdClosed() {
                    VideoToGIFActivity.this.c();
                }
            });
            a();
            return;
        }
        throw new AssertionError();
    }

    private void a() {
        if (!this.s.isLoading() && !this.s.isLoaded()) {
            this.s.loadAd(new Builder().build());
        }
    }


    public void b() {
        if (this.s == null || !this.s.isLoaded()) {
            c();
        } else {
            this.s.show();
        }
    }


    public void c() {
        Intent intent = new Intent(this, GIFPreviewActivity.class);
        intent.putExtra("videourl", outputPath);
        intent.putExtra("isfrommain", i);
        startActivity(intent);
    }

    public void gifcommand() {
        String valueOf = String.valueOf(this.o.getStart() / 1000);
        String valueOf2 = String.valueOf(this.o.getDuration() / 1000);
        new MediaMetadataRetriever().setDataSource(this.o.getFilename());
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsoluteFile());
        sb.append("/");
        sb.append(getResources().getString(R.string.MainFolderName));
        sb.append("/");
        sb.append(getResources().getString(R.string.VideoToGIF));
        sb.append("/");
        sb.append(outputPath.substring(outputPath.lastIndexOf("/") + 1));
        sb.append(".gif");
        outputPath = sb.toString();
        a(new String[]{"-y", "-ss", valueOf, "-t", valueOf2, "-i", this.c, "-f", "gif", "-b", "2000k", "-r", "10", "-s", "320x240", outputPath}, outputPath);
    }

    private void a(String[] strArr, final String str) {
        try {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.show();
            this.fFmpeg.execute(strArr, new ExecuteBinaryResponseHandler() {
                @Override public void onFailure(String str) {
                    Log.d("ffmpegfailure", str);
                    try {
                        new File(str).delete();
                        VideoToGIFActivity.this.deleteFromGallery(str);
                        Toast.makeText(VideoToGIFActivity.this, "Error Creating Video", 0).show();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }

                @Override public void onSuccess(String str) {
                    progressDialog.dismiss();
                    VideoToGIFActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(new File(VideoToGIFActivity.outputPath))));
                    VideoToGIFActivity.this.b();
                }

                @Override public void onProgress(String str) {
                    Log.d("ffmpegResponse", str);
                    StringBuilder sb = new StringBuilder();
                    sb.append("progress : ");
                    sb.append(str);
                    progressDialog.setMessage(sb.toString());
                }

                @Override public void onStart() {
                    progressDialog.setMessage("Processing...");
                }

                @Override public void onFinish() {
                    progressDialog.dismiss();
                    VideoToGIFActivity.this.refreshGallery(str);
                }
            });
            getWindow().clearFlags(16);
        } catch (FFmpegCommandAlreadyRunningException unused) {
        }
    }

    private void d() {
        this.q.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                VideoToGIFActivity.this.g.setSeekBarChangeListener(new SeekBarChangeListener() {
                    public void SeekBarValueChanged(int i, int i2) {
                        if (VideoToGIFActivity.this.g.getSelectedThumb() == 1) {
                            VideoToGIFActivity.this.q.seekTo(VideoToGIFActivity.this.g.getLeftProgress());
                        }
                        VideoToGIFActivity.this.k.setText(VideoToGIFActivity.getTimeForTrackFormat(i, VideoToGIFActivity.i));
                        VideoToGIFActivity.this.l.setText(VideoToGIFActivity.getTimeForTrackFormat(i2, VideoToGIFActivity.i));
                        VideoToGIFActivity.this.e = VideoToGIFActivity.getTimeForTrackFormat(i, VideoToGIFActivity.i);
                        VideoToGIFActivity.this.o.setStart(i);
                        VideoToGIFActivity.this.b = VideoToGIFActivity.getTimeForTrackFormat(i2, VideoToGIFActivity.i);
                        VideoToGIFActivity.this.o.setStop(i2);
                        TextView g = VideoToGIFActivity.this.n;
                        StringBuilder sb = new StringBuilder();
                        sb.append("duration : ");
                        int i3 = (i2 / 1000) - (i / 1000);
                        sb.append(String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf(i3 / 3600), Integer.valueOf((i3 % 3600) / 60), Integer.valueOf(i3 % 60)}));
                        g.setText(sb.toString());
                    }
                });
                VideoToGIFActivity.this.b = VideoToGIFActivity.getTimeForTrackFormat(mediaPlayer.getDuration(), VideoToGIFActivity.i);
                VideoToGIFActivity.this.g.setMaxValue(mediaPlayer.getDuration());
                VideoToGIFActivity.this.g.setLeftProgress(0);
                VideoToGIFActivity.this.g.setRightProgress(mediaPlayer.getDuration());
                VideoToGIFActivity.this.g.setProgressMinDiff(0);
                VideoToGIFActivity.this.q.seekTo(100);
            }
        });
        this.q.setVideoPath(this.o.getFilename());
        this.q.seekTo(0);
        this.b = getTimeForTrackFormat(this.q.getDuration(), i);
    }


    public void e() {
        if (this.q.isPlaying()) {
            this.q.pause();
            this.g.setSliceBlocked(false);
            this.g.removeVideoStatusThumb();
            return;
        }
        this.q.seekTo(this.g.getLeftProgress());
        this.q.start();
        this.g.videoPlayingProgress(this.g.getLeftProgress());
        this.p.a();
    }

    @SuppressLint({"NewApi", "DefaultLocale"})
    public static String getTimeForTrackFormat(int i2, boolean z) {
        long j2 = (long) i2;
        return String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(j2)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(j2) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j2)))});
    }


    @SuppressLint({"WakelockTimeout"})
    @Override public void onResume() {
        super.onResume();
        this.r.acquire();
        this.q.seekTo(this.o.getCurrentTime());
    }


    public void onPause() {
        this.r.release();
        super.onPause();
        this.o.setCurrentTime(this.q.getCurrentPosition());
    }

    private void f() {
        try {
            this.fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override public void onFailure() {
                    VideoToGIFActivity.this.g();
                    Log.d("ffmpeg loading failed! ", "");
                }

                @Override public void onFinish() {
                    Log.d("ffmpeg loading finish! ", "");
                }

                @Override public void onStart() {
                    Log.d("ffmpeg loading started!", "");
                }

                @Override public void onSuccess() {
                    Log.d("ffmpeg loading success!", "");
                }
            });
        } catch (FFmpegNotSupportedException unused) {
            g();
        }
    }


    public void g() {
        new AlertDialog.Builder(this).setIcon(17301543).setTitle("Device not supported").setMessage("FFmpeg is not supported on your device").setCancelable(false).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                VideoToGIFActivity.this.finish();
            }
        }).create().show();
    }

    public void deleteFromGallery(String str) {
        String[] strArr = {"_id"};
        String[] strArr2 = {str};
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor query = contentResolver.query(uri, strArr, "_data = ?", strArr2, null);
        if (query.moveToFirst()) {
            try {
                contentResolver.delete(ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, query.getLong(query.getColumnIndexOrThrow("_id"))), null, null);
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
        } else {
            try {
                new File(str).delete();
                refreshGallery(str);
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
        query.close();
    }

    public void refreshGallery(String str) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(new File(str)));
        sendBroadcast(intent);
    }

    @Override public void onBackPressed() {
        Intent intent = new Intent(this, ListVideoAndMyAlbumActivity.class);
        intent.setFlags(67108864);
        startActivity(intent);
        finish();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picker, menu);
        return i;
    }

   @Override public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
            return i;
        }
        if (menuItem.getItemId() == R.id.Done) {
            if (this.q.isPlaying()) {
                this.q.pause();
                this.f.setBackgroundResource(R.drawable.play2);
            }
            gifcommand();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
