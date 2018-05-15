package com.ucu.milishchuk.ebook;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

public class PageFragment extends android.support.v4.app.Fragment {

    static ArrayList<PageIndexes> pi = new ArrayList<>();
    int pageNumber;
    TextView txt;
    LinearLayout ll;

    public class PageIndexes {
        int chapter;
        int index;

        PageIndexes(int ch, int ind) {
            this.chapter = ch;
            this.index = ind;
        }
    }

    static PageFragment newInstance(int page) {
        PageFragment pageFragment = new PageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("arg_page_num", page);
        pageFragment.setArguments(bundle);
        return pageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.book_fragment, container, false);

        txt = (TextView) v.findViewById(R.id.text);
        ll = (LinearLayout) v.findViewById(R.id.ll);
        txt.setMovementMethod(new LinkMovementMethod());


        pageNumber = getArguments().getInt("arg_page_num");
        Log.i("smth", "");

        ll.post(new Runnable() {
            @Override
            public void run() {
                open_chapter(pageNumber);//chapterNumber);
            }
        });

        return v;
    }

    public void initialize_pi() {
        Spine spine = new Spine(BookFragment.book.getTableOfContents());
        for (int i = 0; i < spine.size(); i++) {
            Resource res = spine.getResource(i);
            String or_text = "";
            InputStream is = null;
            try {
                is = res.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    or_text += line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            or_text = or_text.replaceAll("</span>", "</span><br>");
            or_text = or_text.replaceAll("<head>.*</head>", "");
            or_text = or_text.replaceAll("<a.*?>", "");
            final Spanned text = Html.fromHtml(or_text, new ImageGetter(), null);

            pi.add(new PageIndexes(i, 0));
            int skipped = 0;
            int line_pos = 0;
            int lines = 0;
            int j = 0;
            for (; j < text.length(); j++) {
                if (line_pos == 0 && (lines + 3) * txt.getLineHeight() * 40 * 17 + skipped * 17 > txt.getWidth() * txt.getHeight()) {
                    pi.add(new PageIndexes(i, j));
                    line_pos = 0;
                    skipped = 0;
                    lines = 0;
                    continue;
                } else if (line_pos * 17 > txt.getWidth()) {
                    line_pos = 0;
                    lines += 1;
                    continue;
                } else if (text.charAt(j) == '\n') {
                    skipped += txt.getWidth() / 17 - line_pos;
                    line_pos = 0;
                    lines += 1;
                    continue;
                }
                line_pos++;
            }

        }
    }

    public void open_chapter(int chapter) {
        Spine spine = new Spine(BookFragment.book.getTableOfContents());
        if (chapter < 0 || chapter >= spine.size()) {
            return;
        }
        if (pi.size() == 0) {
            initialize_pi();
        }
        Resource res = spine.getResource(pi.get(chapter).chapter);
        String book_str = "";
        InputStream is = null;
        try {
            is = res.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                book_str += line;
            }
            SpannableString page_ss = hmtlToSpannable(book_str, pi.get(chapter).index);
            txt.setText(page_ss);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public SpannableString hmtlToSpannable(String or_text, int st_ind) {
//        or_text = or_text.replaceAll("</span>", "</span><br>");
        or_text = or_text.replaceAll("<head>.*</head>", "");
        or_text = or_text.replaceAll("<a.*?>", "");
        final Spanned text = Html.fromHtml(or_text, new ImageGetter(), null);

        int skipped = 0;
        int line_pos = 0;
        int lines = 0;
        int j = text.length();
        if (pageNumber + 1 != pi.size() && pi.get(pageNumber + 1).chapter == pi.get(pageNumber).chapter) {
            j = pi.get(pageNumber + 1).index;
        }

        SpannableString spannableString = new SpannableString(text.subSequence(st_ind, j));
        if (j == text.length() - 1) {
            --j;
        }
//        Log.i("epublib", "range: " + st_ind + "-" + j);
        int stWord = 0;//0;
        int stSent = 0;//0;
        final int st_i = st_ind;
        for (int i = 1; i < j - st_ind; i++) {//st_ind = 0
            if (text.charAt(st_i + i) == ' ' || text.charAt(st_i + i) == '\n' || i == j - st_ind - 1) {
                if (i == j - st_i - 1) {
                    i = j - st_i;
                }
                final int st = stWord, en = i, stSen = stSent;

                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        if (en - st < 2) {
                            return;
                        }
                        String word = text.toString().substring(st + st_i, en + st_i);
                        int enSen = stSen;
                        for (int q = stSen + 1 + st_i; q < text.length(); q++) {

                            if (text.charAt(q) == '.' || text.charAt(q) == '?' ||
                                    text.charAt(q) == '!' || text.charAt(q) == '\n') {
                                enSen = q - st_i;
                                break;
                            }
                        }
                        String sentence = text.toString().substring(stSen + st_i, enSen + st_i);

                        //Log.v("TAG", "I've clicked on word " + word);
                        DialogFragment wordDialog = new WordDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString("Word", word);
                        bundle.putString("Sentence", sentence);
                        wordDialog.setArguments(bundle);
                        wordDialog.show(((Activity) getContext()).getFragmentManager(), "WordDialog");
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        ds.setUnderlineText(false);
                        ds.setColor(Color.BLACK);
                    }
                }, st, en, 0);
                stWord = i;
            }
            if (text.charAt(st_ind + i - 1) == '.' || text.charAt(st_ind + i - 1) == '?' ||
                    text.charAt(st_ind + i - 1) == '!' || text.charAt(st_ind + i - 1) == '\n') {
                stSent = i;
            }
        }
        return spannableString;
    }

    public class ImageGetter implements Html.ImageGetter {
        public Drawable getDrawable(String source) {
            try {
                Resource r = BookFragment.book.getResources().getByHref(source);
                if (r == null) {
                    r = BookFragment.book.getResources().getByHref("OEBPS/" + source);
                }
                Bitmap bm = BitmapFactory.decodeByteArray(r.getData(), 0, r.getData().length);
                Drawable drawable = new BitmapDrawable(getResources(), bm);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * 2, drawable.getIntrinsicHeight() * 2);
                return drawable;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
