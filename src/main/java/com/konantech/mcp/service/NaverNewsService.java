package com.konantech.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class NaverNewsService {

    private static final Logger logger = LoggerFactory.getLogger(NaverNewsService.class);

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    @Value("${naver.api.news-url}")
    private String newsUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NaverNewsService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Tool(description = "네이버 뉴스를 검색합니다. 키워드로 최신 뉴스를 조회할 수 있습니다.")
    public String searchNews(
            @ToolParam(description = "검색할 키워드 (예: 주식, IT, 경제, 스포츠)")
            String query,
            @ToolParam(description = "검색 결과 개수 (기본값: 10, 최대: 100)")
            Integer display) {
        logger.info("[TOOL] searchNews 호출됨 - query: {}, display: {}", query, display);

        try {
            // display 기본값 설정
            if (display == null || display <= 0) {
                display = 10;
            }
            if (display > 100) {
                display = 100;
            }

            // URL 인코딩
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String requestUrl = newsUrl + "?query=" + encodedQuery + "&display=" + display;

            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .GET()
                    .build();

            logger.debug("Naver API 요청 URL: {}", requestUrl);

            // API 호출
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("Naver API 응답 상태: {}", response.statusCode());

            if (response.statusCode() == 200) {
                // JSON 파싱
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode items = rootNode.get("items");

                if (items == null || items.size() == 0) {
                    String result = "'" + query + "' 검색 결과가 없습니다.";
                    logger.info("[TOOL] searchNews 결과: {}", result);
                    return result;
                }

                // 결과 포맷팅
                StringBuilder result = new StringBuilder();
                result.append("=== '").append(query).append("' 뉴스 검색 결과 ===\n");
                result.append("총 ").append(items.size()).append("건\n\n");

                for (int i = 0; i < items.size(); i++) {
                    JsonNode item = items.get(i);

                    String title = removeHtmlTags(item.get("title").asText());
                    String description = removeHtmlTags(item.get("description").asText());
                    String link = item.get("link").asText();
                    String pubDate = item.get("pubDate").asText();

                    result.append("[").append(i + 1).append("] ").append(title).append("\n");
                    result.append("내용: ").append(description).append("\n");
                    result.append("링크: ").append(link).append("\n");
                    result.append("날짜: ").append(pubDate).append("\n");
                    result.append("---\n\n");
                }

                logger.info("[TOOL] searchNews 결과: {}건 반환", items.size());
                return result.toString();

            } else {
                String errorMsg = "뉴스 검색 API 호출 실패 (상태 코드: " + response.statusCode() + ")";
                logger.error("[TOOL] searchNews 에러: {}", errorMsg);
                logger.error("응답 내용: {}", response.body());
                return errorMsg;
            }

        } catch (Exception e) {
            String errorMsg = "뉴스 검색 중 오류 발생: " + e.getMessage();
            logger.error("[TOOL] searchNews 예외 발생", e);
            return errorMsg;
        }
    }

    @Tool(description = "특정 주제의 최신 뉴스를 간단히 조회합니다.")
    public String getLatestNews(
            @ToolParam(description = "뉴스 주제 (예: IT, 경제, 정치, 스포츠, 연예)")
            String topic) {
        logger.info("[TOOL] getLatestNews 호출됨 - topic: {}", topic);

        // searchNews를 5개로 호출
        return searchNews(topic, 5);
    }

    /**
     * HTML 태그 제거 (네이버 API는 제목/설명에 <b> 태그 포함)
     */
    private String removeHtmlTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "");
    }
}
