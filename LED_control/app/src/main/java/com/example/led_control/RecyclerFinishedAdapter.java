package com.example.led_control;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerFinishedAdapter extends RecyclerView.Adapter <RecyclerFinishedAdapter.ViewHolder>{

    List<String> finishedTasksList;
    List<Integer> tasksDurrs;

    public RecyclerFinishedAdapter(List<String> finishedTasksList,List<Integer> tasksDurrs) {
        this.finishedTasksList = finishedTasksList;
        this.tasksDurrs= tasksDurrs;
    }
    @NonNull
    @Override
    public RecyclerFinishedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater= LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item_finished,parent,false);

        RecyclerFinishedAdapter.ViewHolder viewHolder = new RecyclerFinishedAdapter.ViewHolder(view);



        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerFinishedAdapter.ViewHolder holder, int position) {

        holder.textViewTaskFinished.setText(finishedTasksList.get(position));

        holder.textViewDurr.setText(String.valueOf(tasksDurrs.get(position)));
        holder.textViewDurr.setVisibility(View.VISIBLE);
        //holder.textViewIdFinished.setText(String.valueOf(position+1));

    }

    @Override
    public int getItemCount() {
        return finishedTasksList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTaskFinished;
        FrameLayout frameTaskFinished;
        TextView textViewIdFinished;
        TextView textViewDurr;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskFinished = itemView.findViewById(R.id.textViewTaskFinished);
            //editTextTask = itemView.findViewById(R.id.edittextTask);
            frameTaskFinished = itemView.findViewById(R.id.frameTaskFinished);
            textViewIdFinished= itemView.findViewById(R.id.textViewIdFinished);
            textViewDurr= itemView.findViewById(R.id.textViewDur);



        }
    }
}
