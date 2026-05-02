package com.prank.hacked;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Vibrator vibrator;
    private ToneGenerator toneGenerator;
    private CountDownTimer countDownTimer;
    private volatile boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        );

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (Exception e) {
            toneGenerator = null;
        }

        TextView countdownText = (TextView) findViewById(R.id.countdownText);
        Button stopButton = (Button) findViewById(R.id.stopButton);

        startPrank(countdownText);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPrank();
            }
        });
    }

    private void startPrank(final TextView countdownText) {
        running = true;

        // Vibração caótica
        long[] pattern = {0, 200, 100, 300, 50, 400, 80, 200, 60, 500};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            vibrator.vibrate(pattern, 0);
        }

        // Som em loop
        final ToneGenerator tg = toneGenerator;
        Thread soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (tg != null) {
                        tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400);
                    }
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    if (tg != null) {
                        tg.startTone(ToneGenerator.TONE_CDMA_HIGH_SS, 200);
                    }
                    try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                }
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();

        // Contagem regressiva
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final long seconds = millisUntilFinished / 1000;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countdownText.setText(String.valueOf(seconds));
                    }
                });
            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countdownText.setText("\uD83D\uDC80");
                    }
                });
            }
        }.start();
    }

    private void stopPrank() {
        running = false;
        if (vibrator != null) vibrator.cancel();
        if (countDownTimer != null) countDownTimer.cancel();
        if (toneGenerator != null) toneGenerator.stopTone();
        finish();
    }

    @Override
    protected void onDestroy() {
        stopPrank();
        super.onDestroy();
    }
}
