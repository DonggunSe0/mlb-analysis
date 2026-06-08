package com.example.mlbanalysis.news.client;

import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.news.dto.NewsItemResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Element;

@Component
public class RssMlbNewsClient implements MlbNewsClient {
    private static final String MLB_NEWS_RSS_URL = "https://www.mlb.com/feeds/news/rss.xml";
    private final RestClient restClient = RestClient.create();

    @Override
    @Cacheable(value = "mlbNews", key = "#limit")
    public List<NewsItemResponse> getLatestNews(int limit) {
        try {
            String xml = restClient.get().uri(MLB_NEWS_RSS_URL).retrieve().body(String.class);
            if (xml == null || xml.isBlank()) {
                throw new MlbApiException("MLB news response body is empty.");
            }
            return parse(xml, limit);
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to retrieve MLB news.", exception);
        } catch (Exception exception) {
            throw new MlbApiException("Failed to parse MLB news.", exception);
        }
    }

    private List<NewsItemResponse> parse(String xml, int limit) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        var document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        var items = document.getElementsByTagName("item");
        List<NewsItemResponse> news = new ArrayList<>();
        int cappedLimit = Math.max(1, Math.min(limit, 20));
        for (int i = 0; i < items.getLength() && news.size() < cappedLimit; i++) {
            Element item = (Element) items.item(i);
            news.add(new NewsItemResponse(
                    text(item, "title"),
                    text(item, "link"),
                    stripHtml(text(item, "description")),
                    text(item, "pubDate")
            ));
        }
        return news;
    }

    private String text(Element parent, String tagName) {
        var nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent();
    }

    private String stripHtml(String value) {
        if (value == null) return null;
        return value.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }
}
