package com.example.eskuvo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eskuvo.R;
import com.example.eskuvo.model.Order;

import java.util.Date;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderTotal.setText("Összeg: " + order.getTotalPrice() + " Ft");
        holder.orderDate.setText("Dátum: " + new Date(order.getTimestamp()).toString());

        if (position == 0) {
            Animation fadeIn = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
            holder.itemView.startAnimation(fadeIn);
        } else {
            Animation slideIn = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_in_left);
            holder.itemView.startAnimation(slideIn);
        }  }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderTotal, orderDate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            orderDate = itemView.findViewById(R.id.orderDate);
        }
    }
}
