package com.konantech.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class UnsplashService {

    // Unsplash API 호출을 통해 이미지 URL/데이터를 가져오는 서비스
    private static final Logger logger = LoggerFactory.getLogger(UnsplashService.class);

    private final RestClient.Builder restClientBuilder;
    private final String baseUrl;
    private final String accessKey;

    public UnsplashService(
            @Value("${unsplash.access-key:}") String accessKey,
            @Value("${unsplash.base-url:https://api.unsplash.com}") String baseUrl,
            RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
        this.baseUrl = baseUrl;
        this.accessKey = accessKey;
    }

    // 키워드로 검색한 첫 번째 이미지의 바이트 데이터를 내려준다
    public byte[] fetchImageByKeyword(String keyword) {
        String imageUrl = fetchImageUrlByKeyword(keyword);
        RestClient client = createClient();
        try {
            return client.get()
                    .uri(imageUrl)
                    .accept(MediaType.ALL)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientResponseException e) {
            logger.error("Unsplash 이미지 다운로드 실패 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    // 키워드로 검색한 첫 번째 결과의 이미지 URL을 가져온다
    public String fetchImageUrlByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("검색 키워드를 입력해 주세요.");
        }
        if (!StringUtils.hasText(accessKey)) {
            throw new IllegalStateException("Unsplash access key is not configured. Set unsplash.access-key or UNSPLASH_ACCESS_KEY.");
        }

        RestClient client = createClient();
        try {
            JsonNode response = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/search/photos")
                            .queryParam("query", keyword)
                            .queryParam("per_page", 1)
                            .queryParam("page", 1)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode results = response != null ? response.path("results") : null;
            if (results == null || !results.isArray() || results.isEmpty()) {
                throw new IllegalStateException("검색 결과가 없습니다: " + keyword);
            }

            String imageUrl = results.get(0).path("urls").path("regular").asText(null);
            if (!StringUtils.hasText(imageUrl)) {
                throw new IllegalStateException("이미지 URL을 찾을 수 없습니다: " + keyword);
            }
            return imageUrl;
        } catch (RestClientResponseException e) {
            logger.error("Unsplash API 호출 실패 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    // Unsplash 호출에 필요한 공통 헤더/기본 URL을 설정한 RestClient 생성
    private RestClient createClient() {
        return restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Client-ID " + accessKey)
                .defaultHeader("Accept-Version", "v1")
                .build();
    }
}
