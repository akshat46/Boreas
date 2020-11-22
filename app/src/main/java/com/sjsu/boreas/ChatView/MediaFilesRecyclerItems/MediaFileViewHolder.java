package com.sjsu.boreas.ChatView.MediaFilesRecyclerItems;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.R;

public class MediaFileViewHolder extends RecyclerView.ViewHolder {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------MesssageViewHolder-- ";

//    public TextView msg_txt;
    public View rootView;
    public ImageView img_view;

    public MediaFileViewHolder(View v) {
        super(v);
        Log.e(TAG, SUB_TAG+"Inside message view holder");
        this.rootView = v;
        this.img_view = (ImageView) v.findViewById(R.id.thumbNail);
    }

    public void bindToListItemView(FileItem file){
        Log.e(TAG, SUB_TAG+"Binding");
        this.img_view.setImageBitmap(file.pic);
    }
}

