package com.ucu.milishchuk.ebook;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class LibraryFragment extends Fragment {
    RecyclerView rv;
    LinearLayoutManager llm;
    RVAdapter adapter;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 21 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                InputStream epubInputStream = getContext().getContentResolver().openInputStream(uri);
                Book book = (new EpubReader()).readEpub(epubInputStream);
                BookCard bc;
                if(book.getMetadata().getAuthors().isEmpty()) {
                    bc = new BookCard(book.getTitle(), "", uri);
                } else {
                    bc = new BookCard(book.getTitle(), book.getMetadata().getAuthors().get(0).toString(), uri);
                }
                bookCards.add(bc);
                adapter.notifyItemInserted(bookCards.size()-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("epublib", Integer.toString(bookCards.size()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.library_fragment, container, false);

        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/epub+zip");
                startActivityForResult(intent, 21);
            }
        });

        rv = (RecyclerView) v.findViewById(R.id.rv);
        llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
        itemDecorator.setDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.divider, null));
        rv.addItemDecoration(itemDecorator);
        initializeData();
        adapter = new RVAdapter(bookCards);
        rv.setAdapter(adapter);

        return v;
    }


    private List<BookCard> bookCards;

    private void initializeData() {
        bookCards = new ArrayList<>();
    }


}

class BookCard {
    String name;
    String author;
    Uri uri;

    BookCard(String name, String author, Uri uri) {
        this.name = name;
        this.author = author;
        this.uri = uri;
    }
}

class RVAdapter extends RecyclerView.Adapter<RVAdapter.BookViewHolder> {


    public static class BookViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView bookName;
        TextView bookAuthor;
        TextView bookUri;

        BookViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            bookName = (TextView) itemView.findViewById(R.id.book_name);
            bookAuthor = (TextView) itemView.findViewById(R.id.book_author);
            bookUri = (TextView) itemView.findViewById(R.id.book_uri);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("epublib", bookUri.getText().toString());
                    AppCompatActivity activity = (AppCompatActivity)v.getContext();
                    Bundle bundle = new Bundle();
                    bundle.putString("uri", bookUri.getText().toString());
                    BookFragment fragment = new BookFragment();
                    fragment.setArguments(bundle);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                }
            });
        }
    }

    List<BookCard> bookCards;

    RVAdapter(List<BookCard> bookCards) {
        this.bookCards = bookCards;
    }

    @Override
    public int getItemCount() {
        return bookCards.size();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_card, viewGroup, false);
        BookViewHolder bvh = new BookViewHolder(v);
        return bvh;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder bookViewHolder, int i) {
        bookViewHolder.bookName.setText(bookCards.get(i).name);
        bookViewHolder.bookAuthor.setText(bookCards.get(i).author);
        bookViewHolder.bookUri.setText(bookCards.get(i).uri.toString());
    }
}

