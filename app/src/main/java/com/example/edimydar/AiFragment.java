package com.example.edimydar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class AiFragment extends Fragment {
    RecyclerView recyclerView;
    TextView welcomeTxt,msgTxt;
    ImageView sendBnt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        // references :
        recyclerView = view.findViewById(R.id.recyclerView);
        welcomeTxt = view.findViewById(R.id.welcomeTxtAI);
        msgTxt = view.findViewById(R.id.msgTxt);
        sendBnt = view.findViewById(R.id.sendBtn);



        return view;
    }
}