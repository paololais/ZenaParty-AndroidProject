package com.example.zenaparty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenaparty.models.EventListInterface;
import com.example.zenaparty.models.MyEvent;
import com.example.zenaparty.R;

import java.util.ArrayList;


public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.MyViewHolder> {
    final Context context;
    ArrayList<MyEvent> list;
    boolean visibility;
    private final EventListInterface eventListInterface;

    public EventListAdapter(Context context, ArrayList<MyEvent> list, EventListInterface eventListInterface,boolean visibility) {
        this.context = context;
        this.list = list;
        this.eventListInterface = eventListInterface;
        this.visibility = visibility;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.event_item_layout,parent,false);
        return new MyViewHolder(v, eventListInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull EventListAdapter.MyViewHolder holder, int position) {
        final MyEvent event = list.get(position);
        holder.event_name.setText(event.getEvent_name());
        holder.time.setText(event.getTime());
        holder.location.setText(event.getLocation());
        holder.type.setText(event.getType());
        holder.price.setText(event.getPrice());

        if (visibility){
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView event_name, time, location, type, price;
        ImageButton btnDelete;

        public MyViewHolder(@NonNull View itemView, EventListInterface eventListInterface) {
            super(itemView);

            event_name = itemView.findViewById(R.id.eventName);
            time = itemView.findViewById(R.id.eventTime);
            location = itemView.findViewById(R.id.eventLocation);
            type = itemView.findViewById(R.id.eventType);
            price = itemView.findViewById(R.id.eventPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(view -> {
                if (eventListInterface != null){
                    int pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION){
                        eventListInterface.onItemClick(pos);
                    }
                }
            });


            btnDelete.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    eventListInterface.onButtonActionClick(position);
                }
            });
        }
    }

    public void setEventList(ArrayList<MyEvent> eventList) {
        this.list = eventList;
    }

    public ArrayList<MyEvent> getList() {
        return list;
    }
}
