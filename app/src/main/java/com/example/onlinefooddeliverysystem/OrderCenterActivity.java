package com.example.onlinefooddeliverysystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinefooddeliverysystem.adapter.OrderAdapter;
import com.example.onlinefooddeliverysystem.data.AddressManager;
import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.data.UserManager;
import com.example.onlinefooddeliverysystem.db.OrderDbHelper;
import com.example.onlinefooddeliverysystem.model.OrderBean;

import java.util.ArrayList;

public class OrderCenterActivity extends AppCompatActivity {
    private TextView tvLoginHint;
    private TextView tvClearOrders;
    private TextView tabCart;
    private TextView tabPending;
    private TextView tabHistory;
    private LinearLayout layoutCart;
    private LinearLayout layoutPending;
    private LinearLayout layoutHistory;
    private TextView tvCartEmpty;
    private LinearLayout cardCart;
    private TextView tvCartShop;
    private TextView tvCartCount;
    private TextView tvCartAddress;
    private TextView tvCartTotal;
    private TextView tvPendingEmpty;
    private TextView tvHistoryEmpty;
    private RecyclerView rvPendingOrders;
    private RecyclerView rvHistoryOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_center);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContent();
    }

    private void initView() {
        tvLoginHint = findViewById(R.id.tv_login_hint);
        tvClearOrders = findViewById(R.id.tv_clear_orders);
        tabCart = findViewById(R.id.tab_cart);
        tabPending = findViewById(R.id.tab_pending);
        tabHistory = findViewById(R.id.tab_history);
        layoutCart = findViewById(R.id.layout_cart);
        layoutPending = findViewById(R.id.layout_pending);
        layoutHistory = findViewById(R.id.layout_history);
        tvCartEmpty = findViewById(R.id.tv_cart_empty);
        cardCart = findViewById(R.id.card_cart);
        tvCartShop = findViewById(R.id.tv_cart_shop);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvCartAddress = findViewById(R.id.tv_cart_address);
        tvCartTotal = findViewById(R.id.tv_cart_total);
        tvPendingEmpty = findViewById(R.id.tv_pending_empty);
        tvHistoryEmpty = findViewById(R.id.tv_history_empty);
        rvPendingOrders = findViewById(R.id.rv_pending_orders);
        rvHistoryOrders = findViewById(R.id.rv_history_orders);

        rvPendingOrders.setLayoutManager(new LinearLayoutManager(this));
        rvHistoryOrders.setLayoutManager(new LinearLayoutManager(this));

        bindBottomNav();

        tabCart.setOnClickListener(v -> switchTab("cart"));
        tabPending.setOnClickListener(v -> switchTab("pending"));
        tabHistory.setOnClickListener(v -> switchTab("history"));
        tvLoginHint.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        tvClearOrders.setOnClickListener(v -> showClearDialog());
        cardCart.setOnClickListener(v -> openCart());

        switchTab("cart");
    }

    private void refreshContent() {
        boolean loggedIn = UserManager.getInstance().isLoggedIn(this);
        tvLoginHint.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        tvClearOrders.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (!loggedIn) {
            layoutCart.setVisibility(View.GONE);
            layoutPending.setVisibility(View.GONE);
            layoutHistory.setVisibility(View.GONE);
            return;
        }

        bindCartCard();
        bindOrderLists();
        switchTab(getCurrentTab());
    }

    private void bindCartCard() {
        CartManager cartManager = CartManager.getInstance();
        boolean hasItems = cartManager.hasItems();

        tvCartEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        cardCart.setVisibility(hasItems ? View.VISIBLE : View.GONE);

        if (!hasItems) {
            return;
        }

        int goodsTotal = cartManager.getTotalPrice();
        int deliveryTotal = cartManager.getDeliveryFeeTotal();
        int total = goodsTotal + deliveryTotal;
        tvCartShop.setText(cartManager.getShopSummary());
        tvCartCount.setText("共 " + cartManager.getTotalCount() + " 件商品，配送费 " + com.example.onlinefooddeliverysystem.util.FormatUtils.price(deliveryTotal));
        tvCartAddress.setText("送到 " + AddressManager.getInstance().getCurrentAddress(this));
        tvCartTotal.setText(com.example.onlinefooddeliverysystem.util.FormatUtils.price(total));
    }

    private void bindOrderLists() {
        ArrayList<OrderBean> allOrders = new OrderDbHelper(this).getOrders();
        ArrayList<OrderBean> pendingOrders = new ArrayList<>();
        ArrayList<OrderBean> historyOrders = new ArrayList<>();

        for (OrderBean order : allOrders) {
            if ("待支付".equals(order.getStatus())) {
                pendingOrders.add(order);
            } else {
                historyOrders.add(order);
            }
        }

        tvPendingEmpty.setVisibility(pendingOrders.isEmpty() ? View.VISIBLE : View.GONE);
        rvPendingOrders.setVisibility(pendingOrders.isEmpty() ? View.GONE : View.VISIBLE);
        rvPendingOrders.setAdapter(new OrderAdapter(pendingOrders, this::continuePay));

        tvHistoryEmpty.setVisibility(historyOrders.isEmpty() ? View.VISIBLE : View.GONE);
        rvHistoryOrders.setVisibility(historyOrders.isEmpty() ? View.GONE : View.VISIBLE);
        rvHistoryOrders.setAdapter(new OrderAdapter(historyOrders, this::showOrderDetail));
    }

    private void showClearDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清除订单")
                .setMessage("确定要清除待支付和历史订单吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("清除", (dialog, which) -> {
                    new OrderDbHelper(this).clearOrders();
                    refreshContent();
                })
                .show();
    }

    private String getCurrentTab() {
        if (tabPending.isSelected()) {
            return "pending";
        }
        if (tabHistory.isSelected()) {
            return "history";
        }
        return "cart";
    }

    private void switchTab(String tab) {
        boolean loggedIn = UserManager.getInstance().isLoggedIn(this);
        layoutCart.setVisibility(loggedIn && "cart".equals(tab) ? View.VISIBLE : View.GONE);
        layoutPending.setVisibility(loggedIn && "pending".equals(tab) ? View.VISIBLE : View.GONE);
        layoutHistory.setVisibility(loggedIn && "history".equals(tab) ? View.VISIBLE : View.GONE);

        updateTabStyle(tabCart, "cart".equals(tab));
        updateTabStyle(tabPending, "pending".equals(tab));
        updateTabStyle(tabHistory, "history".equals(tab));
    }

    private void updateTabStyle(TextView tab, boolean selected) {
        tab.setSelected(selected);
        tab.setBackgroundResource(selected ? R.drawable.bg_tab_selected : 0);
        tab.setTextColor(getColor(selected ? R.color.brand : R.color.text_secondary));
        tab.setTextSize(selected ? 14 : 13);
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void openCart() {
        if (!CartManager.getInstance().hasItems()) {
            return;
        }
        startActivity(new Intent(this, CartActivity.class));
    }

    private void continuePay(OrderBean order) {
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    private void showOrderDetail(OrderBean order) {
        new AlertDialog.Builder(this)
                .setTitle(order.getShopName())
                .setMessage("下单时间：" + order.getCreatedAt()
                        + "\n配送地址：" + order.getAddress()
                        + "\n订单金额：" + com.example.onlinefooddeliverysystem.util.FormatUtils.price(order.getTotalPrice())
                        + "\n订单状态：" + order.getStatus())
                .setPositiveButton("我知道了", null)
                .show();
    }

    private void bindBottomNav() {
        TextView tvHome = findViewById(R.id.nav_home);
        TextView tvRecommend = findViewById(R.id.nav_recommend);
        TextView tvOrder = findViewById(R.id.nav_order);
        TextView tvMine = findViewById(R.id.nav_mine);

        tvHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        tvRecommend.setOnClickListener(v -> startActivity(new Intent(this, RecommendActivity.class)));
        tvMine.setOnClickListener(v -> startActivity(new Intent(this, MyActivity.class)));

        tvOrder.setSelected(true);
    }
}
