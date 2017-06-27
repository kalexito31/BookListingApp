package com.example.android.booklistingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity {

    public static final String LOG_TAG = BookActivity.class.getName();

    /**
     * URL for earthquake data from the Google Books dataset
     */
    private static final String REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    /**
     * Constant value for the book loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int BOOK_LOADER_ID = 1;

    /**
     * Adapter for the list of books
     */
    private BookListAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //Start loading results
                mAdapter.clear();

                //Reload results
                BookAsyncTask task = new BookAsyncTask();
                task.execute();

                swipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        loadContent();
    }

    public void loadContent() {

        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        bookListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of books as input
        mAdapter = new BookListAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected book.
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Book currentBook = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookUri = Uri.parse(currentBook.getBookUrl());

                // Create a new intent to view the book URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // Hide loading indicator because the user has not searched for anything
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        //When button is clicked start AsyncTaskLoader of network is available. Show "No Connection" Otherwise
        Button mSearchButton = (Button) findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User searched for something
                //Show loading indicator until results have appeared
                View loadingIndicator = findViewById(R.id.loading_indicator);
                loadingIndicator.setVisibility(View.VISIBLE);

                //Start loading results
                BookAsyncTask task = new BookAsyncTask();
                task.execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class BookAsyncTask extends AsyncTask<URL, Void, List<Book>> {

        private EditText mSearchEditText = (EditText) findViewById(R.id.book_search);
        private String searchInput = mSearchEditText.getText().toString();

        @Override
        protected List<Book> doInBackground(URL... urls) {
            //Trim string input from the EditText and replace spaces with "+" for the uri
            searchInput = searchInput.trim();
            searchInput = searchInput.replace(" ", "+");

            //Create a new URL based on the search input and preferences
            Uri baseUri = Uri.parse(REQUEST_URL+searchInput);
            final Uri.Builder uriBuilder = baseUri.buildUpon();

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            String numBooks = sharedPrefs.getString(getString(R.string.settings_num_books_key), getString(R.string.settings_num_books_default));

            uriBuilder.appendQueryParameter("maxResults", numBooks);

            // Perform the network request, parse the response, and extract a list of books.
            List<Book> books = QueryUtils.fetchBookData(uriBuilder.toString());

            return books;
        }

        @Override
        protected void onPostExecute(List<Book> books) {

            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Clear the adapter of previous book data
            mAdapter.clear();

            // If there is a valid list of {@link Book}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (books != null && !books.isEmpty()) {
                mAdapter.addAll(books);
            }
        }
    }


}
