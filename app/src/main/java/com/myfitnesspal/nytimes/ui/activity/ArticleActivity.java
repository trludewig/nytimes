package com.myfitnesspal.nytimes.ui.activity;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.myfitnesspal.nytimes.R;
import com.myfitnesspal.nytimes.util.Constants;


public class ArticleActivity extends AppCompatActivity {

    private WebView webView;
    private ShareActionProvider shareActionProvider;
    private String webUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.article_toolbar);
        setSupportActionBar(myToolbar);

        setTitle(getIntent().getExtras().getString(Constants.HEADLINE));

        webView = (WebView) findViewById(R.id.article_web_view);
        webView.setWebViewClient(new WebViewClient());
        this.webUrl = getIntent().getExtras().getString(Constants.WEB_URL);
        webView.loadUrl(webUrl);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_share_menu, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        shareActionProvider.setShareIntent(createShareIntent());
        return true;
    }

    protected Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, Constants.SHARE_STR + " " + webUrl);
        return shareIntent;
    }
}
