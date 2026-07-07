package com.example.onlinefooddeliverysystem;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.UserManager;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etConfirmPassword = findViewById(R.id.et_confirm_password);
        TextView tvSubmit = findViewById(R.id.tv_register_submit);
        TextView tvBack = findViewById(R.id.tv_back);

        tvBack.setOnClickListener(v -> finish());
        tvSubmit.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请先填写用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            if (UserManager.getInstance().accountExists(this, username)) {
                Toast.makeText(this, "这个账号已经注册过了", Toast.LENGTH_SHORT).show();
                return;
            }
            if (UserManager.getInstance().register(this, username, password)) {
                Toast.makeText(this, "注册成功，请返回登录", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
