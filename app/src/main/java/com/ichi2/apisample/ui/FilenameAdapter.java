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

    private final UriName[] uriNames;

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

    public FilenameAdapter(Context context, UriName[] uriNames) {
        this.context = context;
        this.uriNames = uriNames;
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
        holder.getTextView().setText(uriNames[position].name);
        holder.getActionPlay().setOnClickListener(new OnPlayClickListener(context, uriNames[position].uri));
    }

    @Override
    public int getItemCount() {
        return uriNames.length;
    }

    public static class UriName {
        private final Uri uri;
        private final String name;

        public UriName(Uri uri, String name) {
            this.uri = uri;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Uri getUri() {
            return uri;
        }
    }
}
