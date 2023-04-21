package com.example.softbank.recycler;

import android.view.View;

public interface ItemListenter {
    void onItemClick(int id, int position);

    default void Onclick(View view, int position) {
        view.setOnClickListener(v -> onItemClick(v.getId(), position));
    }
}
