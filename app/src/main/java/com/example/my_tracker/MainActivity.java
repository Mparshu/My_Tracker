// MainActivity.java
package com.example.my_tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private List<String> goals;
    private TextView totalViewCountTextView;
    private TextView timerTextView;
    private LinearLayout goalsContainer;
    private EditText goalInputEditText;
    private Button showGoalsButton;
    private int totalViewCount;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean timerRunning;

    private static final String PREFS_NAME = "GoalTrackerPrefs";
    private static final String GOALS_KEY = "goals";
    private static final String VIEW_COUNT_KEY = "viewCount";
    private static final String TIMER_KEY = "timerRemaining";
    private static final long WAIT_TIME_MILLIS = 10 * 60 * 1000; // 10 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        goalsContainer = findViewById(R.id.goalsContainer);
        goalInputEditText = findViewById(R.id.goalInputEditText);
        Button addGoalButton = findViewById(R.id.addGoalButton);
        showGoalsButton = findViewById(R.id.showGoalsButton);
        totalViewCountTextView = findViewById(R.id.totalViewCountTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // Load saved data
        loadGoals();

        // Add Goal Button Listener
        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goal = goalInputEditText.getText().toString().trim();
                if (!goal.isEmpty()) {
                    goals.add(goal);
                    saveGoals();
                    goalInputEditText.setText("");
                    Toast.makeText(MainActivity.this, "Goal Added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Show Goals Button Listener
        showGoalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning) {
                    incrementViewCount();
                    displayGoals();
                    startTimer();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Please wait before checking goals again",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis > 0 ? timeLeftInMillis : WAIT_TIME_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
                timerRunning = true;
                showGoalsButton.setEnabled(false);
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                showGoalsButton.setEnabled(true);
                timerTextView.setText("Ready to check goals");
                timeLeftInMillis = 0;
                saveGoals();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(),
                "Time until next check: %02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void loadGoals() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load goals
        Gson gson = new Gson();
        String jsonGoals = prefs.getString(GOALS_KEY, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        goals = gson.fromJson(jsonGoals, type);

        if (goals == null) {
            goals = new ArrayList<>();
        }

        // Load view count
        totalViewCount = prefs.getInt(VIEW_COUNT_KEY, 0);
        updateViewCountDisplay();

        // Load timer state
        timeLeftInMillis = prefs.getLong(TIMER_KEY, 0);
        if (timeLeftInMillis > 0) {
            startTimer();
        }
    }

    private void saveGoals() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save goals
        Gson gson = new Gson();
        String jsonGoals = gson.toJson(goals);
        editor.putString(GOALS_KEY, jsonGoals);

        // Save view count
        editor.putInt(VIEW_COUNT_KEY, totalViewCount);

        // Save timer state
        editor.putLong(TIMER_KEY, timeLeftInMillis);

        editor.apply();
    }

    private void incrementViewCount() {
        totalViewCount++;
        updateViewCountDisplay();
        saveGoals();
    }

    private void updateViewCountDisplay() {
        totalViewCountTextView.setText("Total Goal Views: " + totalViewCount);
    }

    private void displayGoals() {
        // Clear previous goals
        goalsContainer.removeAllViews();

        // If no goals, show a message
        if (goals.isEmpty()) {
            TextView noGoalsText = new TextView(this);
            noGoalsText.setText("No goals added yet!");
            goalsContainer.addView(noGoalsText);
            return;
        }

        // Dynamically create views for each goal
        for (String goal : goals) {
            TextView goalTextView = new TextView(this);
            goalTextView.setText(goal);
            goalTextView.setTextSize(16f);
            goalTextView.setPadding(0, 8, 0, 8);
            goalsContainer.addView(goalTextView);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveGoals();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}