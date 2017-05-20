package com.freeteam01.androidnewsgroupreader.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NewsGroupArticle implements Parcelable {
    public static final Parcelable.Creator<NewsGroupArticle> CREATOR = new Parcelable.Creator<NewsGroupArticle>() {
        public NewsGroupArticle createFromParcel(Parcel in) {
            return new NewsGroupArticle(in);
        }

        public NewsGroupArticle[] newArray(int size) {
            return new NewsGroupArticle[size];
        }
    };
    private String id;
    private String articleID;
    private String subject;
    private String date;
    private String from;
    private boolean isread;
    private transient List<String> references = new ArrayList<>();
    private transient HashMap<String, NewsGroupArticle> children = new HashMap<>();

    public NewsGroupArticle(String articleId, String subject, String date, String from) {
        this.articleID = articleId;
        this.subject = subject;
        this.date = date;
        this.from = from;
        this.isread = false;
    }

    public NewsGroupArticle(Parcel in) {
        in.readList(this.references, null);
        this.children = in.readHashMap(NewsGroupArticle.class.getClassLoader());
        this.articleID = in.readString();
        this.subject = in.readString();
        this.date = in.readString();
        this.from = in.readString();
    }

    public String getId() {
        return id;
    }

    public String getArticleID() {
        return articleID;
    }

    public String getSubject() {
        return subject;
    }

    public String getSubjectString() {
        if (subject.startsWith("=?UTF-8?Q?")) {
            String subject_cut = subject.replace("=?UTF-8?Q?", "");
            subject_cut = subject_cut.replace("?=", "");
            ByteArrayOutputStream subject_bytes = new ByteArrayOutputStream();
            for (int i = 0; i < subject_cut.length(); i++) {
                char c = subject_cut.charAt(i);
                if (c == '=') {
                    String first_value = String.valueOf(subject_cut.charAt(i + 1));
                    String second_value = String.valueOf(subject_cut.charAt(i + 2));
                    Integer converted = Integer.valueOf((first_value + second_value).toLowerCase(), 16);
                    int converted_int = (int) converted;
                    subject_bytes.write((byte) converted_int);
                    i += 2;
                } else {
                    subject_bytes.write((byte) c);
                }
            }
            try {
                return subject_bytes.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return subject;
    }

    public String getDate() {
        return date;
    }

    public boolean getIsread() {
        return isread;
    }

    public void setIsread(boolean isread) {
        this.isread = isread;
    }

    public boolean hasUnreadChildren() {
        for (Map.Entry<String, NewsGroupArticle> article :
                getChildren().entrySet()) {
            if(!article.getValue().getIsread() || article.getValue().hasUnreadChildren())
            {
              return true;
            }
        }
        return false;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getReferences() {
        return references;
    }

    public HashMap<String, NewsGroupArticle> getChildren() {
        return children;
    }

    public void addReferences(String[] references) {
        this.references.addAll(Arrays.asList(references));
    }

    public void addArticle(NewsGroupArticle ngArticle) {
        addArticle(ngArticle, 0);
    }

    private void addArticle(NewsGroupArticle ngArticle, int depth) {
        if (ngArticle.getReferences().get(depth).equals(articleID)) {
            if (ngArticle.getReferences().size() == depth + 1)
                children.put(ngArticle.getArticleID(), ngArticle);
            else {
                if (children.containsKey(ngArticle.getReferences().get(depth + 1)))
                    children.get(ngArticle.getReferences().get(depth + 1)).addArticle(ngArticle, depth + 1);
                else
                    throw new IllegalArgumentException("An intermediate node is missing");
            }
        } else {
            throw new IllegalArgumentException("The Reference is not the one it should be");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(references);
        dest.writeMap(children);
        dest.writeString(articleID);
        dest.writeString(subject);
        dest.writeString(date);
        dest.writeString(from);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.articleID);
    }
}
