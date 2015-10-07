package org.artifactory.addon.yum;

/**
 * @author Chen Keinan
 */
public class MetadataChangeLog {
    public String author;
    public String date;
    public String text;

    public MetadataChangeLog(String author, String date, String text) {
        this.author = author;
        this.date = date;
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
