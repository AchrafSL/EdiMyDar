package com.example.edimydar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{

    List<message> msgList;
    public MessageAdapter(List<message> list) {
        msgList = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,null);
        return new MyViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        // Implementing the logic of everyElement in the recycler view :
        message msgE = msgList.get(position);
        if(msgE.getSendbyWho().equals(message.SENT_BY_USR))
        {
            holder.LeftChatView.setVisibility(View.GONE);
            holder.RightChatView.setVisibility(View.VISIBLE);
            holder.rightChatTextV.setText(msgE.getMsg());
        }else {
            holder.RightChatView.setVisibility(View.GONE);
            holder.LeftChatView.setVisibility(View.VISIBLE);
            holder.leftChatTextV.setText(msgE.getMsg());
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        LinearLayout LeftChatView,RightChatView;
        TextView leftChatTextV,rightChatTextV;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            LeftChatView = itemView.findViewById(R.id.leftChatView);
            RightChatView = itemView.findViewById(R.id.RightChatView);

            leftChatTextV = itemView.findViewById(R.id.response);
            rightChatTextV = itemView.findViewById(R.id.question);
        }
    }
}
