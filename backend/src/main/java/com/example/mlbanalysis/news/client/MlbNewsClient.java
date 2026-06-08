package com.example.mlbanalysis.news.client;

import com.example.mlbanalysis.news.dto.NewsItemResponse;
import java.util.List;

public interface MlbNewsClient {
    List<NewsItemResponse> getLatestNews(int limit);
}
