package com.skov.timeRegForrest;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.util.concurrent.FutureCallback;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by DWP on 04-12-2015.
 */
public class JiraConnection {

    private static final String JIRA_URL = "http://features.nykreditnet.net";
    private static String username;
    private JiraRestClient jiraRestClient;

    public JiraConnection(String username, String password) {
        try {
            this.username = username;
            BasicHttpAuthenticationHandler authenticationHandler = new BasicHttpAuthenticationHandler(username, password);
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            jiraRestClient = factory.create(new URI(JIRA_URL), authenticationHandler);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void searchIssuesAssignedToUser(final SearchListener listener){
        searchIssuesAssignedToUser(username, listener);
    }

    public void searchIssuesAssignedToUser(String username, final SearchListener listener){
        String jql = "project = xportalen and assignee = " + username;

        internalSearchIssues(jql).then(new FutureCallback<SearchResult>() {
            public void onSuccess(SearchResult searchResult) {
                if (searchResult != null)
                    listener.onSuccess(searchResult.getIssues());
                else
                    listener.onSuccess(null);
            }

            public void onFailure(Throwable throwable) {
                listener.onFailure(throwable);
            }
        });
    }

    private Promise<SearchResult> internalSearchIssues(final String jqlQuery){
        return jiraRestClient.getSearchClient().searchJql(jqlQuery);
    }

    public interface SearchListener {
        /**
         * Is called when the search has finished
         * @param issues Returns a collection of issues; {@value null} if no issues were found.
         */
        public abstract void onSuccess(Iterable<Issue> issues);

        /**
         * Is called if any error occur during search
         * @param throwable The problem that occurred
         */
        public abstract void onFailure(Throwable throwable);
    }

}
