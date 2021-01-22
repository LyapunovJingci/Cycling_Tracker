package com.lyapunov.cyclingtracker.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyapunov.cyclingtracker.R;
import com.lyapunov.cyclingtracker.networking.ImageLoader;

import java.util.ArrayList;

public class HistoryActivityAdapter extends RecyclerView.Adapter<HistoryActivityAdapter.HistoryViewHolder>{
    private int numOfItems;
    private ArrayList<HistoryThumbnail> items;
    final private historyClickListener listener;

    public interface historyClickListener {
        void onHistoryResultClick (int clickedItemIndex);
    }

    public HistoryActivityAdapter(int numOfItems, ArrayList<HistoryThumbnail> items, historyClickListener listener) {
        this.numOfItems = numOfItems;
        this.items = items;
        this.listener = listener;
    }


    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.history_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
//        Context context = holder.mImageHistory.getContext();
//        if (holder.mImageHistory == null) {
//            holder.mImageHistory = new ImageView(context);
//        }
        holder.bind(position);
       //ImageLoader.getImageLoader().loadImage("|40.737102,-73.990318|40.749825,-73.987963|40.752946,-73.987384|40.755823,-73.986397", holder.mImageHistory);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    class HistoryViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        TextView mListItemTextView;
        ImageView mImageHistory;
        public HistoryViewHolder(View itemView) {
            super(itemView);
            mListItemTextView = itemView.findViewById(R.id.history_item);
            mImageHistory = itemView.findViewById(R.id.history_pic);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            mListItemTextView.setText(items.get(position).datencity);
            ImageLoader.getImageLoader().loadImage(items.get(position).path, mImageHistory);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            listener.onHistoryResultClick(clickedPosition);
        }
    }
}


