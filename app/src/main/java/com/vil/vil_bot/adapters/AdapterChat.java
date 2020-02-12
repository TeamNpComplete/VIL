package com.vil.vil_bot.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.vil.vil_bot.R;
import com.vil.vil_bot.models.ModelMessage;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyViewHolder>{

    Context context;
    public ArrayList<ModelMessage> modelMessageArrayList;

    public AdapterChat(Context context, ArrayList<ModelMessage> modelMessageArrayList){
        this.context = context;
        this.modelMessageArrayList = modelMessageArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelMessage modelMessage = modelMessageArrayList.get(position);

        if(!modelMessage.getSenderName().equals("user")){
            holder.message.setTextColor(Color.parseColor("#ffffff"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.LEFT;
            float factor = holder.card.getContext().getResources().getDisplayMetrics().density;
            layoutParams.leftMargin = (int)(10 * factor);
            layoutParams.rightMargin = (int)(50 * factor);
            layoutParams.topMargin = (int)(8 * factor);
            layoutParams.bottomMargin = (int)(8 * factor);
            holder.card.setLayoutParams(layoutParams);
            Drawable drawable = context.getResources().getDrawable(R.drawable.blue_rectangle);
            holder.linearLayout.setBackground(drawable);
        } else {
            holder.message.setTextColor(Color.parseColor("#000000"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.RIGHT;
            float factor = holder.card.getContext().getResources().getDisplayMetrics().density;
            layoutParams.leftMargin = (int)(50 * factor);
            layoutParams.rightMargin = (int)(10 * factor);
            layoutParams.topMargin = (int)(8 * factor);
            layoutParams.bottomMargin = (int)(8 * factor);
            holder.card.setLayoutParams(layoutParams);
            Drawable drawable = context.getResources().getDrawable(R.drawable.white_rectangle);
            holder.linearLayout.setBackground(drawable);
        }

        holder.message.setText(modelMessage.getText());
    }

    @Override
    public int getItemCount() {
        return modelMessageArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView message;//, time;
        CardView card;
        LinearLayout linearLayout;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

            message = itemView.findViewById(R.id.chat_message_text);
            //time = itemView.findViewById(R.id.chat_message_time);
            card = itemView.findViewById(R.id.chat_msg_card);
            linearLayout = itemView.findViewById(R.id.chat_card_linear_layout);
        }
    }

    public void addItem(ModelMessage modelMessage){
        modelMessageArrayList.add(modelMessage);
        notifyDataSetChanged();
    }

}
