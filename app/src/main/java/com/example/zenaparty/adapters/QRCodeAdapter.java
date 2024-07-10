package com.example.zenaparty.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenaparty.R;
import com.example.zenaparty.fragments.QRCodeDialogFragment;
import com.example.zenaparty.models.OnQRCodeDeletedListener;
import com.example.zenaparty.models.QRCodeData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder> {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static List<QRCodeData> qrDataList;
    private static Bitmap qrCodeBitmap;
    private static boolean isCreator;
    private static OnQRCodeDeletedListener listener;


    public QRCodeAdapter(List<QRCodeData> qrDataList, Context context, boolean isCreator, OnQRCodeDeletedListener listener) {
        QRCodeAdapter.qrDataList = qrDataList;
        QRCodeAdapter.context = context;
        QRCodeAdapter.isCreator = isCreator;
        QRCodeAdapter.listener = listener;
    }

    @NonNull
    @Override
    public QRCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_qr_code, parent, false);
        return new QRCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRCodeViewHolder holder, int position) {
        String message = qrDataList.get(position).getMessage();
        String qrCodeId = qrDataList.get(position).getQrCodeId();
        qrCodeBitmap = generateQRCode(qrCodeId, 200);
        holder.qrCodeImageView.setImageBitmap(qrCodeBitmap);
        holder.qrCodeTV.setText(message);
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    QRCodeData qrCodeData = qrDataList.get(position);
                    QRCodeDialogFragment dialogFragment = QRCodeDialogFragment.newInstance(byteArray, qrCodeData.getMessage(), qrCodeData.getQrCodeId(), isCreator, listener);
                    dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "QRCodeDialogFragment");
                }
            });
        }
    }

    public static Bitmap generateQRCode(String data, int dim) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, dim, dim);
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
