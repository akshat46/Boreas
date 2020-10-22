package com.sjsu.boreas.ChatViewRelatedStuff.MessageListViewStuff;

import android.util.Log;
import android.view.View;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.R;

public class OneOnOneMessageViewHolder extends RecyclerView.ViewHolder {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------MesssageViewHolder-- ";

    public TextView msg_txt;
    public View rootView;

    public OneOnOneMessageViewHolder(View v) {
        super(v);
        Log.e(TAG, SUB_TAG+"Inside message view holder");
        this.rootView = v;
        this.msg_txt = (TextView) v.findViewById(R.id.txt_msg);
    }

    public void bindToListItemView(ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"Binding");
        this.msg_txt.setText(mssg.mssgText);
    }
}
