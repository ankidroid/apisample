package com.ichi2.apisample.ui;

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
    private final UriPathName[] uriPathNames;
    private final OnPlayClickListener[] listeners;
    private final OnGroupPlayClickListener[] groupListeners;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textFilename;
        private final Button actionPlay;

        public ViewHolder(View view) {
            super(view);

            textFilename = view.findViewById(R.id.textFilename);
            actionPlay = view.findViewById(R.id.actionPlay);
        }

        public TextView getTextFilename() {
            return textFilename;
        }

        public Button getActionPlay() {
            return actionPlay;
        }
    }

    public FilenameAdapter(MainActivity mainActivity, UriPathName[] uriPathNames, OnPlayAllClickListener allListener) {
        this.uriPathNames = uriPathNames;
        int nFilenames = uriPathNames.length;
        listeners = new OnPlayClickListener[nFilenames];
        groupListeners = new OnGroupPlayClickListener[nFilenames];
        for (int i = 0; i < nFilenames; i++) {
            OnPlayClickListener listener = new OnPlayClickListener(mainActivity, uriPathNames[i], null);
            listeners[i] = listener;
            OnGroupPlayClickListener groupListener = new OnGroupPlayClickListener(listener, listeners, allListener);
            groupListeners[i] = groupListener;
        }
    }

    public OnPlayClickListener[] getListeners() {
        return listeners;
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
        UriPathName uriPathName = uriPathNames[position];
        String label = uriPathName.getLabel();
        TextView textFilename = holder.getTextFilename();
        textFilename.setText(label);

        Button actionPlay = holder.getActionPlay();
        actionPlay.setOnClickListener(groupListeners[position]);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        int position = holder.getAdapterPosition();
        OnPlayClickListener listener = listeners[position];
        Button actionPlay = holder.getActionPlay();
        listener.setActionPlay(actionPlay);
        actionPlay.setText(listener.isPlaying() ? R.string.stop : R.string.play);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        int position = holder.getAdapterPosition();
        listeners[position].setActionPlay(null);
    }

    @Override
    public int getItemCount() {
        return uriPathNames.length;
    }

    public static class UriPathName {
        private final Uri uri;
        private final String path;
        private final String name;
        private String label;

        public UriPathName(Uri uri, String path, String name, String label) {
            this.uri = uri;
            this.path = path;
            this.name = name;
            this.label = label;
        }

        public Uri getUri() {
            return uri;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
