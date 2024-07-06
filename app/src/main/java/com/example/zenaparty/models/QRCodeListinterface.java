package com.example.zenaparty.models;

public interface QRCodeListinterface {
    void onItemClick(int position);

    void onButtonActionClick(int position);

    void onQRCodeRemoved(boolean success, int position);
}
