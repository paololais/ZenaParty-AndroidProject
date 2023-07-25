package com.example.zenaparty.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class MyEvent implements Parcelable {
    Long event_id;
    String event_name, date, location, time, type,price, description, username;

    public MyEvent() {
    }

    // Implementa Parcelable
    protected MyEvent(Parcel in) {
        if (in.readByte() == 0) {
            event_id = null;
        } else {
            event_id = in.readLong();
        }
        event_name = in.readString();
        date = in.readString();
        location = in.readString();
        time = in.readString();
        type = in.readString();
        price = in.readString();
        description = in.readString();
        username = in.readString();
    }

    public MyEvent(String event_name, String date, String location, String time, String type, String price, String description, String username) {
        this.event_id = (long) -1;
        this.event_name = event_name;
        this.date = date;
        this.location = location;
        this.time = time;
        this.type = type;
        this.price = price;
        this.description = description;
        this.username = username;
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


    public Long getEvent_id() {
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

    public String getUsername() {
        return username;
    }

    public void setEvent_id(Long event_id) {
        this.event_id = event_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (event_id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(event_id);
        }
        dest.writeString(event_name);
        dest.writeString(date);
        dest.writeString(location);
        dest.writeString(time);
        dest.writeString(type);
        dest.writeString(price);
        dest.writeString(description);
        dest.writeString(username);
    }

}