package com.vil.vil_bot.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.vil.vil_bot.MainActivity;
import com.vil.vil_bot.R;
import com.vil.vil_bot.models.ModelMessage;
import com.vil.vil_bot.models.RechargeDetails;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyViewHolder>{

    private Context context;
    private ArrayList<ModelMessage> modelMessageArrayList;
    private RecyclerView recyclerView;
    private RecyclerView.RecycledViewPool recycledViewPool;
    private AdapterCard adapterCard;

    public AdapterChat(Context context, ArrayList<ModelMessage> modelMessageArrayList, RecyclerView recyclerView){
        this.context = context;
        this.modelMessageArrayList = modelMessageArrayList;
        recycledViewPool = new RecyclerView.RecycledViewPool();
        this.recyclerView = recyclerView;
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
            // message by bot
            holder.message.setTextColor(Color.parseColor("#000000"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.LEFT;
            float factor = holder.card.getContext().getResources().getDisplayMetrics().density;
            layoutParams.leftMargin = (int)(10 * factor);
            layoutParams.rightMargin = (int)(50 * factor);
            layoutParams.topMargin = (int)(8 * factor);
            layoutParams.bottomMargin = (int)(8 * factor);
            holder.card.setLayoutParams(layoutParams);
            Drawable drawable = context.getResources().getDrawable(R.drawable.white_rectangle);
            holder.linearLayout.setBackground(drawable);

            ArrayList<RechargeDetails> rechargeDetailsArrayList = new ArrayList<>();
            if(modelMessage.getIntent().equals("recharge.phone.upgrade")) {
                SQLiteDatabase database = context.openOrCreateDatabase("TeleData",0, null);
                Cursor c = database.rawQuery("SELECT * FROM Unlimited", null);
                c.moveToFirst();
                do {
                    RechargeDetails rechargeDetails = new RechargeDetails();
                    rechargeDetails.setPrice(c.getInt(c.getColumnIndex("cost")));
                    rechargeDetails.setRechargeValidity(c.getString(c.getColumnIndex("validity")));
                    rechargeDetails.setRechargeUsage(c.getString(c.getColumnIndex("data")));
                    /*Log.d("Checking", "In Adapter Chat");
                    Log.e("Cost", c.getString(c.getColumnIndex("cost")));
                    Log.e("Validity", c.getString(c.getColumnIndex("validity")));
                    Log.e("NOS", c.getString(c.getColumnIndex("no_of_sms")));*/
                    rechargeDetailsArrayList.add(rechargeDetails);
                }while(c.moveToNext());
                c.close();

            }
            adapterCard = new AdapterCard(rechargeDetailsArrayList, modelMessageArrayList, "bot", recyclerView);
            holder.recyclerViewBill.setAdapter(adapterCard);
            holder.recyclerViewBill.setRecycledViewPool(recycledViewPool);

        }
        else {
            // message by user
            holder.message.setTextColor(Color.parseColor("#ffffff"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.RIGHT;
            float factor = holder.card.getContext().getResources().getDisplayMetrics().density;
            layoutParams.leftMargin = (int)(50 * factor);
            layoutParams.rightMargin = (int)(10 * factor);
            layoutParams.topMargin = (int)(8 * factor);
            layoutParams.bottomMargin = (int)(8 * factor);
            holder.card.setLayoutParams(layoutParams);
            Drawable drawable = context.getResources().getDrawable(R.drawable.blue_rectangle);
            holder.linearLayout.setBackground(drawable);
        }

        holder.message.setText(modelMessage.getText());
    }

    @Override
    public int getItemCount() {
        return modelMessageArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView message;
        CardView card;
        LinearLayout linearLayout;
        RecyclerView recyclerViewBill;
        LinearLayoutManager verticalManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

        private MyViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.chat_message_text);
            card = itemView.findViewById(R.id.chat_msg_card);
            linearLayout = itemView.findViewById(R.id.chat_card_linear_layout);

            recyclerViewBill = itemView.findViewById(R.id.plansList);
            recyclerViewBill.setHasFixedSize(true);
            recyclerViewBill.setNestedScrollingEnabled(false);
            recyclerViewBill.setLayoutManager(verticalManager);
            recyclerViewBill.setItemAnimator(new DefaultItemAnimator());
        }
    }

    public void addItem(ModelMessage modelMessage){
        modelMessageArrayList.add(modelMessage);
        notifyDataSetChanged();
    }

}
