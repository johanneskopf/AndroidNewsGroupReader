package com.freeteam01.androidnewsgroupreader.Services;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupServer;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupPostArticle;

import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;
import org.apache.commons.net.nntp.SimpleNNTPHeader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsGroupService {

    private String hostname;
    private NewsGroupServer server;

    private NNTPClient client;

    public NewsGroupService(NewsGroupServer server) {
        this.server = server;
        this.hostname = server.getName();
    }

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
            newsgroupEntries.add(new NewsGroupEntry(server, safeLongToInt(info.getArticleCountLong()), info.getNewsgroup()));
        }
        return newsgroupEntries;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public List<NewsGroupArticle> getAllArticlesFromNewsgroup(NewsGroupEntry newsgroup) throws IOException {
        NewsgroupInfo group = new NewsgroupInfo();
        client.selectNewsgroup(newsgroup.getName(), group);
        long first = group.getFirstArticleLong();
        long last = group.getLastArticleLong();

        HashMap<String, NewsGroupArticle> articles = new HashMap<>();

        for (Article article : client.iterateArticleInfo(first, last)) {
            int references = article.getReferences().length;
            if(references == 0)
                articles.put(article.getArticleId(), new NewsGroupArticle(newsgroup, article.getArticleId(), article.getSubject(), article.getDate(), article.getFrom()));
            else {
                NewsGroupArticle ngArticle = new NewsGroupArticle(newsgroup, article.getArticleId(), article.getSubject(), article.getDate(), article.getFrom());
                ngArticle.addReferences(article.getReferences());
                for (NewsGroupArticle root : articles.values()) {
                    root.addArticle(ngArticle);
                }
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
        final String border = "\r\n\r\n";
        article_text = article_text.substring(article_text.indexOf(border) + border.length());
        return article_text;
    }

    public void postArticle(NewsGroupPostArticle article) throws IOException {
        if(!client.isAllowedToPost())
            throw new IOException("Client is not allowed to post to NG");

        Writer writer = client.postArticle();
        if(writer == null)
            throw new IOException("Couldn't get a Writer");

        SimpleNNTPHeader header = new SimpleNNTPHeader(article.getFrom(), article.getSubject());

        for (String newsgroup : article.getNewsgroups()) {
            header.addNewsgroup(newsgroup);
        }

        StringBuilder references = new StringBuilder();
        for (String reference : article.getReferences()) {
            references.append(reference + " ");
        }
        header.addHeaderField("References", references.toString());

        writer.write(header.toString());
        writer.write(article.getMessage());
        writer.close();
        if(!client.completePendingCommand())
            throw new IOException("Post couldn't be finished");
    }
}
