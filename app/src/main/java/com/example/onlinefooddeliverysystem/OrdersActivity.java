package com.example.onlinefooddeliverysystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinefooddeliverysystem.adapter.OrderAdapter;
import com.example.onlinefooddeliverysystem.db.OrderDbHelper;
import com.example.onlinefooddeliverysystem.model.OrderBean;

import java.util.ArrayList;

public class OrdersActivity extends AppCompatActivity {
    private OrderDbHelper dbHelper;
    private TextView tvEmpty;
    private TextView tvClear;
    private RecyclerView rvOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        dbHelper = new OrderDbHelper(this);
        TextView tvBack = findViewById(R.id.tv_back);
        tvClear = findViewById(R.id.tv_clear_orders);
        tvEmpty = findViewById(R.id.tv_empty);
        rvOrder = findViewById(R.id.rv_order);

        tvBack.setOnClickListener(v -> finish());
        tvClear.setOnClickListener(v -> showClearDialog());
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) {
            loadOrders();
        }
    }

    private void loadOrders() {
        ArrayList<OrderBean> orders = dbHelper.getOrders();
        tvEmpty.setText(orders.isEmpty() ? "暂无历史订单，去首页下一单吧" : "");
        tvClear.setVisibility(orders.isEmpty() ? View.GONE : View.VISIBLE);
        rvOrder.setAdapter(new OrderAdapter(orders, this::continuePay));
    }

    private void continuePay(OrderBean order) {
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    private void showClearDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清空历史订单？")
                .setMessage("清空后当前列表中的订单记录会被删除。")
                .setNegativeButton("取消", null)
                .setPositiveButton("清空", (dialog, which) -> {
                    dbHelper.clearOrders();
                    loadOrders();
                })
                .show();
    }
}
