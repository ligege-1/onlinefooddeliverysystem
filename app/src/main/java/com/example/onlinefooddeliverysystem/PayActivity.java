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
    private TextView tvTitle;
    private TextView tvHint;
    private TextView tvMockSuccess;
    private FrameLayout flQrBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        order = (OrderBean) getIntent().getSerializableExtra("order");
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvInfo = findViewById(R.id.tv_pay_info);
        TextView tvHistory = findViewById(R.id.tv_to_history);
        ImageView ivQr = findViewById(R.id.iv_pay_qr);
        tvTitle = findViewById(R.id.tv_pay_title);
        tvHint = findViewById(R.id.tv_pay_hint);
        tvMockSuccess = findViewById(R.id.tv_mock_success);
        flQrBox = findViewById(R.id.fl_qr_box);

        tvBack.setOnClickListener(v -> finish());
        if (order != null) {
            tvInfo.setText(order.getShopName() + "\n" + FormatUtils.price(order.getTotalPrice()) + "\n" + order.getCreatedAt());
            showQrCode(ivQr, buildQrContent(order));
            if ("已支付".equals(order.getStatus())) {
                showPaidState(false);
            }
        } else {
            showQrCode(ivQr, "PAY_SUCCESS");
        }
        tvMockSuccess.setOnClickListener(v -> showPaidState(true));
        tvHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrdersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void showPaidState(boolean saveStatus) {
        if (saveStatus && order != null && order.getId() > 0) {
            new OrderDbHelper(this).updateStatus(order.getId(), "已支付");
        }
        tvTitle.setText("支付成功");
        tvHint.setText("测试支付已完成，可以查看历史订单");
        flQrBox.setVisibility(View.GONE);
        tvMockSuccess.setVisibility(View.GONE);
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
