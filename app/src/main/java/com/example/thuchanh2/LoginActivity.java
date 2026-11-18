package com.example.thuchanh2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> handleLogin());

        // Hiển thị thông báo popup nếu vừa chuyển từ màn hình khác sang (ví dụ: Đăng xuất)
        if (getIntent() != null) {
            String popupMsg = getIntent().getStringExtra(AppPopup.EXTRA_POPUP_MESSAGE);
            if (popupMsg != null && !popupMsg.isEmpty()) {
                AppPopup.showSuccess(this, popupMsg);
                getIntent().removeExtra(AppPopup.EXTRA_POPUP_MESSAGE);
            }
        }
    }

    private void handleLogin() {
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Không cho phép đăng nhập khi tên đăng nhập hoặc mật khẩu bị bỏ trống
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_empty_username));
            AppPopup.showError(this, R.string.error_empty_username);
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_empty_password));
            AppPopup.showError(this, R.string.error_empty_password);
            etPassword.requestFocus();
            return;
        }

        // Kiểm tra với bảng users trong cơ sở dữ liệu
        if (dbHelper.checkLogin(username, password)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(AppPopup.EXTRA_POPUP_MESSAGE, getString(R.string.msg_login_success));
            startActivity(intent);
            finish();
        } else {
            AppPopup.showError(this, R.string.error_wrong_credentials);
            etPassword.setText("");
            etPassword.requestFocus();
        }
    }
}
