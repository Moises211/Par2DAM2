package com.example.reusisam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reusisam.DTOS.ProductsDTO;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductsDTO> productList;
    private OnProductListener listener;

    public interface OnProductListener {
        void onEditClick(ProductsDTO product);
        void onDeleteClick(ProductsDTO product);
    }

    public ProductAdapter(List<ProductsDTO> productList, OnProductListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductsDTO product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvCode.setText("Código: " + product.getCode());

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNombreProducto);
            tvCode = itemView.findViewById(R.id.tvCodigoProducto);
            btnEdit = itemView.findViewById(R.id.btnEditar);
            btnDelete = itemView.findViewById(R.id.btnEliminar);
        }
    }
}