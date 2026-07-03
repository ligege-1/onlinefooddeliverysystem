package com.example.onlinefooddeliverysystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinefooddeliverysystem.adapter.ShopAdapter;
import com.example.onlinefooddeliverysystem.data.DataRepository;
import com.example.onlinefooddeliverysystem.model.ShopBean;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<ShopBean> displayShops = new ArrayList<>();
    private ShopAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        refreshShops("");
    }

    private void initView() {
        EditText etSearch = findViewById(R.id.et_search);
        RecyclerView rvShop = findViewById(R.id.rv_shop);
        TextView tvHistory = findViewById(R.id.tv_history);
        TextView tvAi = findViewById(R.id.tv_ai);

        rvShop.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShopAdapter(displayShops, shop -> {
            Intent intent = new Intent(MainActivity.this, ShopDetailActivity.class);
            intent.putExtra("shop", shop);
            startActivity(intent);
        });
        rvShop.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshShops(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tvAi.setOnClickListener(v -> startActivity(new Intent(this, RecommendActivity.class)));
        tvHistory.setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
    }

    private void refreshShops(String keyword) {
        displayShops.clear();
        displayShops.addAll(DataRepository.searchShops(keyword));
        adapter.notifyDataSetChanged();
    }
}
