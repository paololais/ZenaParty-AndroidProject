package com.example.zenaparty.models;

import com.example.zenaparty.adapters.QRCodeAdapter;

public class QRCodeData {
    private String qrCodeId;
    private String message;
    private String qrUserId;

    public QRCodeData(){ }

    public QRCodeData(String qrCodeId, String message, String qrUserId) {
        this.qrCodeId = qrCodeId;
        this.message = message;
        this.qrUserId = qrUserId;
    }

    public String getQrCodeId() {
        return qrCodeId;
    }

    public String getMessage() {
        return message;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getQrUserId() {
        return qrUserId;
    }

    public void setQrUserId(String qrUserId) {
        this.qrUserId = qrUserId;
    }
}
