package com.freeteam01.androidnewsgroupreader.Other;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;

import java.util.Comparator;

public class ArticleSorter implements Comparator<NewsGroupArticle> {

    public ArticleSorter(NewsGroupSortType type) {
        this.newsGroupSortType = type;
    }

    NewsGroupSortType newsGroupSortType;

    @Override
    public int compare(NewsGroupArticle o1, NewsGroupArticle o2) {
        if (newsGroupSortType == NewsGroupSortType.AUTHOR) {
            String firstSurname = o1.getAuthor().getSurname(), secondSurname = o2.getAuthor().getSurname();
            String firstNameString = o1.getAuthor().getNameString(), secondNameString = o2.getAuthor().getNameString();
            if (firstSurname != null && secondSurname != null)
                return firstSurname.toLowerCase().compareTo(secondSurname.toLowerCase());
            if (firstSurname == null && secondSurname != null)
                return firstNameString.toLowerCase().compareTo(secondSurname.toLowerCase());
            if (firstSurname != null && secondSurname == null)
                return firstSurname.toLowerCase().compareTo(secondNameString.toLowerCase());
            else
                return firstNameString.toLowerCase().compareTo(secondNameString.toLowerCase());
        } else if (newsGroupSortType == NewsGroupSortType.DATE) {
            return o1.getDate().getDate().compareTo(o2.getDate().getDate()) * -1;
        } else { //if (newsGroupSortType == NewsGroupSortType.SUBJECT) {
            return o1.getSubjectString().compareTo(o2.getSubjectString());
        }
    }
}
