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
        int i = holder.getAdapterPosition();
        final UriPathName uriPathName = uriPathNames[i];
        holder.getTextView().setText(uriPathName.getLabel());
        Button actionPlay = holder.getActionPlay();
        OnPlayClickListener listener = listeners[i];
        actionPlay.setText(listener.isPlaying() ? R.string.stop : R.string.play);
        listener.setActionPlay(actionPlay);
        actionPlay.setOnClickListener(groupListeners[i]);
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
