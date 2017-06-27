package com.example.android.booklistingapp;

/**
 * Created by Kevin on 6/18/2017.
 */

public class Book {

    public String mTitle;
    public String [] mAuthors;
    public String mDescription;
    public String mThumbnailLink;
    public String mUrl;

    public Book(String bookTitle, String [] bookAuthors, String bookDescription, String bookThumbnailLink, String bookUrl) {
        mTitle = bookTitle;
        mAuthors = bookAuthors;
        mDescription = bookDescription;
        mThumbnailLink = bookThumbnailLink;
        mUrl = bookUrl;

    }

    public String getBookTitle() {
        return mTitle;
    }

    public String[] getBookAuthors() {
        return mAuthors;
    }

    public String getBookDescription() {
        return mDescription;
    }

    public String getBookThumbnailLink() {
        return mThumbnailLink;
    }

    public String getBookUrl() {
        return mUrl;
    }
}
