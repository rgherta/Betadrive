package com.ride.betadrive.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ride.betadrive.DataModels.MessageContract;
import com.ride.betadrive.R;

import java.util.ArrayList;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.MyViewHolder> {
    private ArrayList<MessageContract> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ConstraintLayout leftMsgLayout;
        public ConstraintLayout rightMsgLayout;
        public TextView leftMsgTextView;
        public TextView rightMsgTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            if(itemView!=null) {
                leftMsgLayout = itemView.findViewById(R.id.left_chat);
                rightMsgLayout = itemView.findViewById(R.id.right_chat);
                leftMsgTextView = itemView.findViewById(R.id.left_message);
                rightMsgTextView = itemView.findViewById(R.id.right_message);
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatMessagesAdapter(ArrayList<MessageContract>  myDataset) {
        this.mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatMessagesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.message_item, parent, false);
        return new MyViewHolder(view);


    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        MessageContract mMessage = mDataset.get(position);

        holder.rightMsgLayout.setVisibility(View.VISIBLE);
        holder.leftMsgLayout.setVisibility(View.GONE);
        holder.rightMsgTextView.setText(mMessage.getMessage());


//        // If the message is a received message.
//        if(msgDto.MSG_TYPE_RECEIVED.equals(msgDto.getMsgType()))
//        {
//            // Show received message in left linearlayout.
//            holder.leftMsgLayout.setVisibility(LinearLayout.VISIBLE);
//            holder.leftMsgTextView.setText(msgDto.getMsgContent());
//            // Remove left linearlayout.The value should be GONE, can not be INVISIBLE
//            // Otherwise each iteview's distance is too big.
//            holder.rightMsgLayout.setVisibility(LinearLayout.GONE);
//        }
//        // If the message is a sent message.
//        else if(msgDto.MSG_TYPE_SENT.equals(msgDto.getMsgType()))
//        {
//            // Show sent message in right linearlayout.
//            holder.rightMsgLayout.setVisibility(LinearLayout.VISIBLE);
//            holder.rightMsgTextView.setText(msgDto.getMsgContent());
//            // Remove left linearlayout.The value should be GONE, can not be INVISIBLE
//            // Otherwise each iteview's distance is too big.
//            holder.leftMsgLayout.setVisibility(LinearLayout.GONE);
//        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(mDataset != null){
            return mDataset.size();
        } else {
            return 0;
        }

    }
}