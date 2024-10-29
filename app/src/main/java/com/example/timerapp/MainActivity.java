package com.example.timerapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText inputTime;
    private TextView timerText;
    private Button startPauseButton, stopButton, resetButton;
    private TimerTask timerTask;
    private int timeInSeconds;
    private int remainingTime;
    private boolean isPaused = false;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        inputTime = findViewById(R.id.input_time);
        timerText = findViewById(R.id.timer_text);
        startPauseButton = findViewById(R.id.start_pause_button);
        stopButton = findViewById(R.id.stop_button);
        resetButton = findViewById(R.id.reset_button);

        startPauseButton.setOnClickListener(view -> {
            if (!isRunning) {
                String timeInput = inputTime.getText().toString();
                if (!timeInput.isEmpty()) {
                    timeInSeconds = Integer.parseInt(timeInput);
                    if (timeInSeconds > 0) {
                        remainingTime = timeInSeconds;
                        startTimer();
                    } else {
                        Toast.makeText(MainActivity.this, "Enter a valid time in seconds", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter time in seconds", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isPaused) {
                    resumeTimer();
                } else {
                    pauseTimer();
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        stopButton.setOnClickListener(view -> stopTimer());
        resetButton.setOnClickListener(view -> resetTimer());
    }

    private void startTimer() {
        isRunning = true;
        isPaused = false;
        startPauseButton.setText("Pause");
        timerTask = new TimerTask();
        timerTask.execute(remainingTime);
    }

    private void pauseTimer() {
        isPaused = true;
        startPauseButton.setText("Resume");
    }

    private void resumeTimer() {
        isPaused = false;
        startPauseButton.setText("Pause");
        timerTask = new TimerTask();
        timerTask.execute(remainingTime);
    }

    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel(true);
        }
        isRunning = false;
        startPauseButton.setText("Start");
        timerText.setText(remainingTime + "");
    }

    private void resetTimer() {
        stopTimer();
        remainingTime = 0;
        timerText.setText("0");
        startPauseButton.setText("Start");
    }

    private class TimerTask extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            remainingTime = params[0];
            try {
                while (remainingTime > 0 && !isCancelled()) {
                    if (!isPaused) {
                        publishProgress(remainingTime);
                        remainingTime--;
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int secondsLeft = values[0];
            timerText.setText(String.valueOf(secondsLeft));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!isPaused && remainingTime == 0) {
                timerText.setText("✓");
                triggerAlarm();
                isRunning = false;
                startPauseButton.setText("Start");
            }
        }
    }

    private void triggerAlarm() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(1000);
        }
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();
    }
}