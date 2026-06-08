package com.example.mlbanalysis.news.dto;

public record NewsItemResponse(
        String title,
        String link,
        String summary,
        String publishedAt
) {
}
