package com.prank.hacked;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
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
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tela cheia, sem barra de status
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Volume no máximo
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        );

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        TextView countdownText = findViewById(R.id.countdownText);
        Button stopButton = findViewById(R.id.stopButton);

        startPrank(countdownText);

        stopButton.setOnClickListener(v -> stopPrank());
    }

    private void startPrank(TextView countdownText) {
        running = true;

        // Vibração caótica contínua
        long[] pattern = {0, 200, 100, 300, 50, 400, 80, 200, 60, 500, 100, 300};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            vibrator.vibrate(pattern, 0);
        }

        // Som de alarme em loop
        Thread soundThread = new Thread(() -> {
            while (running) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_SS, 200);
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400);
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();

        // Contagem regressiva de 30 segundos
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                runOnUiThread(() ->
                    countdownText.setText(String.valueOf(seconds))
                );
            }

            @Override
            public void onFinish() {
                runOnUiThread(() ->
                    countdownText.setText("💀")
                );
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
