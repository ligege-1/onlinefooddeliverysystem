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
import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    public interface OnFoodActionListener {
        void onFoodClick(FoodBean food);

        void onCartChanged();
    }

    private final List<FoodBean> foods;
    private final OnFoodActionListener listener;

    public FoodAdapter(List<FoodBean> foods, OnFoodActionListener listener) {
        this.foods = foods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodBean food = foods.get(position);
        int count = CartManager.getInstance().getCount(food);
        holder.ivFood.setImageResource(food.getImageResId());
        holder.tvName.setText(food.getName());
        holder.tvTaste.setText(food.getTaste() + " · 月售" + food.getSales());
        holder.tvDesc.setText("近期推荐 · " + food.getDescription());
        holder.tvPrice.setText(FormatUtils.price(food.getPrice()));
        holder.tvCount.setText(String.valueOf(count));
        holder.btnMinus.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.tvCount.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onFoodClick(food));
        holder.btnAdd.setOnClickListener(v -> {
            CartManager.getInstance().addFood(food);
            notifyItemChanged(holder.getBindingAdapterPosition());
            listener.onCartChanged();
        });
        holder.btnMinus.setOnClickListener(v -> {
            CartManager.getInstance().decreaseFood(food);
            notifyItemChanged(holder.getBindingAdapterPosition());
            listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvName;
        TextView tvTaste;
        TextView tvDesc;
        TextView tvPrice;
        TextView tvCount;
        ImageButton btnAdd;
        ImageButton btnMinus;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.iv_food);
            tvName = itemView.findViewById(R.id.tv_food_name);
            tvTaste = itemView.findViewById(R.id.tv_food_taste);
            tvDesc = itemView.findViewById(R.id.tv_food_desc);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            tvCount = itemView.findViewById(R.id.tv_count);
            btnAdd = itemView.findViewById(R.id.btn_add);
            btnMinus = itemView.findViewById(R.id.btn_minus);
        }
    }
}
