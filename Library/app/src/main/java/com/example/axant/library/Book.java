package com.example.axant.library;

/**
 * Created by axant on 13/02/2017.
 */

public class Book {

    int id;
    String title;
    String author;
    String publishing;
    int year;
    String status;

    public Book(int id, String title, String author, String publishing, int year, String status ) {
        this.id = id;
        this.title=title;
        this.author=author;
        this.publishing=publishing;
        this.year = year;
        this.status = status;

    }

    public int getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublishing() {
        return publishing;
    }

    public String getStatus() {
        return status;
    }

}
