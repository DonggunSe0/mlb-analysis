package com.example.mlbanalysis.news.service;

import com.example.mlbanalysis.news.client.MlbNewsClient;
import com.example.mlbanalysis.news.dto.NewsListResponse;
import org.springframework.stereotype.Service;

@Service
public class NewsService {
    private final MlbNewsClient mlbNewsClient;

    public NewsService(MlbNewsClient mlbNewsClient) {
        this.mlbNewsClient = mlbNewsClient;
    }

    public NewsListResponse getLatestNews(Integer limit) {
        return new NewsListResponse(mlbNewsClient.getLatestNews(limit == null ? 8 : limit));
    }
}
