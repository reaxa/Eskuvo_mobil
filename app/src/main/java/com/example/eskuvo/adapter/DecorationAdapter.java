package com.example.eskuvo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eskuvo.R;
import com.example.eskuvo.model.Decoration;
import com.example.eskuvo.service.CartManager;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class DecorationAdapter extends RecyclerView.Adapter<DecorationAdapter.ViewHolder> {

    private List<Decoration> decorationList;
    private Context context;
    private FirebaseUser currentUser;

    public DecorationAdapter(Context context, List<Decoration> decorationList, FirebaseUser currentUser) {
        this.context = context;
        this.decorationList = decorationList;
        this.currentUser = currentUser;
    }

    @Override
    public DecorationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.decoration_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DecorationAdapter.ViewHolder holder, int position) {
        Decoration decoration = decorationList.get(position);
        holder.name.setText(decoration.getName());
        holder.description.setText(decoration.getDescription());
        holder.price.setText(decoration.getPrice() + " Ft");

        Glide.with(context)
                .load(decoration.getImageUrl()) // fontos: legyen imageUrl az objektumban
                .placeholder(R.drawable.placeholder)
                .into(holder.image);



        // Kosárba rakás gomb csak akkor, ha bejelentkezett és nem anonim
        if (currentUser != null && !currentUser.isAnonymous()) {
            holder.buttonAddToCart.setVisibility(View.VISIBLE);
            holder.buttonAddToCart.setOnClickListener(v -> {
                CartManager.getInstance().addToCart(decoration);
                Toast.makeText(context, "Hozzáadva a kosárhoz", Toast.LENGTH_SHORT).show();
            });
        } else {
            holder.buttonAddToCart.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return decorationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, price;
        ImageView image;
        ImageButton buttonAddToCart;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewName);
            description = itemView.findViewById(R.id.textViewDescription);
            price = itemView.findViewById(R.id.textViewPrice);
            image = itemView.findViewById(R.id.imageViewDecoration);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
        }
    }
}
