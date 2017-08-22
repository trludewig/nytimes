package com.myfitnesspal.nytimes.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myfitnesspal.nytimes.R;
import com.myfitnesspal.nytimes.model.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * RecyclerView Adapter to show list of articles returned from search.
 *
 * Original from Etienne Lawlor's Adapter here:
 * https://github.com/lawloretienne/Loop/blob/master/app/src/main/java/com/etiennelawlor/loop/adapters/VideosAdapter.java
 *
 */
public class ArticleSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private List<Article> articles;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, ArticleViewHolder view);
    }

    public ArticleSearchAdapter(Context context) {
        this.context = context;
        this.articles = new ArrayList<Article>();
    }

    public ArticleSearchAdapter(Context context, ArrayList<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    public Article getItem(int position) {
        return articles.get(position);
    }

    public ArrayList<Article> getArticles() {
        return (ArrayList) articles;
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder {
        private TextView headlineView;
        private ImageView thumbnailView;


        ArticleViewHolder(View itemView) {
            super(itemView);
            headlineView = (TextView) itemView.findViewById(R.id.headline);
            thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }

    @Override
    public int getItemCount() {

        return articles.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View articleView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.article_search_item, viewGroup, false);

        return new ArticleViewHolder(articleView);
    }

    public boolean isLastPosition(int position) {

        return (position == articles.size()-1);
    }


    /**
     * Replaces the content in the article item view.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ArticleViewHolder articleViewHolder = (ArticleViewHolder) holder;
        Article article = articles.get(position);
        articleViewHolder.headlineView.setText(article.getHeadline());
        Picasso.with(context).load(article.getThumbnailUrl()).placeholder(R.mipmap.ic_launcher).into(articleViewHolder.thumbnailView);

        articleViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int layoutPosition = articleViewHolder.getLayoutPosition();
                if(layoutPosition != RecyclerView.NO_POSITION){
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(layoutPosition, articleViewHolder);
                    }
                }
            }
        });
    }

    public void add(Article item) {
        articles.add(item);
        System.out.println("Adding: " + item.getHeadline());
        notifyItemInserted(articles.size() - 1);
    }

    // ConcurrentModificationException issue here?!
    public void addAll(List<Article> items) {
        for (Article i : items) {
            add(i);
        }
    }

    private void remove(Article item) {
        int position = articles.indexOf(item);
        if (position > -1) {
            articles.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void removeOnItemClickListener() {
        this.itemClickListener = null;
    }

}