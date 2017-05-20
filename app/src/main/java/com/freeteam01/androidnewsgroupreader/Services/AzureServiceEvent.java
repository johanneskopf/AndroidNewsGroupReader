package com.freeteam01.androidnewsgroupreader.Services;

import com.freeteam01.androidnewsgroupreader.Models.NewsGroupEntry;

import java.util.List;

public interface AzureServiceEvent {
    void OnNewsgroupsLoaded(List<NewsGroupEntry> newsGroupEntries);
}
