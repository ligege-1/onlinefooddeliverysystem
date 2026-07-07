package com.example.onlinefooddeliverysystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.UserManager;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        TextView tvLogin = findViewById(R.id.tv_login_submit);
        TextView tvRegister = findViewById(R.id.tv_to_register);
        TextView tvBack = findViewById(R.id.tv_back);

        tvBack.setOnClickListener(v -> finish());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (UserManager.getInstance().login(this, username, password)) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                finish();
            } else if (!UserManager.getInstance().hasAccount(this)) {
                Toast.makeText(this, "还没有账户，先去注册吧", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "账号或密码不正确", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
