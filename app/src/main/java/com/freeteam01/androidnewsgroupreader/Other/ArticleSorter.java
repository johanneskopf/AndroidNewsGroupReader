package com.freeteam01.androidnewsgroupreader.Other;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;

import java.util.Comparator;

public class ArticleSorter implements Comparator<NewsGroupArticle> {

    public ArticleSorter(NewsGroupSortType type)
    {
        this.newsGroupSortType = type;
    }

    NewsGroupSortType newsGroupSortType;

    @Override
    public int compare(NewsGroupArticle o1, NewsGroupArticle o2) {
        if(newsGroupSortType == NewsGroupSortType.AUTHOR)
        {
            if(o1.getAuthor().getSurname() != null && o2.getAuthor().getSurname() != null)
                return o1.getAuthor().getSurname().toLowerCase().compareTo(o2.getAuthor().getSurname().toLowerCase());
            if(o1.getAuthor().getSurname() == null && o2.getAuthor().getSurname() != null)
                return o1.getAuthor().getNameString().toLowerCase().compareTo(o2.getAuthor().getSurname().toLowerCase());
            if(o1.getAuthor().getSurname() != null && o2.getAuthor().getSurname() == null)
                return o1.getAuthor().getSurname().toLowerCase().compareTo(o2.getAuthor().getNameString().toLowerCase());
            else
                return o1.getAuthor().getNameString().toLowerCase().compareTo(o2.getAuthor().getNameString().toLowerCase());
        }
        else if(newsGroupSortType == NewsGroupSortType.DATE)
        {
            return o1.getDate().getDate().compareTo(o2.getDate().getDate()) * -1;
        }
        else if(newsGroupSortType == NewsGroupSortType.SUBJECT)
        {
            return o1.getSubjectString().compareTo(o2.getSubjectString());
        }
        else
        {
            throw new UnsupportedOperationException("Sort Operator not Implemented");
        }
    }
}
