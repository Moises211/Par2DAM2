package com.example.reusisam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {
    public interface Listener {
        void onEdit(CategoriaDTO categoria);
        void onDelete(CategoriaDTO categoria);
    }

    private final List<CategoriaDTO> categorias;
    private final Listener listener;

    public CategoriaAdapter(List<CategoriaDTO> categorias, Listener listener) {
        this.categorias = categorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoriaDTO categoria = categorias.get(position);
        holder.nombre.setText(categoria.getNombre());
        holder.editar.setOnClickListener(v -> listener.onEdit(categoria));
        holder.eliminar.setOnClickListener(v -> listener.onDelete(categoria));
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        Button editar;
        Button eliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tvNombreCategoria);
            editar = itemView.findViewById(R.id.btnEditarCategoria);
            eliminar = itemView.findViewById(R.id.btnEliminarCategoria);
        }
    }
}
