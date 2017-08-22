package com.myfitnesspal.nytimes.service;

/**
 * Created by tludewig on 8/6/17.
 */

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ArticlesService {
    String BASE_URL = "https://api.nytimes.com/";

    // My extra API key: 09dd030a1b854cc7af42a80dd45887f5
    // MFP key: d31fe793adf546658bd67e2b6a7fd11a
    // TODO - Make this string more less hard-coded and move constants to Constants file!
    @GET("svc/search/v2/articlesearch.json?api-key=d31fe793adf546658bd67e2b6a7fd11a&fl=web_url,headline,multimedia")
    Call<ResponseBody> getArticles(@Query("q") String q, @Query("page") int page);

}
