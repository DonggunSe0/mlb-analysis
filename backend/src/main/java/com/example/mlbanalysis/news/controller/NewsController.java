package com.example.mlbanalysis.news.controller;

import com.example.mlbanalysis.news.dto.NewsListResponse;
import com.example.mlbanalysis.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @Operation(summary = "Return latest MLB news from MLB RSS feed")
    @GetMapping
    public NewsListResponse latest(@RequestParam(required = false) Integer limit) {
        return newsService.getLatestNews(limit);
    }
}
