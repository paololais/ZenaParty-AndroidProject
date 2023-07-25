package com.example.zenaparty.models;

public interface EventListInterface {
    void onItemClick(int position);

    void onButtonActionClick(int position);

    void onEventRemoved(boolean success, int position);
}
