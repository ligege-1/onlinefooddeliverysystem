package com.example.onlinefooddeliverysystem.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinefooddeliverysystem.R;
import com.example.onlinefooddeliverysystem.data.AddressManager;
import com.example.onlinefooddeliverysystem.model.ShopBean;
import com.example.onlinefooddeliverysystem.util.DeliveryUtils;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    public interface OnShopClickListener {
        void onShopClick(ShopBean shop);
    }

    private final List<ShopBean> shops;
    private final OnShopClickListener listener;

    public ShopAdapter(List<ShopBean> shops, OnShopClickListener listener) {
        this.shops = shops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        ShopBean shop = shops.get(position);
        String address = AddressManager.getInstance().getCurrentAddress(holder.itemView.getContext());
        holder.ivShop.setImageResource(shop.getImageResId());
        holder.tvName.setText(shop.getName());
        holder.tvCategory.setText(shop.getCategory() + " · " + DeliveryUtils.buildListDeliveryText(shop, address));
        holder.tvScore.setText("评分 " + shop.getScore() + "  月售" + estimateSales(shop) + "+  人均 " + FormatUtils.price(estimatePerCapita(shop)));
        holder.tvPrice.setText("起送" + FormatUtils.price(shop.getMinPrice()) + "  配送" + FormatUtils.price(shop.getDeliveryFee()));
        holder.tvDesc.setText(shop.getNotice());
        holder.itemView.setOnClickListener(v -> listener.onShopClick(shop));
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    private int estimateSales(ShopBean shop) {
        int sales = 0;
        for (int i = 0; i < shop.getFoods().size(); i++) {
            sales += shop.getFoods().get(i).getSales();
        }
        return Math.max(100, sales / 10 * 10);
    }

    private int estimatePerCapita(ShopBean shop) {
        int total = 0;
        for (int i = 0; i < shop.getFoods().size(); i++) {
            total += shop.getFoods().get(i).getPrice();
        }
        int averagePrice = total / Math.max(1, shop.getFoods().size());
        int suggestedPerCapita = averagePrice + Math.max(3, shop.getDeliveryFee());
        return Math.max(suggestedPerCapita, shop.getMinPrice() + 4);
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivShop;
        TextView tvName;
        TextView tvCategory;
        TextView tvScore;
        TextView tvDesc;
        TextView tvPrice;

        ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivShop = itemView.findViewById(R.id.iv_shop);
            tvName = itemView.findViewById(R.id.tv_shop_name);
            tvCategory = itemView.findViewById(R.id.tv_shop_category);
            tvScore = itemView.findViewById(R.id.tv_shop_score);
            tvDesc = itemView.findViewById(R.id.tv_shop_desc);
            tvPrice = itemView.findViewById(R.id.tv_shop_price);
        }
    }
}
