package com.lyapunov.cyclingtracker.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyapunov.cyclingtracker.R;

import java.util.ArrayList;

public class HistoryActivityAdapter extends RecyclerView.Adapter<HistoryActivityAdapter.HistoryViewHolder>{
    private int numOfItems;
    private ArrayList<String> items;
    final private historyClickListener listener;

    public interface historyClickListener {
        void onHistoryResultClick (int clickedItemIndex);
    }

    public HistoryActivityAdapter(int numOfItems, ArrayList<String> data, historyClickListener listener) {
        this.numOfItems = numOfItems;
        this.items = data;
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
        HistoryViewHolder viewHolder = new HistoryViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    class HistoryViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        TextView mListItemTextView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            mListItemTextView = itemView.findViewById(R.id.history_item);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            mListItemTextView.setText(items.get(position));
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            listener.onHistoryResultClick(clickedPosition);
        }
    }
}


