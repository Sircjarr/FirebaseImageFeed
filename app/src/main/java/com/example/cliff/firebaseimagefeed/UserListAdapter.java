package com.example.cliff.firebaseimagefeed;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.cliff.firebaseimagefeed.Model.UserImage;

import java.util.List;

class UserListAdapter extends ArrayAdapter<UserImage> {

    private static final String TAG = "CustomListAdapter";

    private Context mContext;
    private int mResource;

    public UserListAdapter(Context context, int resource, List<UserImage> objects) {
        super(context, resource, objects);

        // MainActivity is passed into this context
        mContext = context;
        mResource = resource;
    }

    // The ViewHolder will hold all the views in each list item
    static class ViewHolder {
        ImageView userImage;
        ProgressBar progressBar;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        // Get person's information
        String imgURL = getItem(position).getUserImageURL();

        // Best design pattern for listView
        if (convertView == null) {

            // Inflate the view
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            // Get the views with ViewHolder
            holder = new ViewHolder();
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.cardProgressDialog);
            holder.userImage = (ImageView) convertView.findViewById(R.id.ivUserImage);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Retrieve the placeholder image
        int placeholder = mContext.getResources().getIdentifier("@drawable/default_image", null, mContext.getPackageName());

        // Use Glide to load the image
        if (imgURL == null) {
            holder.userImage.setImageResource(placeholder);
            holder.progressBar.setVisibility(View.GONE);
        }
        else {
            Glide.with(mContext)
                .load(imgURL)
                .placeholder(placeholder)
                .error(placeholder)
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
                .into(holder.userImage);
        }
        return convertView;
    }
}