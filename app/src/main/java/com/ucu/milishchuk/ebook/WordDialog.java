package com.ucu.milishchuk.ebook;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordDialog extends DialogFragment {

    TextView wordView;
    TextView categView;
    RecyclerView rv_word;
    LinearLayoutManager llm;
    RVWordAdapter adapter;
    LinearLayout fl;
    ProgressBar pb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.word_dialog, container, false);
        final String word = getArguments().getString("Word");
        final String sentence = getArguments().getString("Sentence");
        final String word_order = "0";

        rv_word = (RecyclerView) v.findViewById(R.id.rv_word);
        fl = (LinearLayout) v.findViewById(R.id.word_def_group);
        pb = (ProgressBar) v.findViewById(R.id.word_pb);
        wordView = (TextView) v.findViewById(R.id.word);
        categView = (TextView) v.findViewById(R.id.category);

        fl.setVisibility(View.GONE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        llm = new LinearLayoutManager(getContext());
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
        itemDecorator.setDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.divider, null));
        rv_word.addItemDecoration(itemDecorator);
        rv_word.setLayoutManager(llm);
        final List<WordCard> wordCards = new ArrayList<>();
        adapter = new RVWordAdapter(wordCards);
        rv_word.setAdapter(adapter);

        String url = "http://momka45.pythonanywhere.com/";
        Log.i("epublib", "start request");
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject("{\"word\":\"" + word + "\", \"sentence\":\"" + sentence + "\", \"word_order\":\"" + word_order + "\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("epublib", response.toString());
                try {
                    String lemmatized_word = response.getString("word");
                    String category = response.getJSONObject("senses").getJSONObject("word_sense").getString("category");
                    Log.i("epublib", "find definitions");
                    JSONArray definitions = response.getJSONObject("senses").getJSONObject("word_sense").getJSONArray("definitions");
                    String definitions_str = "";
                    for (int i = 0; i < definitions.length(); i++) {
                        String definition = definitions.optJSONObject(i).getString("definition");
                        String example = "";
                        try {
                            example = definitions.optJSONObject(i).getString("example");
                        } catch (JSONException e) {}
                        wordCards.add(new WordCard(definition, example));
                        adapter.notifyItemChanged(wordCards.size()-1);
                        definitions_str += definition + "\n\n";
                    }
                    wordView.setText(Character.toUpperCase(lemmatized_word.charAt(0)) + lemmatized_word.substring(1));
                    categView.setText(category);
                    pb.setVisibility(View.GONE);
                    fl.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("epublib", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("word", word);
                params.put("sentence", sentence);
                params.put("word_order", "0");

                return params;
            }
        };
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonRequest);
        return v;
    }


}

class WordCard {
    String defin;
    String example;

    WordCard(String defin, String example) {
        this.defin = defin;
        this.example = example;
    }
}

class RVWordAdapter extends RecyclerView.Adapter<RVWordAdapter.WordViewHolder> {

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView def_tv;
        TextView exam_tv;

        WordViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv_word);
            def_tv = (TextView) itemView.findViewById(R.id.definition);
            exam_tv = (TextView) itemView.findViewById(R.id.example);
        }
    }

    private List<WordCard> wordCards;

    RVWordAdapter(List<WordCard> wordCards) {
        this.wordCards = wordCards;
    }
    @NonNull
    @Override

    public RVWordAdapter.WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_card, parent, false);
        return new WordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RVWordAdapter.WordViewHolder holder, int position) {
        holder.def_tv.setText(wordCards.get(position).defin);
        holder.exam_tv.setText(wordCards.get(position).example);
    }

    @Override
    public int getItemCount() {
        return wordCards.size();
    }
}

