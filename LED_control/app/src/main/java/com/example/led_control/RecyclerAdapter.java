package com.example.led_control;




import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class RecyclerAdapter extends RecyclerView.Adapter <RecyclerAdapter.ViewHolder>{


    List<String> tasksList;

    private Context mContext;

    public RecyclerAdapter(List<String> tasksList) {
        this.tasksList = tasksList;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        LayoutInflater layoutInflater= LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item,parent,false);

        ViewHolder viewHolder = new ViewHolder(view);



        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        holder.textViewTask.setText(tasksList.get(position));
        holder.editTextTask.requestFocus();

        if (tasksList.get(position) == "wtf"){

        holder.editTextTask.setVisibility(View.VISIBLE);
        holder.textViewTask.setVisibility(View.GONE);
        holder.buttonTask.setVisibility(View.VISIBLE);
        holder.imageViewTask.setVisibility(View.GONE);

        holder.editTextTask.setText("");

        holder.frameTask.setBackgroundColor(Color.parseColor("#BDBDBD"));

        }







    }

    @Override
    public int getItemCount() {

        return tasksList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView textViewTask;
        EditText editTextTask;
        FrameLayout frameTask;
        Button buttonTask;
        ImageView imageViewTask;




        public ViewHolder(@NonNull final View itemView) {
            super(itemView);



            textViewTask = itemView.findViewById(R.id.textViewTask);
            editTextTask = itemView.findViewById(R.id.edittextTask);
            frameTask = itemView.findViewById(R.id.frameTask);
            buttonTask = itemView.findViewById(R.id.buttonTask);
            imageViewTask = itemView.findViewById(R.id.imageViewTask);



            itemView.setOnClickListener(this);


//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//
//
//                    editTextTask.setVisibility(View.VISIBLE);
//                    textViewTask.setVisibility(View.GONE);
//                    buttonTask.setVisibility(View.VISIBLE);
//                    imageViewTask.setVisibility(View.GONE);
//
//                    frameTask.setBackgroundColor(Color.parseColor("#1B732A"));
//
//
//
//
//
//                    return true;
//                }
//            });



            editTextTask.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String taskText = editTextTask.getText().toString();
                    if (!taskText.isEmpty()){
                        frameTask.setBackgroundColor(Color.parseColor("#1B732A"));
                        buttonTask.setEnabled(true);


                    }
                    else{
                        frameTask.setBackgroundColor(Color.parseColor("#BDBDBD"));

                        buttonTask.setEnabled(false);


                    }
                }

                @Override
                public void afterTextChanged(Editable s) {


                }
            });

            buttonTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextTask.setVisibility(View.GONE);
                    textViewTask.setText(editTextTask.getText().toString());
                    textViewTask.setVisibility(View.VISIBLE);
                    buttonTask.setVisibility(View.GONE);
                    imageViewTask.setVisibility(View.VISIBLE);

                    tasksList.set(getAdapterPosition(), editTextTask.getText().toString());


                    frameTask.setBackgroundColor(Color.parseColor("#CFCFCF"));




                }
            });







        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(),String.valueOf(getAdapterPosition()),Toast.LENGTH_SHORT).show();
        }
    }
}
