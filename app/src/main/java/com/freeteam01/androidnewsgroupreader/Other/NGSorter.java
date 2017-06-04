package com.freeteam01.androidnewsgroupreader.Other;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupArticle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by christian on 04.06.17.
 */

public class NGSorter {
    static NGSorter instance_;

    public static NGSorter instance() {
        if (instance_ == null) {
            instance_ = new NGSorter();
        }
        return instance_;
    }

    //ty stackoverflow
    public ArrayList<NewsGroupArticle> sortByAuthor(ArrayList<NewsGroupArticle> arts) {
        HashMap<String, String> passedMap = new HashMap<>();
        for(NewsGroupArticle entry: arts){
            if(entry.getAuthor().getSurname() != null)
                passedMap.put(entry.getArticleID(), entry.getAuthor().getSurname().toLowerCase());
            else
                passedMap.put(entry.getArticleID(), entry.getAuthor().getNameString().toLowerCase());
        }

        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<String> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, String> sortedMap = new LinkedHashMap<>();

        Iterator<String> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            String val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                String comp1 = passedMap.get(key);
                String comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }

        ArrayList<NewsGroupArticle> sorted_ngs = new ArrayList<>();
        for(LinkedHashMap.Entry<String, String> sort: sortedMap.entrySet()){
            for(NewsGroupArticle entry: arts){
                if(entry.getArticleID().equals(sort.getKey()))
                    sorted_ngs.add(entry);
            }
        }

        return sorted_ngs;
    }

    //ty stackoverflow
    public ArrayList<NewsGroupArticle> sortBySubject(ArrayList<NewsGroupArticle> arts) {
        HashMap<String, String> passedMap = new HashMap<>();
        for(NewsGroupArticle entry: arts){
                passedMap.put(entry.getArticleID(), entry.getSubjectString().toLowerCase());
        }

        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<String> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, String> sortedMap = new LinkedHashMap<>();

        Iterator<String> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            String val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                String comp1 = passedMap.get(key);
                String comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }

        ArrayList<NewsGroupArticle> sorted_ngs = new ArrayList<>();
        for(LinkedHashMap.Entry<String, String> sort: sortedMap.entrySet()){
            for(NewsGroupArticle entry: arts){
                if(entry.getArticleID().equals(sort.getKey()))
                    sorted_ngs.add(entry);
            }
        }

        return sorted_ngs;
    }

    public ArrayList<NewsGroupArticle> sortByDate(ArrayList<NewsGroupArticle> arts){
        SortedMap<GregorianCalendar, String> dates = new TreeMap<>();
        for(NewsGroupArticle entry: arts){
            dates.put(entry.getDate().getDate(), entry.getArticleID());
        }
        ArrayList<NewsGroupArticle> sorted = new ArrayList<>();
        for(SortedMap.Entry<GregorianCalendar, String> sort: dates.entrySet()){
            for(NewsGroupArticle entry: arts){
                if(entry.getDate().getDate() == sort.getKey())
                    sorted.add(entry);
            }
        }
        Collections.reverse(sorted);
        return sorted;
    }

}
