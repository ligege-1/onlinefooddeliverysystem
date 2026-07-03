package com.example.onlinefooddeliverysystem.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinefooddeliverysystem.R;
import com.example.onlinefooddeliverysystem.model.OrderBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    public interface OnOrderClickListener {
        void onOrderClick(OrderBean order);
    }

    private final List<OrderBean> orders;
    private final OnOrderClickListener listener;

    public OrderAdapter(List<OrderBean> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderBean order = orders.get(position);
        boolean pending = "待支付".equals(order.getStatus());
        holder.tvShop.setText(order.getShopName());
        holder.tvTime.setText(order.getCreatedAt());
        holder.tvAddress.setText(order.getAddress());
        holder.tvPrice.setText(FormatUtils.price(order.getTotalPrice()));
        holder.tvStatus.setText(order.getStatus());
        holder.tvAction.setText(pending ? "点击继续支付" : "");
        holder.itemView.setOnClickListener(v -> {
            if (pending) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvShop;
        TextView tvTime;
        TextView tvAddress;
        TextView tvPrice;
        TextView tvStatus;
        TextView tvAction;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShop = itemView.findViewById(R.id.tv_order_shop);
            tvTime = itemView.findViewById(R.id.tv_order_time);
            tvAddress = itemView.findViewById(R.id.tv_order_address);
            tvPrice = itemView.findViewById(R.id.tv_order_price);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvAction = itemView.findViewById(R.id.tv_order_action);
        }
    }
}
