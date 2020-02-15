package com.vil.vil_bot.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vil.vil_bot.MainActivity;
import com.vil.vil_bot.R;
import com.vil.vil_bot.models.ModelMessage;
import com.vil.vil_bot.models.RechargeDetails;

import java.util.ArrayList;

public class AdapterCard extends RecyclerView.Adapter<AdapterCard.MyViewHolder> {

    private String sender;
    private ArrayList<RechargeDetails> rechargeDetailsArrayList;
    private ArrayList<ModelMessage> modelMessageArrayList;
    private RecyclerView recyclerView;

    public AdapterCard() {

    }

    public AdapterCard(ArrayList<RechargeDetails> rechargeDetailsArrayList, ArrayList<ModelMessage> modelMessageArrayList, String sender, RecyclerView recyclerView) {
        this.sender = sender;
        this.modelMessageArrayList = modelMessageArrayList;
        this.rechargeDetailsArrayList = rechargeDetailsArrayList;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public AdapterCard.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recharge_details_card, parent, false);
        if(rechargeDetailsArrayList.size()>0){
            if(!rechargeDetailsArrayList.get(0).getRechargeType().equals("Calls")){
                view.findViewById(R.id.rechargeUsage).setVisibility(view.GONE);
            }
        }
//        if(((TextView) view.findViewById(R.id.recharge_usage)).getText().toString().equals("0")) {
//            view.findViewById(R.id.rechargeUsage).setVisibility(view.GONE);
//        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String price = ((TextView) view.findViewById(R.id.recharge_amount)).getText().toString();
//                Log.e("onClick", "Card Clicked " + price);
                modelMessageArrayList.add(new ModelMessage("Confirm recharge of " + price + "?", "", "bot"));
                notifyDataSetChanged();
                recyclerView.scrollToPosition(modelMessageArrayList.size()-1);
            }
        });
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCard.MyViewHolder holder, int position) {
        RechargeDetails rechargeDetails = rechargeDetailsArrayList.get(position);

        holder.rechargeAmount.setText("₹" + rechargeDetails.getPrice());
        holder.rechargeType.setText(rechargeDetails.getRechargeType());
        holder.rechargeLimit.setText(rechargeDetails.getRechargeLimit());
        holder.rechargeUsage.setText(rechargeDetails.getRechargeUsage());
        holder.rechargeValidity.setText(rechargeDetails.getRechargeValidity());
    }

    @Override
    public int getItemCount() {
        if(!sender.equals("bot"))
            return 0;
        return (rechargeDetailsArrayList.size()>4)?4:rechargeDetailsArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView rechargeAmount, rechargeType, rechargeLimit, rechargeUsage, rechargeValidity;

        private MyViewHolder(@NonNull View itemView) {
            super(itemView);

            rechargeAmount = itemView.findViewById(R.id.recharge_amount);
            rechargeType = itemView.findViewById(R.id.recharge_type);
            rechargeLimit = itemView.findViewById(R.id.recharge_limit);
            rechargeUsage = itemView.findViewById(R.id.recharge_usage);
            rechargeValidity = itemView.findViewById(R.id.recharge_validity);
        }
    }
}
