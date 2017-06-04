package com.freeteam01.androidnewsgroupreader.Models;

import com.freeteam01.androidnewsgroupreader.Services.NewsGroupService;

import java.io.IOException;
import java.util.*;
import java.util.Date;

public class NewsGroupEntry {

    HashMap<String, NewsGroupArticle> articles_ = new HashMap<>();
    private String id;
    private int articleCount = 0;
    private String name = null;
    private boolean subscribed_ = false;
    private NewsGroupServer server_ = null;

    public NewsGroupEntry(NewsGroupServer server, int articleCount, String name) {
        super();
        this.server_ = server;
        this.articleCount = articleCount;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSubscribed() {
        return subscribed_;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    public String toString() {
        return "Name: '" + this.name + "', articleCount: '" + this.articleCount + "', selected: '" + this.subscribed_+ "'" + "', id: '" + this.id + "'";
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed_ = subscribed;
    }

    public void loadArticles() throws IOException {
        NewsGroupService service = new NewsGroupService(server_);
        service.Connect();
        for (NewsGroupArticle article : service.getAllArticlesFromNewsgroup(this)) {
            if (!articles_.containsKey(article.getArticleID())) {
                articles_.put(article.getArticleID(), article);
            } else {
                //TODO Update logic
            }
        }
        service.Disconnect();
    }

    public Collection<NewsGroupArticle> getArticles() {
        return articles_.values();
    }

    public Collection<NewsGroupArticle> getArticlesSortedBySubject(){
        List<String> names = new ArrayList<>();
        for(Map.Entry<String, NewsGroupArticle> entry: articles_.entrySet()){
            names.add(entry.getValue().getSubjectString());
        }
        Collections.sort(names);
        ArrayList<NewsGroupArticle> sorted = new ArrayList<>();
        for(String name: names){
            for(Map.Entry<String, NewsGroupArticle> entry: articles_.entrySet()){
                if(entry.getValue().getSubjectString().equals(name))
                    sorted.add(entry.getValue());
            }
        }
        return sorted;
    }

    public Collection<NewsGroupArticle> getArticlesSortedByAuthor(){
        List<String> names = new ArrayList<>();
        for(Map.Entry<String, NewsGroupArticle> entry: articles_.entrySet()){
            if(entry.getValue().getAuthor().getSurname() != null)
                names.add(entry.getValue().getAuthor().getSurname());
            else
                names.add(entry.getValue().getAuthor().getNameString());
        }
        Collections.sort(names);
        ArrayList<NewsGroupArticle> sorted = new ArrayList<>();
        for(String name: names){
            for(Map.Entry<String, NewsGroupArticle> entry: articles_.entrySet()){
                if(entry.getValue().getAuthor().getSurname() != null && entry.getValue().getAuthor().getSurname().equals(name))
                    sorted.add(entry.getValue());
                else if(entry.getValue().getAuthor().getNameString().equals(name))
                    sorted.add(entry.getValue());
            }
        }
        return sorted;
    }

    public Collection<NewsGroupArticle> getArticlesSortedByDate(){
        List<GregorianCalendar> dates = new ArrayList<>();
        for(Map.Entry<String, NewsGroupArticle> entry: articles_.entrySet()){
                dates.add(entry.getValue().getDate().getDate());
        }
        Collections.sort(dates);
        ArrayList<NewsGroupArticle> sorted = new ArrayList<>();
        for(GregorianCalendar date: dates){
            for(Map.Entry<String, NewsGroupArticle> entry: articles_.entrySet()){
                if(entry.getValue().getDate().getDate() == date)
                    sorted.add(entry.getValue());
            }
        }
        Collections.reverse(sorted);
        return sorted;
    }

    public NewsGroupServer getServer() {
        return server_;
    }

    public NewsGroupArticle getArticle(String article) {
        NewsGroupArticle newsGroupArticle = articles_.get(article);
        if (newsGroupArticle == null) {
            for (HashMap.Entry<String, NewsGroupArticle> ng : articles_.entrySet()) {
                newsGroupArticle = ng.getValue().getSubArticel(article);
                if(newsGroupArticle != null)
                    break;
//                if (ng.getValue().getSubArticel(articel).getChildren().containsKey(article)) {
//                    newsGroupArticle = ng.getValue().getChildren().get(article);
//                    break;
//                }
            }
        }
        return newsGroupArticle;
    }
}