package com.example.onlinefooddeliverysystem;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.AddressManager;

public class AddressManageActivity extends AppCompatActivity {
    private LinearLayout llAddressList;
    private TextView tvDefaultAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_manage);
        initView();
        renderAddressList();
    }

    private void initView() {
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvAddAddress = findViewById(R.id.tv_add_address_entry);
        llAddressList = findViewById(R.id.ll_address_list);
        tvDefaultAddress = findViewById(R.id.tv_default_address_value);

        tvBack.setOnClickListener(v -> finish());
        tvDefaultAddress.setOnClickListener(v -> {
            AddressManager.getInstance().setCurrentAddress(this, AddressManager.getInstance().getDefaultAddress());
            renderAddressList();
        });
        tvAddAddress.setOnClickListener(v -> showAddAddressTypeDialog());
    }

    private void renderAddressList() {
        AddressManager addressManager = AddressManager.getInstance();
        llAddressList.removeAllViews();

        String current = addressManager.getCurrentAddress(this);
        boolean defaultSelected = addressManager.getDefaultAddress().equals(current);
        tvDefaultAddress.setText(addressManager.getDefaultAddress());
        tvDefaultAddress.setBackgroundResource(defaultSelected ? R.drawable.bg_tab_selected : R.drawable.bg_card);
        tvDefaultAddress.setTextColor(getColor(defaultSelected ? R.color.brand : R.color.text_primary));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (String address : addressManager.getCustomAddresses(this)) {
            View itemView = inflater.inflate(R.layout.item_address_option, llAddressList, false);
            TextView tvName = itemView.findViewById(R.id.tv_address_name);
            TextView tvTag = itemView.findViewById(R.id.tv_address_tag);
            boolean selected = address.equals(current);
            tvName.setText(address);
            tvName.setTextColor(getColor(selected ? R.color.brand : R.color.text_primary));
            tvTag.setText(selected ? "当前" : "选择");
            itemView.setBackgroundResource(selected ? R.drawable.bg_tab_selected : R.drawable.bg_card);
            itemView.setOnClickListener(v -> {
                AddressManager.getInstance().setCurrentAddress(this, address);
                renderAddressList();
            });
            llAddressList.addView(itemView);
        }
    }

    private void showAddAddressTypeDialog() {
        String[] options = {"普通地址", "寝室地址"};
        new AlertDialog.Builder(this)
                .setTitle("添加地址")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showCustomAddressDialog();
                    } else {
                        showDormAddressDialog();
                    }
                })
                .show();
    }

    private void showCustomAddressDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_address, null, false);
        EditText etCustomAddress = dialogView.findViewById(R.id.et_custom_address);
        new AlertDialog.Builder(this)
                .setTitle("添加新地址")
                .setView(dialogView)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String detail = etCustomAddress.getText().toString().trim();
                    if (detail.isEmpty()) {
                        Toast.makeText(this, "请先填写新地址", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String address = AddressManager.getInstance().buildCustomAddress(detail);
                    if (!AddressManager.getInstance().addCustomAddress(this, address)) {
                        Toast.makeText(this, "地址保存失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    renderAddressList();
                    Toast.makeText(this, "新地址已添加", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showDormAddressDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_dorm_address, null, false);
        EditText etBuilding = dialogView.findViewById(R.id.et_building);
        EditText etRoom = dialogView.findViewById(R.id.et_room);
        new AlertDialog.Builder(this)
                .setTitle("添加寝室地址")
                .setView(dialogView)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String building = etBuilding.getText().toString().trim();
                    String room = etRoom.getText().toString().trim();
                    if (!AddressManager.getInstance().isValidBuilding(building)) {
                        Toast.makeText(this, "寝室楼请填写 B1 到 B5", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!AddressManager.getInstance().isValidRoom(room)) {
                        Toast.makeText(this, "寝室号请填写 1000-1136 到 6000-6136", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String address = AddressManager.getInstance().buildDormAddress(building, room);
                    if (!AddressManager.getInstance().addCustomAddress(this, address)) {
                        Toast.makeText(this, "寝室地址保存失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    renderAddressList();
                    Toast.makeText(this, "寝室地址已添加", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
