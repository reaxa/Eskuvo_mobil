package com.example.eskuvo.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eskuvo.R;
import com.example.eskuvo.model.CartItem;

import java.util.List;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private Context context;

    public BasketAdapter(List<CartItem> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;
    }

    @Override
    public BasketAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }
    public interface QuantityChangeListener {
        void onQuantityChanged();
    }

    // 2. Listener változó
    private QuantityChangeListener quantityChangeListener;

    // 3. Setter metódus a listener beállításához
    public void setQuantityChangeListener(QuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

    @Override
    public void onBindViewHolder(BasketAdapter.ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.name.setText(item.getDecoration().getName());
        holder.quantity.setText("Mennyiség: " + item.getQuantity());
        holder.price.setText(String.format("%.2f Ft", item.getDecoration().getPrice() * item.getQuantity()));




        holder.buttonIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            // Frissíthetjük az összárat, ha van callback vagy más megoldás
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChanged();
            }
        });

        holder.buttonDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, price;
        ImageButton buttonIncrease, buttonDecrease;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewCartName);
            quantity = itemView.findViewById(R.id.textViewCartQuantity);
            price = itemView.findViewById(R.id.textViewCartPrice);

            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
        }
    }
}
