package com.freeteam01.androidnewsgroupreader.Services;

import android.util.Log;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;

import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class NewsGroupService {

    private static String hostname = "news.TUGraz.at";

    private NNTPClient client;

    public void Connect() throws IOException {
        client = new NNTPClient();
        client.connect(hostname);
    }

    public void Disconnect() throws IOException {
        client.disconnect();
    }

    public List<NewsGroupEntry> getAllNewsgroups() throws IOException {
        NewsgroupInfo[] newsgroups = client.listNewsgroups();

        List<NewsGroupEntry> newsgroupEntries = new ArrayList<>();
        for (NewsgroupInfo info : newsgroups) {
            newsgroupEntries.add(new NewsGroupEntry(info.getArticleCountLong(), info.getNewsgroup(), false));
        }
        return newsgroupEntries;
    }

    public List<NewsGroupArticle> getAllArticlesFromNewsgroup(String newsgroup) throws IOException {
        NewsgroupInfo group = new NewsgroupInfo();
        client.selectNewsgroup(newsgroup, group);
        long first = group.getFirstArticleLong();
        long last = group.getLastArticleLong();
        HashMap<String, NewsGroupArticle> articles = new HashMap<>();
        SortedMap<Integer, List<Article>> articlesByDepth = new TreeMap<>();

        for (Article article : client.iterateArticleInfo(first, last)) {
            int references = article.getReferences().length;
            if (!articlesByDepth.containsKey(references))
                articlesByDepth.put(references, new ArrayList<Article>());

            articlesByDepth.get(references).add(article);
        }

        for (Article article : articlesByDepth.get(0)) {
            articles.put(article.getArticleId(), new NewsGroupArticle(article.getArticleId(), article.getSubject(), article.getDate(), article.getFrom()));
        }
        articlesByDepth.remove(0);

        for (List<Article> articleList : articlesByDepth.values()) {
            for (Article article : articleList) {
                NewsGroupArticle ngArticle = new NewsGroupArticle(article.getArticleId(), article.getSubject(), article.getDate(), article.getFrom());
                ngArticle.addReferences(article.getReferences());
                NewsGroupArticle root = articles.get(ngArticle.getReferences().get(0));
                if(root == null)
                    articles.put(article.getArticleId(), ngArticle);
                else
                    root.addArticle(ngArticle);
            }
        }

        return new ArrayList<>(articles.values());
    }

    public String getArticleText(String id) throws IOException {
        Reader r = client.retrieveArticle(id);
        String article_text = "";
        int value;
        while((value = r.read()) != -1){
            article_text += (char) value;
        }
        article_text = article_text.substring(article_text.indexOf("\r\n\r\n"));
        return article_text;
    }
}
