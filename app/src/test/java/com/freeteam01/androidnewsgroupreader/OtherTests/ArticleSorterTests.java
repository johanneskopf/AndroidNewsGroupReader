package com.freeteam01.androidnewsgroupreader.OtherTests;

import com.freeteam01.androidnewsgroupreader.Models.Author;
import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;
import com.freeteam01.androidnewsgroupreader.Models.PostDate;
import com.freeteam01.androidnewsgroupreader.Other.ArticleSorter;
import com.freeteam01.androidnewsgroupreader.Other.NewsGroupSortType;

import org.junit.Test;

import java.util.GregorianCalendar;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArticleSorterTests {

    @Test
    public void test_compare() throws Exception {
        ArticleSorter articleSorter = new ArticleSorter(NewsGroupSortType.AUTHOR);

        NewsGroupArticle articleFirst = mock(NewsGroupArticle.class);
        NewsGroupArticle articleSecond = mock(NewsGroupArticle.class);

        Author authorFirst = mock(Author.class);
        Author authorSecond = mock(Author.class);

        when(articleFirst.getAuthor()).thenReturn(authorFirst);
        when(authorFirst.getSurname()).thenReturn("Max").thenReturn(null).thenReturn("Max").thenReturn(null);
        when(authorFirst.getNameString()).thenReturn("MaxMustermann");
        when(articleSecond.getAuthor()).thenReturn(authorSecond);
        when(authorSecond.getSurname()).thenReturn("Max").thenReturn("Max").thenReturn(null).thenReturn(null);
        when(authorSecond.getNameString()).thenReturn("MaxMustermann");

        for (int times = 0; times < 5; times++) {
            articleSorter.compare(articleFirst, articleSecond);
        }


        articleSorter = new ArticleSorter(NewsGroupSortType.DATE);

        PostDate postDate = mock(PostDate.class);
        when(articleFirst.getDate()).thenReturn(postDate);
        when(articleSecond.getDate()).thenReturn(postDate);
        GregorianCalendar calendar = mock(GregorianCalendar.class);
        when(postDate.getDate()).thenReturn(calendar);

        articleSorter.compare(articleFirst, articleSecond);


        articleSorter = new ArticleSorter(NewsGroupSortType.SUBJECT);

        when(articleFirst.getSubjectString()).thenReturn("Subject First");
        when(articleSecond.getSubjectString()).thenReturn("Subject Second");

        articleSorter.compare(articleFirst, articleSecond);
    }
}
