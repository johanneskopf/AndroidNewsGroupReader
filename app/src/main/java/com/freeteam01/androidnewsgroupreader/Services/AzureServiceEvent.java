package com.freeteam01.androidnewsgroupreader.Services;

import java.util.List;

public interface AzureServiceEvent {
    <T> void OnLoaded(Class<T> classType, List<T> entries);
//    void OnSubscribedNewsgroupsLoaded(List<SubscribedNewsgroup> subscribedNewsgroups);
//    void OnServersLoaded(List<Server> servers);
//    void OnReadArticlesLoaded(List<ReadArticle> readArticles);
}
