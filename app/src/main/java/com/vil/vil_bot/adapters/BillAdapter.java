package com.vil.vil_bot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vil.vil_bot.R;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.MyViewHolder> {


    @NonNull
    @Override
    public BillAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recharge_details_card, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BillAdapter.MyViewHolder holder, int position) {
//        holder.rechargeValidity.setText("ahfa");
//        holder.rechargeType.setText("Unlimited");
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView rechargeAmnt, rechargeType, rechargeUsage, rechargeValidity;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

          rechargeAmnt = itemView.findViewById(R.id.recharge_amount);
          rechargeType = itemView.findViewById(R.id.recharge_type);
          rechargeUsage = itemView.findViewById(R.id.recharge_usage);
          rechargeValidity = itemView.findViewById(R.id.recharge_validity);
        }
    }
}
