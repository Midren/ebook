package com.ucu.milishchuk.ebook;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;

import static java.lang.Math.min;

public class BookFragment extends Fragment{

    static Book book;
    int cur_chapter = 0;
    ViewPager pager;
    PagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.book_view_pager, container, false);

        if (getArguments() != null) {
            String uri = getArguments().getString("uri", "");
            InputStream epubInputStream = null;
            try {
                epubInputStream = getContext().getContentResolver().openInputStream(Uri.parse(uri));
                book = (new EpubReader()).readEpub(epubInputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("last_book", uri);
            editor.apply();
//            open_chapter(cur_chapter);
        } else {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String book_uri = sharedPref.getString("last_book", "");
            cur_chapter = sharedPref.getInt("cur_chapter", 0);

            if (book_uri.length() == 0) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/epub+zip");
                startActivityForResult(intent, 42);
            } else {
                try {
                    InputStream epubInputStream = getContext().getContentResolver().openInputStream(Uri.parse(book_uri));
                    book = (new EpubReader()).readEpub(epubInputStream);
//                    open_chapter(cur_chapter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        pager = (ViewPager) v.findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("epublib", "onPageSelected, position = " + position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return v;
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public PageFragment getItem(int position) {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            Spine spine = new Spine(BookFragment.book.getTableOfContents());
            return spine.size();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                InputStream epubInputStream = getContext().getContentResolver().openInputStream(uri);
                book = (new EpubReader()).readEpub(epubInputStream);
                cur_chapter = 0;
//                open_chapter(cur_chapter);
            } catch (IOException e) {
                e.printStackTrace();
            }

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("last_book", uri.toString());
            editor.apply();
        }
    }

    @Override
    public void onDestroyView() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("cur_chapter", cur_chapter);
        editor.apply();
        super.onDestroyView();
    }

}
