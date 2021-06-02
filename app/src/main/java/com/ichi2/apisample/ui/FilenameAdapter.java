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
    private final SoundPlayer soundPlayer;

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

    public FilenameAdapter(UriPathName[] uriPathNames, SoundPlayer soundPlayer) {
        this.uriPathNames = uriPathNames;
        this.soundPlayer = soundPlayer;
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
        final UriPathName uriPathName = uriPathNames[position];
        holder.getTextView().setText(uriPathName.label);
        holder.getActionPlay().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPlayer.play(uriPathName.uri, uriPathName.path);
            }
        });
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

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
