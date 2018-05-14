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

public class PageFragment extends android.support.v4.app.Fragment {

    static ArrayList<Integer> pi = new ArrayList<>();
    int pageNumber;
    int chapterNumber;
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
//        if(pi.size() == pageNumber) {
//            if(pageNumber == 0) {
//                pi.add(new PageIndexes(0, 0));
//            } else {
//                pi.add(new PageIndexes(pi.get(pi.size() - 1).chapter, pi.get(pi.size()-1).index));
//            }
//        }
//        for(PageIndexes p : pi) {
//            Log.i("smth", p.chapter + " " + p.index);
//        }
//        chapterNumber = pi.get(pageNumber).chapter;
        Log.i("Ohohoh", "" + chapterNumber);


        ll.post(new Runnable() {
            @Override
            public void run() {
                open_chapter(pageNumber);//chapterNumber);
            }
        });

        return v;
    }

    public void open_chapter(int chapter) {
        Spine spine = new Spine(BookFragment.book.getTableOfContents());
        if (chapter < 0 || chapter >= spine.size()) {
            return;
        }
        if ( chapter == 0 && pi.size() == 0) {

            for (int i = 0; i < spine.size(); i++) {
                Resource res = spine.getResource(i);
                String book_str = "";
                InputStream is = null;
                try {
                    is = res.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        book_str += line;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int k = count_char(book_str);
//                Log.i("epublib", "Current chapter " + i);
//                Log.i("epublib", "Current page size: " + k + " Max Size: " + txt.getWidth() * txt.getHeight());
//                Log.i("epublib", "Number of pages: " + (k / txt.getWidth() / txt.getHeight() + 1));
//                Log.i("epublib", "");
                
                pi.add((k / txt.getWidth() / txt.getHeight() + 1));

            }
        }
        Log.i("epublib", "pages = " + pi.get(chapter));
        Resource res = spine.getResource(chapter);
        String book_str = "";
        InputStream is = null;
        try {
            is = res.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                book_str += line;
            }
            //Log.i("epublib", book_str);
            SpannableString page_ss = hmtlToSpannable(book_str);
            txt.setText(page_ss);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int count_char(String or_text) {
        or_text = or_text.replaceAll("</span>", "</span><br>");
        or_text = or_text.replaceAll("<head>.*</head>", "");
        or_text = or_text.replaceAll("<a.*?>", "");
        final Spanned text = Html.fromHtml(or_text, new ImageGetter(), null);

        int skipped = 0;
        int line_pos = 0;
        int lines = 0;
        int j = 0;
        for (; j < text.length(); j++) {
//            if (line_pos == 0 && (lines + 3) * txt.getLineHeight() * 40 * 17 + skipped * 17 > txt.getWidth() * txt.getHeight()) {
//                break;
//            } else
            if (line_pos * 17 > txt.getWidth()) {
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

        return ((lines + 3) * txt.getLineHeight() * 40 * 17 + skipped * 17);
    }

    public SpannableString hmtlToSpannable(String or_text) {
//        Log.i("Measure", "" + txt.getMeasuredWidth() +" " + txt.getMeasuredHeight());
//        Log.i("Lines", ""+txt.getLineHeight() + " " + txt.getMaxHeight());

        or_text = or_text.replaceAll("</span>", "</span><br>");
        or_text = or_text.replaceAll("<head>.*</head>", "");
        or_text = or_text.replaceAll("<a.*?>", "");
        final Spanned text = Html.fromHtml(or_text, new ImageGetter(), null);
//        int k = count_char(or_text);

        int skipped = 0;
        int line_pos = 0;
        int lines = 0;
//        int j = pi.get(pageNumber).index;
        int j = 0;
        for (; j < text.length(); j++) {
            if (line_pos == 0 && (lines + 3) * txt.getLineHeight() * 40 * 17 + skipped * 17 > txt.getWidth() * txt.getHeight()) {
                break;
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

        int stWord = 0;//pi.get(pageNumber).index;
        int stSent = 0;//pi.get(pageNumber).index;
        SpannableString spannableString = new SpannableString(text.subSequence(0, j));//pi.get(pageNumber).index, j));
        if (j == text.length() - 1) {
            --j;
        }
        for (int i = 1; i < j; i++) {
            if (text.charAt(i) == ' ' || text.charAt(i) == '\n' || i == j - 1) {
                final int st = stWord, en = i, stSen = stSent;
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        if (en - st < 2) {
                            return;
                        }
                        String word = text.toString().substring(st, en);
                        int enSen = stSen;
                        for (int j = stSen + 1; j < text.length(); j++) {

                            if (text.charAt(j) == '.' || text.charAt(j) == '?' ||
                                    text.charAt(j) == '!' || text.charAt(j) == '\n') {
                                enSen = j;
                                break;
                            }
                        }
                        String sentence = text.toString().substring(stSen, enSen);

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
            if (text.charAt(i - 1) == '.' || text.charAt(i - 1) == '?' ||
                    text.charAt(i - 1) == '!' || text.charAt(i - 1) == '\n') {
                stSent = i;
            }
        }
//        if (pi.get(pi.size() - 1).chapter <= chapterNumber && pi.get(pi.size() - 1).index < j) {
//            if(pi.size() <= pageNumber) {
//                pi.add(new PageIndexes(pageNumber, 0));
//            }
//            Log.i("lol", j + " " + text.length());
//            if (j + 1>= text.length()) {
//                pi.set(pi.size() -1, new PageIndexes(chapterNumber + 1, 0));
//            } else {
//                pi.set(pi.size() -1, new PageIndexes(chapterNumber, j));
//            }
//        }
        return spannableString;
    }

    public class ImageGetter implements Html.ImageGetter {
        public Drawable getDrawable(String source) {
            try {
                Resource r = BookFragment.book.getResources().getByHref(source);
                if (r == null) {
                    r = BookFragment.book.getResources().getByHref("OEBPS/" + source);
                }
                //Log.i("epublib", r.getHref());
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
