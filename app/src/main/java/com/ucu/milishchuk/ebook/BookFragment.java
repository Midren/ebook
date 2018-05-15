package com.ucu.milishchuk.ebook;

import android.app.ActionBar;
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
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;

import static java.lang.Math.min;

public class BookFragment extends Fragment {

    static Book book;
    int cur_chapter = 0;
    ViewPager pager;
    PagerAdapter pagerAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        ((Activity) getContext()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

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
                PageFragment.pi = new ArrayList<>();
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
                    PageFragment.pi = new ArrayList<>();
//                    open_chapter(cur_chapter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ((AppCompatActivity) getContext()).getSupportActionBar().hide();
        View decorView = ((Activity) getContext()).getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        pager = (ViewPager) v.findViewById(R.id.pager);

        pagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(cur_chapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                cur_chapter = position;
//                Log.i("epublib", "" + cur_chapter);
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
                PageFragment.pi = new ArrayList<>();
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
        ((AppCompatActivity) getContext()).getSupportActionBar().show();
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onDestroyView();
    }
}
