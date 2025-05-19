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
import com.example.eskuvo.model.CartItem;
import com.example.eskuvo.model.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // Rendelés ID megjelenítése
        if (order.getOrderId() != null && !order.getOrderId().isEmpty()) {
            holder.orderIdTextView.setText("Rendelés ID: " + order.getOrderId());
        } else {
            holder.orderIdTextView.setText("Rendelés ID: Nem elérhető"); // Vagy más alapértelmezett, pl. index
        }

        // Összeg és dátum megjelenítése
        holder.orderTotal.setText(String.format(Locale.getDefault(), "Összeg: %.2f Ft", order.getTotalPrice()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(order.getTimestamp()));
        holder.orderDate.setText("Dátum: " + formattedDate);

        // Rendelt tételek megjelenítése a CartItem és Decoration modellek alapján
        StringBuilder itemsBuilder = new StringBuilder("Tételek:\n");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (CartItem item : order.getItems()) {
                if (item.getDecoration() != null) {
                    itemsBuilder.append("- ").append(item.getDecoration().getName())
                            .append(" (").append(item.getQuantity())
                            .append(" db)");
                    if (item.getDecoration().getPrice() > 0) {
                        itemsBuilder.append(" - ").append(String.format(Locale.getDefault(), "%.2f Ft/db", item.getDecoration().getPrice()));
                    }
                    itemsBuilder.append("\n");
                }
            }
        } else {
            itemsBuilder.append("Nincs tétel.");
        }
        holder.orderItemsTextView.setText(itemsBuilder.toString());

        // Animációk
        if (position == 0) {
            Animation fadeIn = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
            holder.itemView.startAnimation(fadeIn);
        } else {
            Animation slideIn = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_in_left);
            holder.itemView.startAnimation(slideIn);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderTotal, orderDate, orderItemsTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderItemsTextView = itemView.findViewById(R.id.orderItemsTextView);
        }
    }
}