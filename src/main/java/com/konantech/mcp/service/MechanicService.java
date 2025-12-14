package com.konantech.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konantech.mcp.security.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class MechanicService {

    private static final Logger logger = LoggerFactory.getLogger(MechanicService.class);

    @Value("${product.api.base-url}")
    private String apiBaseUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TokenProvider tokenProvider;

    public MechanicService(TokenProvider tokenProvider) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.tokenProvider = tokenProvider;
    }

    @Tool(description = "제품 ID로 상세한 제품 정보를 조회합니다. 제품의 전체 스펙, 가격, 재고, 특징을 확인할 수 있습니다.")
    public String getProductDetails(
            @ToolParam(description = "조회할 제품 ID (예: LAPTOP-2024-001, MONITOR-2024-001, KEYBOARD-2024-001)")
            String productId) {
        logger.info("[TOOL] getProductDetails 호출됨 - productId: {}", productId);

        try {
            String url = apiBaseUrl + "/api/products/" + productId;
            String jsonResponse = callApi(url);

            if (jsonResponse == null) {
                return "제품 ID '" + productId + "'를 찾을 수 없습니다.";
            }

            JsonNode product = objectMapper.readTree(jsonResponse);

            StringBuilder result = new StringBuilder();
            result.append("=== 제품 상세 정보 ===\n");
            result.append("제품명: ").append(product.get("name").asText()).append("\n");
            result.append("제품 ID: ").append(product.get("productId").asText()).append("\n");
            result.append("카테고리: ").append(product.get("category").asText()).append("\n");
            result.append("제조사: ").append(product.get("manufacturer").asText()).append("\n");
            result.append("출시일: ").append(product.get("releaseDate").asText()).append("\n");
            result.append("가격: ").append(String.format("%,d원", product.get("price").asInt())).append("\n");
            result.append("재고: ").append(product.get("stock").asInt()).append("개\n");
            result.append("스펙: ").append(product.get("specs").asText()).append("\n");
            result.append("주요 기능:\n");

            JsonNode features = product.get("features");
            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    result.append("  - ").append(feature.asText()).append("\n");
                }
            }

            logger.info("[TOOL] getProductDetails 결과 반환");
            return result.toString();

        } catch (Exception e) {
            logger.error("[TOOL] getProductDetails 에러 발생", e);
            return "제품 정보 조회 중 오류 발생: " + e.getMessage();
        }
    }

    @Tool(description = "카테고리별로 제품 목록을 조회합니다. 각 제품의 기본 정보와 가격, 재고를 확인할 수 있습니다.")
    public String searchProductsByCategory(
            @ToolParam(description = "조회할 제품 카테고리 (예: 노트북, 모니터, 키보드, 마우스, 저장장치, 헤드셋)")
            String category) {
        logger.info("[TOOL] searchProductsByCategory 호출됨 - category: {}", category);

        try {
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
            String url = apiBaseUrl + "/api/products/category/" + encodedCategory;
            String jsonResponse = callApi(url);

            if (jsonResponse == null) {
                return "카테고리 '" + category + "'에 해당하는 제품이 없습니다.";
            }

            JsonNode products = objectMapper.readTree(jsonResponse);

            if (!products.isArray() || products.size() == 0) {
                return "카테고리 '" + category + "'에 해당하는 제품이 없습니다.";
            }

            StringBuilder result = new StringBuilder();
            result.append("=== ").append(category).append(" 카테고리 제품 목록 ===\n");

            for (JsonNode product : products) {
                result.append("\n제품 ID: ").append(product.get("productId").asText()).append("\n");
                result.append("제품명: ").append(product.get("name").asText()).append("\n");
                result.append("제조사: ").append(product.get("manufacturer").asText()).append("\n");
                result.append("가격: ").append(String.format("%,d원", product.get("price").asInt())).append("\n");
                result.append("재고: ").append(product.get("stock").asInt()).append("개\n");
                result.append("---\n");
            }

            logger.info("[TOOL] searchProductsByCategory 결과: {}개 제품 발견", products.size());
            return result.toString();

        } catch (Exception e) {
            logger.error("[TOOL] searchProductsByCategory 에러 발생", e);
            return "제품 검색 중 오류 발생: " + e.getMessage();
        }
    }

    @Tool(description = "가격 범위 내의 제품을 검색합니다. 예산에 맞는 제품을 찾을 때 유용합니다.")
    public String searchProductsByPriceRange(
            @ToolParam(description = "최소 가격 (원)")
            int minPrice,
            @ToolParam(description = "최대 가격 (원)")
            int maxPrice) {
        logger.info("[TOOL] searchProductsByPriceRange 호출됨 - minPrice: {}, maxPrice: {}", minPrice, maxPrice);

        try {
            String url = apiBaseUrl + "/api/products/price-range?minPrice=" + minPrice + "&maxPrice=" + maxPrice;
            String jsonResponse = callApi(url);

            if (jsonResponse == null) {
                return String.format("%,d원 ~ %,d원 범위의 제품이 없습니다.", minPrice, maxPrice);
            }

            JsonNode products = objectMapper.readTree(jsonResponse);

            if (!products.isArray() || products.size() == 0) {
                return String.format("%,d원 ~ %,d원 범위의 제품이 없습니다.", minPrice, maxPrice);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("=== %,d원 ~ %,d원 범위 제품 ===\n", minPrice, maxPrice));

            for (JsonNode product : products) {
                result.append("\n제품명: ").append(product.get("name").asText()).append("\n");
                result.append("제품 ID: ").append(product.get("productId").asText()).append("\n");
                result.append("카테고리: ").append(product.get("category").asText()).append("\n");
                result.append("가격: ").append(String.format("%,d원", product.get("price").asInt())).append("\n");
                result.append("재고: ").append(product.get("stock").asInt()).append("개\n");
                result.append("---\n");
            }

            logger.info("[TOOL] searchProductsByPriceRange 결과: {}개 제품 발견", products.size());
            return result.toString();

        } catch (Exception e) {
            logger.error("[TOOL] searchProductsByPriceRange 에러 발생", e);
            return "제품 검색 중 오류 발생: " + e.getMessage();
        }
    }

    @Tool(description = "재고가 있는 제품만 조회합니다. 즉시 구매 가능한 제품을 확인할 수 있습니다.")
    public String getAvailableProducts() {
        logger.info("[TOOL] getAvailableProducts 호출됨");

        try {
            String url = apiBaseUrl + "/api/products/available";
            String jsonResponse = callApi(url);

            if (jsonResponse == null) {
                return "재고 보유 제품이 없습니다.";
            }

            JsonNode products = objectMapper.readTree(jsonResponse);

            StringBuilder result = new StringBuilder();
            result.append("=== 재고 보유 제품 목록 ===\n");

            for (JsonNode product : products) {
                result.append("\n제품명: ").append(product.get("name").asText()).append("\n");
                result.append("제품 ID: ").append(product.get("productId").asText()).append("\n");
                result.append("카테고리: ").append(product.get("category").asText()).append("\n");
                result.append("가격: ").append(String.format("%,d원", product.get("price").asInt())).append("\n");
                result.append("재고: ").append(product.get("stock").asInt()).append("개\n");
                result.append("---\n");
            }

            logger.info("[TOOL] getAvailableProducts 결과: {}개 제품 반환", products.size());
            return result.toString();

        } catch (Exception e) {
            logger.error("[TOOL] getAvailableProducts 에러 발생", e);
            return "제품 조회 중 오류 발생: " + e.getMessage();
        }
    }

    @Tool(description = "제조사별 제품을 조회합니다. 특정 브랜드의 모든 제품을 확인할 수 있습니다.")
    public String searchProductsByManufacturer(
            @ToolParam(description = "제조사 이름 (예: TechPro, WorkMaster, UltraView, MechaMaster, PrecisionPro, SpeedMax, AudioPro)")
            String manufacturer) {
        logger.info("[TOOL] searchProductsByManufacturer 호출됨 - manufacturer: {}", manufacturer);

        try {
            String encodedManufacturer = URLEncoder.encode(manufacturer, StandardCharsets.UTF_8);
            String url = apiBaseUrl + "/api/products/manufacturer/" + encodedManufacturer;
            String jsonResponse = callApi(url);

            if (jsonResponse == null) {
                return "제조사 '" + manufacturer + "'의 제품이 없습니다.";
            }

            JsonNode products = objectMapper.readTree(jsonResponse);

            if (!products.isArray() || products.size() == 0) {
                return "제조사 '" + manufacturer + "'의 제품이 없습니다.";
            }

            StringBuilder result = new StringBuilder();
            result.append("=== ").append(manufacturer).append(" 제품 목록 ===\n");

            for (JsonNode product : products) {
                result.append("\n제품명: ").append(product.get("name").asText()).append("\n");
                result.append("제품 ID: ").append(product.get("productId").asText()).append("\n");
                result.append("카테고리: ").append(product.get("category").asText()).append("\n");
                result.append("가격: ").append(String.format("%,d원", product.get("price").asInt())).append("\n");
                result.append("재고: ").append(product.get("stock").asInt()).append("개\n");
                result.append("스펙: ").append(product.get("specs").asText()).append("\n");
                result.append("---\n");
            }

            logger.info("[TOOL] searchProductsByManufacturer 결과: {}개 제품 발견", products.size());
            return result.toString();

        } catch (Exception e) {
            logger.error("[TOOL] searchProductsByManufacturer 에러 발생", e);
            return "제품 검색 중 오류 발생: " + e.getMessage();
        }
    }

    /**
     * REST API 호출 헬퍼 메서드
     * JWT 토큰을 포함하여 API 호출
     */
    private String callApi(String url) {
        try {
            // JWT 토큰 가져오기
            String authHeader = tokenProvider.getAuthorizationHeader();

            // HTTP 요청 생성
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET();

            // JWT 토큰이 있으면 Authorization 헤더 추가
            if (authHeader != null) {
                requestBuilder.header("Authorization", authHeader);
                logger.debug("API 호출 시 JWT 토큰 전달: {}", url);
            }

            HttpRequest request = requestBuilder.build();

            // API 호출
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("API 응답 상태: {} - URL: {}", response.statusCode(), url);

            if (response.statusCode() == 200) {
                return response.body();
            } else if (response.statusCode() == 404) {
                return null; // 데이터 없음
            } else {
                logger.error("API 호출 실패 - 상태 코드: {}, URL: {}", response.statusCode(), url);
                return null;
            }

        } catch (Exception e) {
            logger.error("API 호출 중 예외 발생 - URL: {}", url, e);
            return null;
        }
    }
}
