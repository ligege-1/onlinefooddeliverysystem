package com.example.onlinefooddeliverysystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.db.OrderDbHelper;
import com.example.onlinefooddeliverysystem.model.OrderBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;
import com.example.onlinefooddeliverysystem.util.QrCodeUtils;
import com.google.zxing.WriterException;

public class PayActivity extends AppCompatActivity {
    private OrderBean order;
    private View rootView;
    private TextView tvTitle;
    private TextView tvHint;
    private TextView tvMockSuccess;
    private FrameLayout flQrBox;
    private boolean paid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        order = (OrderBean) getIntent().getSerializableExtra("order");

        rootView = findViewById(R.id.pay_root);
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvInfo = findViewById(R.id.tv_pay_info);
        ImageView ivQr = findViewById(R.id.iv_pay_qr);
        tvTitle = findViewById(R.id.tv_pay_title);
        tvHint = findViewById(R.id.tv_pay_hint);
        tvMockSuccess = findViewById(R.id.tv_mock_success);
        flQrBox = findViewById(R.id.fl_qr_box);

        tvBack.setOnClickListener(v -> {
            if (paid) {
                returnHome();
            } else {
                finish();
            }
        });

        if (order != null) {
            tvInfo.setText(order.getShopName() + "\n" + FormatUtils.price(order.getTotalPrice()) + "\n" + order.getCreatedAt());
            showQrCode(ivQr, buildQrContent(order));
        } else {
            showQrCode(ivQr, "PAY_SUCCESS");
        }

        tvMockSuccess.setOnClickListener(v -> showPaidState());
    }

    private void showPaidState() {
        if (paid) {
            return;
        }
        paid = true;
        if (order != null && order.getId() > 0) {
            new OrderDbHelper(this).updateStatus(order.getId(), "已支付");
        }
        tvTitle.setText("支付成功");
        tvHint.setText("点击页面任意位置返回首页");
        flQrBox.setVisibility(View.GONE);
        tvMockSuccess.setVisibility(View.GONE);
        rootView.setOnClickListener(v -> returnHome());
    }

    private void returnHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void showQrCode(ImageView ivQr, String content) {
        try {
            Bitmap bitmap = QrCodeUtils.create(content, 640);
            ivQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "二维码生成失败", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildQrContent(OrderBean order) {
        return "PAY_SUCCESS_" + order.getTotalPrice();
    }
}
