package com.example.softbank.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.softbank.activity.AppInfo;
import com.example.softbank.databinding.EijfddhgebcBinding;
import com.example.softbank.recycler.ItemListenter;

import java.util.List;

public class DangerousAdapter extends RecyclerView.Adapter<DangerousAdapter.ViewHolder> {
    private final List<AppInfo> appInfos;
    private final Context context;
    private final ItemListenter itemListenter;

    public DangerousAdapter(Context context, ItemListenter itemListenter, List<AppInfo> appInfos) {
        this.appInfos = appInfos;
        this.itemListenter = itemListenter;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(EijfddhgebcBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        itemListenter.Onclick(holder.binding.remove, position);
        itemListenter.Onclick(holder.binding.info, position);
        AppInfo appInfo = appInfos.get(position);
        holder.binding.name.setText(appInfo.getAppName());
    }


    @Override
    public int getItemCount() {
        return appInfos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final EijfddhgebcBinding binding;

        public ViewHolder(@NonNull EijfddhgebcBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
