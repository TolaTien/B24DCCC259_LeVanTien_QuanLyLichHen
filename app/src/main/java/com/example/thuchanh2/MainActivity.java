package com.example.thuchanh2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_APPOINTMENT_ID = "appointment_id";

    private TextView tvStatTotal;
    private TextView tvStatReminder;
    private TextView tvStatNoReminder;
    private ListView lvAppointments;
    private LinearLayout layoutEmpty;
    private Button btnAddAppointment;
    private Button btnLogout;

    private DatabaseHelper dbHelper;
    private AppointmentAdapter adapter;
    private ArrayList<Appointment> appointmentList;

    private final ActivityResultLauncher<Intent> actionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadDataFromDatabase();
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String msg = result.getData().getStringExtra(AppPopup.EXTRA_POPUP_MESSAGE);
                    if (msg != null && !msg.isEmpty()) {
                        AppPopup.showSuccess(this, msg);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatReminder = findViewById(R.id.tvStatReminder);
        tvStatNoReminder = findViewById(R.id.tvStatNoReminder);
        lvAppointments = findViewById(R.id.lvAppointments);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnAddAppointment = findViewById(R.id.btnAddAppointment);
        btnLogout = findViewById(R.id.btnLogout);

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(this, appointmentList);
        lvAppointments.setAdapter(adapter);

        // Nhấn vào một dòng để mở màn hình chi tiết lịch hẹn (truyền id qua Intent)
        lvAppointments.setOnItemClickListener((parent, view, position, id) -> {
            Appointment selected = adapter.getItem(position);
            Intent intent = new Intent(MainActivity.this, AppointmentDetailActivity.class);
            intent.putExtra(EXTRA_APPOINTMENT_ID, selected.getId());
            actionLauncher.launch(intent);
        });

        // Mở màn hình thêm lịch hẹn
        btnAddAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddAppointmentActivity.class);
            actionLauncher.launch(intent);
        });

        // Đăng xuất và chuyển về màn hình LoginActivity kèm thông báo popup
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra(AppPopup.EXTRA_POPUP_MESSAGE, getString(R.string.msg_logout_success));
            startActivity(intent);
            finish();
        });

        // Hiển thị thông báo popup nếu vừa đăng nhập thành công
        if (getIntent() != null) {
            String popupMsg = getIntent().getStringExtra(AppPopup.EXTRA_POPUP_MESSAGE);
            if (popupMsg != null && !popupMsg.isEmpty()) {
                AppPopup.showSuccess(this, popupMsg);
                getIntent().removeExtra(AppPopup.EXTRA_POPUP_MESSAGE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        // Truy vấn danh sách lịch hẹn từ SQLite (ORDER BY date ASC, time ASC)
        appointmentList = dbHelper.getAllAppointments();
        adapter.setAppointments(appointmentList);

        // Truy vấn thống kê từ SQLite
        int totalCount = dbHelper.countAll();
        int reminderCount = dbHelper.countReminders();
        int noReminderCount = Math.max(0, totalCount - reminderCount);

        tvStatTotal.setText(String.valueOf(totalCount));
        tvStatReminder.setText(String.valueOf(reminderCount));
        tvStatNoReminder.setText(String.valueOf(noReminderCount));

        // Xử lý hiển thị khi danh sách rỗng
        if (appointmentList.isEmpty()) {
            lvAppointments.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            lvAppointments.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
