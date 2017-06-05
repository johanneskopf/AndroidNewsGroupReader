package com.freeteam01.androidnewsgroupreader.Services;

import android.util.Log;

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

import java.nio.charset.StandardCharsets;

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
        Reader r = client.retrieveArticleBody(id);

        String article_text = "";
        int value;
        while((value = r.read()) != -1){
            article_text += (char) value;
        }

        byte[] article_bytes = article_text.getBytes();

        return new String(article_bytes, "UTF-8");
    }

    public boolean post(String user_name, String user_mail, String article_text,
                        String subject, String group){
        try {
            if(!client.isAllowedToPost())
                throw new IOException("Client is not allowed to post to NG");

            Writer writer = client.postArticle();
            if(writer == null) { // failure
                Log.d("NGS", "writer is null");
                return false;
            }

            writer.write(constructNNTPMessage(user_name, user_mail, article_text, subject,
                    group, null, null));

            writer.close();
            if(!client.completePendingCommand()) { // failure
                Log.d("NGS", "pending is false");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean answer(String user_name, String user_mail, String article_text, String subject,
                          String group, String article_id, List<String> references){
        try {
            if(!client.isAllowedToPost())
                throw new IOException("Client is not allowed to post to NG");

            Writer writer = client.postArticle();
            if(writer == null) { // failure
                Log.d("NGS", "writer is null");
                return false;
            }

            writer.write(constructNNTPMessage(user_name, user_mail, article_text, subject, group,
                    article_id, references));

            writer.close();
            if(!client.completePendingCommand()) { // failure
                Log.d("NGS", "pending is false");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String constructNNTPMessage(String user_name, String user_mail, String article_text,
                                       String subject, String group, String article_id,
                                       List<String> references){
        //TODO: insert user credentials here
        SimpleNNTPHeader nntp_header = new SimpleNNTPHeader(user_name +  " <" + user_mail + ">", subject);
        //nntp_header.addNewsgroup(group);
        nntp_header.addNewsgroup(group);

        //System.out.println(references);
        if(references != null) {
            String nntp_reference = "";

            if (references.size() != 0) {
                for (String reference : references) {
                    nntp_reference += reference + " ";
                }
            }

            nntp_reference += article_id;
            nntp_header.addHeaderField("References", nntp_reference);
        }
        return nntp_header.toString() + new String(article_text.getBytes(StandardCharsets.ISO_8859_1));
    }

}
