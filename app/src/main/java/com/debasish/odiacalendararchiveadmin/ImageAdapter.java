package com.debasish.odiacalendararchiveadmin;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.debasish.odiacalendararchiveadmin.model.Upload;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final List<Upload> uploads;
    private OnItemClickListener listener;

    public ImageAdapter(List<Upload> uploads) {
        this.uploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Upload uploadCurrent = uploads.get(position);
        holder.progressCircle.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(uploadCurrent.getImageUrl())
                .fit()
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressCircle.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //setup an interface for context menu for click and hold action
    public interface OnItemClickListener {
        void onShowDetails(int position);

        void onEditClick(int position);

        void onDeleteClick(int position);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        ImageView imageView;
        ProgressBar progressCircle;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view_upload);
            progressCircle = itemView.findViewById(R.id.progress_circular);

            itemView.setOnCreateContextMenuListener(this);
        }

        //fill the context menu on click and hold action
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem showDetails = menu.add(Menu.NONE, 1, 1, "Show Details");
            MenuItem edit = menu.add(Menu.NONE, 2, 2, "Edit");
            MenuItem delete = menu.add(Menu.NONE, 3, 3, "Delete");

            showDetails.setOnMenuItemClickListener(this);
            edit.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        //set up the context menu
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (item.getItemId()) {
                        case 1:
                            listener.onShowDetails(position);
                            return true;
                        case 2:
                            listener.onEditClick(position);
                            return true;
                        case 3:
                            listener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }
}