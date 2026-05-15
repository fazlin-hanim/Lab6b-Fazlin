package com.example.lab6b_fazlin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView textAlarmPrompt;
    private Button buttonstartSetDialog;
    private Button button;
    private EditText message;

    private static final int ALARM_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textAlarmPrompt = findViewById(R.id.alarmprompt);
        buttonstartSetDialog = findViewById(R.id.startSetDialog);
        message = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        buttonstartSetDialog.setOnClickListener(v -> {
            textAlarmPrompt.setText("");
            openTimePickerDialog(false);
        });

        button.setOnClickListener(arg0 -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Exit Application?")
                    .setMessage("Click yes to exit!")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> finish())
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel())
                    .create()
                    .show();
        });
    }

    private void openTimePickerDialog(boolean is24HourView) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                MainActivity.this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24HourView
        );
        timePickerDialog.setTitle("Set Alarm Time");
        timePickerDialog.show();
    }

    private final TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calSet.set(Calendar.MINUTE, minute);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);

        if (calSet.compareTo(calNow) <= 0) {
            calSet.add(Calendar.DATE, 1);
        }

        setAlarm(calSet);
    };

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Calendar targetCal) {
        String msgText = message.getText().toString();

        textAlarmPrompt.setText(
                "\n\n**************************************************\n" +
                        "Alarm is set @ " + targetCal.getTime() + "\n" +
                        "**************************************************\n" +
                        "Message: " + msgText
        );

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.putExtra("msg", msgText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
        }
    }
}