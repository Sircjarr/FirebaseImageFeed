package com.example.cliff.firebaseimagefeed.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.cliff.firebaseimagefeed.Model.UserPreview;
import com.example.cliff.firebaseimagefeed.R;

import java.util.List;

public class PreviewListAdapter extends ArrayAdapter<UserPreview> {

    private static final String TAG = "CustomListAdapter";

    private Context mContext;
    private int mResource;

    public PreviewListAdapter(Context context, int resource, List<UserPreview> objects) {
        super(context, resource, objects);

        // MainActivity is passed into this context
        mContext = context;
        mResource = resource;
    }

    // The ViewHolder will hold all the views in each list item
    static class ViewHolder {
        TextView tvUsername;
        ImageView ivUserImage;
        ProgressBar progressBar;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        // Get person's information
        String username = getItem(position).getUsername();
        String imgURL = getItem(position).getProfileImageURL();

        // Best design pattern for ListView
        if (convertView == null) {

            // Inflate the view
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            // Get the views with ViewHolder
            holder = new ViewHolder();
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.previewProgressDialog);
            holder.tvUsername = (TextView) convertView.findViewById(R.id.tvUsername);
            holder.ivUserImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Retrieve the placeholder image
        int placeholder = mContext.getResources().getIdentifier("@drawable/default_image", null, mContext.getPackageName());

        // Set views
        holder.tvUsername.setText(username);
        // Use Glide to load the image
        if (imgURL.equals("none")) {
            holder.ivUserImage.setImageResource(placeholder);
            holder.progressBar.setVisibility(View.GONE);
        }
        else {
            Glide.with(mContext)
                    .load(imgURL)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .dontAnimate() // Must have for the CircleImageView to work
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false; // important to return false so the error placeholder can be placed
                        }
                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.ivUserImage);
        }
        return convertView;
    }
}
