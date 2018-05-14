package com.ucu.milishchuk.ebook;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.HashMap;
import java.util.Map;

public class WordDialog extends DialogFragment {

    TextView wordView;
    TextView definitionView;
    TextView exampleView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.word_dialog, null);
        final String word = getArguments().getString("Word");
        final String sentence = getArguments().getString("Sentence");
        final String word_order = "0";
        wordView = (TextView) v.findViewById(R.id.word);
        definitionView = (TextView) v.findViewById(R.id.definition);
        exampleView = (TextView) v.findViewById(R.id.example);

        String url = "http://momka45.pythonanywhere.com/";
        Log.i("epublib", "start request");
        RequestQueue  queue = Volley.newRequestQueue(this.getContext());
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject("{\"word\":\"" + word + "\", \"sentence\":\"" + sentence + "\", \"word_order\":\""+ word_order+"\"}");
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
                    for(int i = 0; i < definitions.length(); i++) {
                        String definition = definitions.optJSONObject(i).getString("definition");
//                        String example = definitions.optJSONObject(i).getString("example");
                        definitions_str += definition + "\n\n";
                    }
                    wordView.setText(lemmatized_word + "(" + category + ")");
                    definitionView.setText(definitions_str);
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
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
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
        wordView.setText(word);
        definitionView.setText(sentence);
        return v;
    }
}
