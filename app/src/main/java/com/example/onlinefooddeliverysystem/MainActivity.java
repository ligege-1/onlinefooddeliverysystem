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
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        refreshShops("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshShops(etSearch.getText().toString());
    }

    private void initView() {
        etSearch = findViewById(R.id.et_search);
        RecyclerView rvShop = findViewById(R.id.rv_shop);

        rvShop.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShopAdapter(displayShops, shop -> {
            Intent intent = new Intent(MainActivity.this, ShopDetailActivity.class);
            intent.putExtra("shop", shop);
            startActivity(intent);
        });
        rvShop.setAdapter(adapter);

        bindBottomNav();

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
    }

    private void bindBottomNav() {
        TextView tvHome = findViewById(R.id.nav_home);
        TextView tvRecommend = findViewById(R.id.nav_recommend);
        TextView tvOrder = findViewById(R.id.nav_order);
        TextView tvMine = findViewById(R.id.nav_mine);

        tvRecommend.setOnClickListener(v -> startActivity(new Intent(this, RecommendActivity.class)));
        tvOrder.setOnClickListener(v -> startActivity(new Intent(this, OrderCenterActivity.class)));
        tvMine.setOnClickListener(v -> startActivity(new Intent(this, MyActivity.class)));

        tvHome.setSelected(true);
    }

    private void refreshShops(String keyword) {
        displayShops.clear();
        displayShops.addAll(DataRepository.searchShops(keyword));
        adapter.notifyDataSetChanged();
    }
}
