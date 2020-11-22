package com.sjsu.boreas.ChatView.MediaFilesRecyclerItems;

import android.graphics.Bitmap;
import android.net.Uri;

public class FileItem {
//    public Uri picUri;
    public Bitmap pic;
//    public String date;

    public FileItem(Bitmap pic){
        this.pic = pic;
    }
}
