package com.example.onlinefooddeliverysystem.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinefooddeliverysystem.R;
import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.model.CartItem;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    public interface OnCartActionListener {
        void onCartChanged();
    }

    private final List<CartItem> items;
    private final OnCartActionListener listener;

    public CartAdapter(List<CartItem> items, OnCartActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.ivFood.setImageResource(item.getFood().getImageResId());
        holder.tvName.setText(item.getFood().getName());
        holder.tvDesc.setText(item.getFood().getTaste() + " · " + item.getFood().getDescription());
        holder.tvPrice.setText(FormatUtils.price(item.getTotalPrice()));
        holder.tvCount.setText("x" + item.getCount());
        holder.btnAdd.setOnClickListener(v -> {
            CartManager.getInstance().addFood(item.getFood());
            listener.onCartChanged();
        });
        holder.btnMinus.setOnClickListener(v -> {
            CartManager.getInstance().decreaseFood(item.getFood());
            listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvName;
        TextView tvDesc;
        TextView tvPrice;
        TextView tvCount;
        ImageButton btnAdd;
        ImageButton btnMinus;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.iv_cart_food);
            tvName = itemView.findViewById(R.id.tv_cart_name);
            tvDesc = itemView.findViewById(R.id.tv_cart_desc);
            tvPrice = itemView.findViewById(R.id.tv_cart_price);
            tvCount = itemView.findViewById(R.id.tv_cart_count);
            btnAdd = itemView.findViewById(R.id.btn_cart_add);
            btnMinus = itemView.findViewById(R.id.btn_cart_minus);
        }
    }
}
