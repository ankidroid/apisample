package com.ichi2.apisample.ui;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ichi2.apisample.R;

public class FilenameAdapter extends RecyclerView.Adapter<FilenameAdapter.ViewHolder> {
    private final Context context;

    private final String[] names;
    private final Uri[] uris;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final Button actionPlay;

        public ViewHolder(View view) {
            super(view);

            textView = view.findViewById(R.id.textFilename);
            actionPlay = view.findViewById(R.id.actionPlay);
        }

        public TextView getTextView() {
            return textView;
        }

        public Button getActionPlay() {
            return actionPlay;
        }
    }

    public FilenameAdapter(Context context, String[] names, Uri[] uris) {
        this.context = context;
        this.names = names;
        this.uris = uris;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_sound, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTextView().setText(names[position]);
        holder.getActionPlay().setOnClickListener(new OnPlayClickListener(context, uris[position]));
    }

    @Override
    public int getItemCount() {
        return names.length;
    }
}
