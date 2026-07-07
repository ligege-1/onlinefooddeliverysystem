package com.example.onlinefooddeliverysystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.UserManager;

public class MyActivity extends AppCompatActivity {
    private ImageView ivAvatar;
    private TextView tvTitle;
    private TextView tvAuthAction;
    private TextView tvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserState();
    }

    private void initView() {
        ivAvatar = findViewById(R.id.tv_avatar);
        tvTitle = findViewById(R.id.tv_user_title);
        tvAuthAction = findViewById(R.id.tv_auth_action);
        tvLogout = findViewById(R.id.tv_logout);

        bindBottomNav();

        tvAuthAction.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        ivAvatar.setOnClickListener(v -> showAvatarDialog());
        findViewById(R.id.card_address).setOnClickListener(v -> openAddressManage());
        tvLogout.setOnClickListener(v -> {
            UserManager.getInstance().logout(this);
            refreshUserState();
        });
    }

    private void refreshUserState() {
        boolean loggedIn = UserManager.getInstance().isLoggedIn(this);
        if (loggedIn) {
            ivAvatar.setImageResource(UserManager.getInstance().getAvatarResId(this));
            tvTitle.setText(UserManager.getInstance().getUsername(this));
            tvAuthAction.setText("已登录");
            tvAuthAction.setEnabled(false);
            tvLogout.setVisibility(TextView.VISIBLE);
        } else {
            ivAvatar.setImageResource(R.drawable.avatar_default);
            tvTitle.setText("");
            tvAuthAction.setText("登录 / 注册");
            tvAuthAction.setEnabled(true);
            tvLogout.setVisibility(TextView.GONE);
        }
    }

    private void showAvatarDialog() {
        if (!UserManager.getInstance().isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_avatar_picker, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        bindAvatarOption(dialogView, dialog, R.id.avatar_option_tom, UserManager.AVATAR_TOM);
        bindAvatarOption(dialogView, dialog, R.id.avatar_option_xiaoxin, UserManager.AVATAR_XIAOXIN);
        bindAvatarOption(dialogView, dialog, R.id.avatar_option_jerry, UserManager.AVATAR_JERRY);
        bindAvatarOption(dialogView, dialog, R.id.avatar_option_pig, UserManager.AVATAR_PIG);

        dialog.show();
    }

    private void bindAvatarOption(View rootView, AlertDialog dialog, int viewId, String avatarKey) {
        rootView.findViewById(viewId).setOnClickListener(v -> {
            UserManager.getInstance().updateAvatar(this, avatarKey);
            ivAvatar.setImageResource(UserManager.getInstance().getAvatarResIdByKey(avatarKey));
            dialog.dismiss();
        });
    }

    private void openAddressManage() {
        if (!UserManager.getInstance().isLoggedIn(this)) {
            Toast.makeText(this, "登录后才可以修改地址", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        startActivity(new Intent(this, AddressManageActivity.class));
    }

    private void bindBottomNav() {
        TextView tvHome = findViewById(R.id.nav_home);
        TextView tvRecommend = findViewById(R.id.nav_recommend);
        TextView tvOrder = findViewById(R.id.nav_order);
        TextView tvMine = findViewById(R.id.nav_mine);

        tvHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        tvRecommend.setOnClickListener(v -> startActivity(new Intent(this, RecommendActivity.class)));
        tvOrder.setOnClickListener(v -> startActivity(new Intent(this, OrderCenterActivity.class)));

        tvMine.setSelected(true);
    }
}
