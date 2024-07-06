package com.example.zenaparty.adapters;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenaparty.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.encoder.ByteMatrix;

import java.util.List;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder> {

    private List<String> qrDataList;

    public QRCodeAdapter(List<String> qrDataList) {
        this.qrDataList = qrDataList;
    }

    @NonNull
    @Override
    public QRCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_qr_code, parent, false);
        return new QRCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRCodeViewHolder holder, int position) {
        String data = qrDataList.get(position);
        Bitmap qrCodeBitmap = generateQRCode(data);
        holder.qrCodeImageView.setImageBitmap(qrCodeBitmap);
        holder.qrCodeTV.setText(data);
    }

    @Override
    public int getItemCount() {
        return qrDataList.size();
    }

    public static class QRCodeViewHolder extends RecyclerView.ViewHolder {
        ImageView qrCodeImageView;
        TextView qrCodeTV;

        public QRCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            qrCodeImageView = itemView.findViewById(R.id.qrCodeImageView);
            qrCodeTV = itemView.findViewById(R.id.qrCodeTV);
        }
    }

    private Bitmap generateQRCode(String data) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
