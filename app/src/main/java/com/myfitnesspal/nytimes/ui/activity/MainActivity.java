package com.myfitnesspal.nytimes.ui.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jayway.jsonpath.Configuration;
import com.myfitnesspal.nytimes.R;
import com.myfitnesspal.nytimes.exceptions.NetworkException;
import com.myfitnesspal.nytimes.model.Article;
import com.myfitnesspal.nytimes.service.ArticlesService;
import com.myfitnesspal.nytimes.service.ServiceGenerator;
import com.myfitnesspal.nytimes.service.interceptors.NetworkInterceptor;
import com.myfitnesspal.nytimes.ui.adapter.ArticleSearchAdapter;
import com.myfitnesspal.nytimes.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.SearchManager.QUERY;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.jayway.jsonpath.JsonPath.read;

/**
 * TODO - Move progress spinner to recyclerView layout as a row.
 * TODO - Make layouts that are specific to device size and landscape/portrait.
 * TODO - Get images into the correct drawable folders such that appropriately sized images are shown.
 * TODO - Fix search such that you can search on blank!
 * TODO - Use best practices with styling/theme/layouts.
 * TODO - Sanitize query strings entered by user before attempting service call.
 * TODO - Review generics and update use of them here.
 * TODO - Implement using Fragments.
 * TODO - Exception Handling
 * TODO - Logging framework
 * TODO - Write tests.
 * TODO - Improve documentation for javadocs.
 */
public class MainActivity extends AppCompatActivity implements ArticleSearchAdapter.OnItemClickListener {

    private LinearLayoutManager layoutManager;
    private ArticleSearchAdapter articlesAdapter;
    private RecyclerView recyclerView;
    private LinearLayout emptyLayout, errorLayout;
    private SearchView searchView;
    private ProgressBar spinner;

    private boolean isLastPage = false;
    private int currentPage = 0;
    private String currentView;
    private String queryStr;
    private boolean isLoading = false;
    private ArticlesService articlesService;

    private static final int PAGE_SIZE = 10;
    private ArrayList<Article> articles = new ArrayList<>();
    private List<Call> calls = new ArrayList<>();
    private static final String LAYOUT_MGR_STATE = "layoutState";
    private static final String ARTICLES = "articles";
    private static final String QUERY_STR = "queryStr";
    private static final String CURRENT_PAGE = "page";
    private static final String CURRENT_VIEW = "view";
    private static final String RECYCLER_VIEW = "recycler";
    private static final String EMPTY_VIEW = "empty";
    private static final String ERROR_VIEW = "error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            setUpViews();
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            articlesAdapter = new ArticleSearchAdapter(this);
            recyclerView.setAdapter(articlesAdapter);
        }

        articlesService = ServiceGenerator.createService(
                ArticlesService.class,
                ArticlesService.BASE_URL,
                new NetworkInterceptor(this));

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                handleIntent(intent);
            }
            else {
                refreshAdapter();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(final Call call : calls) {

            try {
                call.cancel();
            } catch (NetworkOnMainThreadException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        removeListeners();
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        addListeners();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.

        outState.putParcelable(LAYOUT_MGR_STATE, layoutManager.onSaveInstanceState());
        outState.putParcelableArrayList(ARTICLES, articlesAdapter.getArticles());
        outState.putString(QUERY_STR, queryStr);
        outState.putInt(CURRENT_PAGE, currentPage);
        outState.putString(CURRENT_VIEW, currentView);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        articles = savedInstanceState.getParcelableArrayList(ARTICLES);
        queryStr = savedInstanceState.getString(QUERY_STR);
        currentPage = savedInstanceState.getInt(CURRENT_PAGE);

        setUpViews();

        Parcelable layoutMgrSavedState = savedInstanceState.getParcelable(LAYOUT_MGR_STATE);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.onRestoreInstanceState(layoutMgrSavedState);
        recyclerView.setLayoutManager(layoutManager);

        showView(savedInstanceState.getString(CURRENT_VIEW));

        articlesAdapter = new ArticleSearchAdapter(this, articles);
        recyclerView.setAdapter(articlesAdapter);

    }

    protected void setUpViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        emptyLayout = (LinearLayout) findViewById(R.id.empty_view);
        errorLayout = (LinearLayout) findViewById(R.id.error_view);
        recyclerView = (RecyclerView) findViewById(R.id.article_search_view);
        spinner = (ProgressBar)findViewById(R.id.progressBar);

    }

    protected void showView(String whichView, String copyText) {

        switch(whichView) {
            case RECYCLER_VIEW:
                if (currentView != RECYCLER_VIEW) {
                    emptyLayout.setVisibility(GONE);
                    errorLayout.setVisibility(GONE);
                    recyclerView.setVisibility(VISIBLE);
                    currentView = RECYCLER_VIEW;
                }
                break;
            case EMPTY_VIEW:
                emptyLayout.setVisibility(VISIBLE);
                errorLayout.setVisibility(GONE);
                recyclerView.setVisibility(GONE);
                spinner.setVisibility(GONE);
                currentView = EMPTY_VIEW;
                break;
            case ERROR_VIEW:
                emptyLayout.setVisibility(GONE);
                errorLayout.setVisibility(VISIBLE);
                recyclerView.setVisibility(GONE);
                spinner.setVisibility(GONE);
                TextView errorText = (TextView) errorLayout.findViewById(R.id.error_message);
                errorText.setText(copyText != null ? copyText : Constants.GENERAL_ERROR);
                currentView = ERROR_VIEW;
                break;
            default:
                emptyLayout.setVisibility(GONE);
                errorLayout.setVisibility(GONE);
                spinner.setVisibility(GONE);
                recyclerView.setVisibility(VISIBLE);
                currentView = RECYCLER_VIEW;
                break;
        }

    }

    protected void showView(String whichView) {
        showView(whichView, null);
    }

    protected void addListeners() {
        articlesAdapter.setOnItemClickListener(this);
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    protected void removeListeners() {
        articlesAdapter.removeOnItemClickListener();
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);

        return true;
    }

    /**
     * @param intent
     */
    protected void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            queryStr = intent.getStringExtra(QUERY);
        }
        refreshAdapter();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    protected Callback<ResponseBody> getArticlesCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            isLoading = false;
            spinner.setVisibility(GONE);

            ArrayList<Article> articles = null;

            if (!response.isSuccessful()) {
                // TODO - Log responseCode.
                System.out.println("failed service call: " + response.errorBody() + " and code: " + response.code());
                showView(ERROR_VIEW);
                return;
            }

            try {
                articles = parseArticlesFromResponse(response.body().string());

                if (articles != null && articles.size() > 0) {
                    articlesAdapter.addAll(articles);
                    if (articles.size() < PAGE_SIZE) {
                        isLastPage = true;
                    }
                }
                if (articlesAdapter.isEmpty()) {
                    showView(EMPTY_VIEW);
                }
                else {
                    showView(RECYCLER_VIEW);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // TODO - Log responseCode.
                showView(ERROR_VIEW);
                return;
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            // TODO - Log exception.
            t.printStackTrace();
            if (!call.isCanceled()) {
                isLoading = false;

                if (t instanceof NetworkException) {
                    showView(ERROR_VIEW, Constants.NETWORK_ERROR);
                }
                else {
                    showView(ERROR_VIEW, Constants.GENERAL_ERROR);
                }
            }
        }
    };

    protected void refreshAdapter() {
        articlesAdapter.clear();
        currentPage = 0;

        isLoading = true;
        spinner.setVisibility(VISIBLE);

        Call getArticlesCall = articlesService.getArticles(queryStr, currentPage);
        calls.add(getArticlesCall);
        getArticlesCall.enqueue(getArticlesCallback);
    }

    protected void loadMoreItems() {
        isLoading = true;
        spinner.setVisibility(VISIBLE);

        currentPage += 1;

        Call getArticlesCall = articlesService.getArticles(queryStr, currentPage);
        calls.add(getArticlesCall);
        getArticlesCall.enqueue(getArticlesCallback);
    }

    /**
     * Clean this up, it's awful!  I didn't want to create all the superfluous POJO classes in the json.  I realize there are ways to ignore but...
     * @param s
     * @return
     */
    protected ArrayList<Article> parseArticlesFromResponse(String s) {

        Object d = Configuration.defaultConfiguration().jsonProvider().parse(s);
        int l = read(d, "$.response.docs.length()");
        ArrayList<Article> articles = new ArrayList<Article>(l);
        for (int i = 0; i < l; i++) {
            Article a = new Article();
            a.setHeadline((String) read(d, "$.response.docs[" + i + "].headline.main"));
            a.setWebUrl((String) read(d, "$.response.docs[" + i + "].web_url"));
            try {
                List<Map<String, Object>> t = read(d, "$.response.docs[" + i + "].multimedia[?(@.width == 190)].url");
                if (t != null && t.size() > 0) {
                    a.setThumbnail("http://www.nytimes.com/" + t.get(0));
                }
            } catch (Exception e) {
                // TODO - handle exception
            }
            articles.add(a);
        }
        return articles;
    }

    protected RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreItems();
                }
            }
        }
    };

    @Override
    public void onItemClick(int position, ArticleSearchAdapter.ArticleViewHolder view) {

        Article selectedArticle = articlesAdapter.getItem(position);
        Intent detailIntent = new Intent(this, ArticleActivity.class);
        detailIntent.putExtra(Constants.HEADLINE, selectedArticle.getHeadline());
        detailIntent.putExtra(Constants.WEB_URL, selectedArticle.getWebUrl());
        startActivity(detailIntent);
    }
}