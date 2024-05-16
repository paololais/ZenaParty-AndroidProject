package com.example.zenaparty.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class MyEvent implements Parcelable {
    String event_id, event_name, date, location, time, type,price, description, userId;

    public MyEvent() {
    }

    // Implementa Parcelable
    protected MyEvent(Parcel in) {
        event_id = in.readString();
        event_name = in.readString();
        date = in.readString();
        location = in.readString();
        time = in.readString();
        type = in.readString();
        price = in.readString();
        description = in.readString();
        userId = in.readString();
    }

    public MyEvent(String event_id, String event_name, String date, String location, String time, String type, String price, String description, String userId) {
        this.event_id = event_id;
        this.event_name = event_name;
        this.date = date;
        this.location = location;
        this.time = time;
        this.type = type;
        this.price = price;
        this.description = description;
        this.userId = userId;
    }


    public static final Creator<MyEvent> CREATOR = new Creator<MyEvent>() {
        @Override
        public MyEvent createFromParcel(Parcel in) {
            return new MyEvent(in);
        }

        @Override
        public MyEvent[] newArray(int size) {
            return new MyEvent[size];
        }
    };


    public String getEvent_id() {
        return event_id;
    }

    public String getEvent_name() {
        return event_name;
    }

    public String getDate() {return date;}

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() {
        return userId;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(event_id);
        dest.writeString(event_name);
        dest.writeString(date);
        dest.writeString(location);
        dest.writeString(time);
        dest.writeString(type);
        dest.writeString(price);
        dest.writeString(description);
        dest.writeString(userId);
    }

}