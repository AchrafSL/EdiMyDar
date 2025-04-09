package com.example.edimydar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiFragment extends Fragment {
    RecyclerView recyclerView;
    TextView welcomeTxt, msgTxt;
    ImageView sendBnt;
    List<message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    public static final MediaType JSON = MediaType.get("application/json");
    OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        welcomeTxt = view.findViewById(R.id.welcomeTxtAI);
        msgTxt = view.findViewById(R.id.msgTxt);
        sendBnt = view.findViewById(R.id.sendBtn);

        sendBnt.setOnClickListener(v -> {
            String msg = msgTxt.getText().toString().trim();
            if (!msg.isEmpty()) {
                addToLocalChat(msg, message.SENT_BY_USR);
                msgTxt.setText("");
                CallAPI(msg);
                welcomeTxt.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "Message cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        return view;
    }

    public void addToLocalChat(String msg, String sendBy) {
        message msgE = new message(msg, sendBy);
        messageList.add(msgE);
        messageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    public void addLocalResponce(String response) {
       messageList.remove(messageList.size()-1);
        requireActivity().runOnUiThread(() -> addToLocalChat(response, message.SENT_BY_BOT));
    }

    public void CallAPI(String question) {
        messageList.add(new message("Typing ...",message.SENT_BY_BOT));


        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("contents", new JSONArray()
                    .put(new JSONObject()
                            .put("parts", new JSONArray()
                                    .put(new JSONObject()
                                            .put("text", question)))));

        } catch (JSONException e) {
            Log.e("JSONError", "Failed to create JSON body: " + e.getMessage());
            addLocalResponce("Failed to process request due to JSON error.");
            return;
        }

        String apiKey = BuildConfig.GEMINI_API_KEY; // Ensure this is set in your BuildConfig


        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("APIError", "Request failed: " + e.getMessage());
                addLocalResponce("Failed to load response due to network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("APIResponse", "Raw response: " + responseBody);

                        JSONObject jsonObject = new JSONObject(responseBody);

                        JSONArray candidates = jsonObject.getJSONArray("candidates");
                        JSONObject candidate = candidates.getJSONObject(0);

                        JSONObject content = candidate.getJSONObject("content");

                        JSONArray parts = content.getJSONArray("parts");

                        JSONObject part = parts.getJSONObject(0);

                        String result = part.getString("text").trim();
                        addLocalResponce(result);



                    } catch (JSONException e) {
                        Log.e("JSONParseError", "Failed to parse API response: " + e.getMessage());
                        addLocalResponce("Failed to parse response due to JSON error.");
                    }


                } else {

                    if (response.code() == 429) {
                        addLocalResponce("Too many requests. Please try again later.");
                    } else {
                        addLocalResponce("Failed to load response due to server error: " + response.message());
                    }
                }
            }
        });
    }
}