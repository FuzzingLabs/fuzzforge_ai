package app.beetlebug.p001db;

import app.beetlebug.utils.Configuration;

/* loaded from: classes3.dex */
public class DatabaseRecord {
    private String author;
    private Configuration config = new Configuration();

    /* renamed from: id */
    private int f112id;
    private String title;

    public DatabaseRecord() {
    }

    public DatabaseRecord(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public int getId() {
        return this.f112id;
    }

    public void setId(int id) {
        this.f112id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (new String("").equals(title)) {
            title = this.config.default_title_database_item;
        }
        this.title = title;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        if (new String("").equals(author)) {
            author = this.config.default_author_database_item;
        }
        this.author = author;
    }

    public String toString() {
        return "Record [id=" + this.f112id + ", title=" + this.title + ", author=" + this.author + "]";
    }
}
