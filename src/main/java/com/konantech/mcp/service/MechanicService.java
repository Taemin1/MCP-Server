package com.konantech.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MechanicService {

    private static final Logger logger = LoggerFactory.getLogger(MechanicService.class);

    private static final Map<String, ProductInfo> PRODUCT_CATALOG = new HashMap<>();

    static class ProductInfo {
        String productId;
        String name;
        String category;
        int price;
        int stock;
        String specs;
        String manufacturer;
        String releaseDate;
        List<String> features;

        ProductInfo(String productId, String name, String category, int price, int stock,
                   String specs, String manufacturer, String releaseDate, List<String> features) {
            this.productId = productId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.specs = specs;
            this.manufacturer = manufacturer;
            this.releaseDate = releaseDate;
            this.features = features;
        }
    }

    static {
        PRODUCT_CATALOG.put("LAPTOP-2024-001", new ProductInfo(
            "LAPTOP-2024-001",
            "TechPro X1 Ultra",
            "노트북",
            2890000,
            15,
            "Intel Core i9-13900H, 32GB DDR5 RAM, 1TB NVMe SSD, RTX 4070 8GB, 15.6\" 4K OLED",
            "TechPro",
            "2024-03-15",
            Arrays.asList("Thunderbolt 4 포트 3개", "Wi-Fi 6E", "4K 웹캠", "지문인식", "배터리 12시간")
        ));

        PRODUCT_CATALOG.put("LAPTOP-2024-002", new ProductInfo(
            "LAPTOP-2024-002",
            "WorkMaster Pro 14",
            "노트북",
            1590000,
            8,
            "AMD Ryzen 7 7840HS, 16GB DDR5 RAM, 512GB NVMe SSD, Radeon 780M, 14\" 2.8K IPS",
            "WorkMaster",
            "2024-01-20",
            Arrays.asList("USB-C 충전", "백라이트 키보드", "MIL-STD-810H 인증", "배터리 15시간")
        ));

        PRODUCT_CATALOG.put("MONITOR-2024-001", new ProductInfo(
            "MONITOR-2024-001",
            "UltraView 32 Quantum",
            "모니터",
            890000,
            23,
            "32\" 4K 144Hz IPS Quantum Dot, HDR1000, 1ms 응답속도, DisplayPort 1.4, HDMI 2.1 x2",
            "UltraView",
            "2024-02-10",
            Arrays.asList("G-Sync Compatible", "FreeSync Premium Pro", "높이/회전 조절", "USB-C 90W PD")
        ));

        PRODUCT_CATALOG.put("KEYBOARD-2024-001", new ProductInfo(
            "KEYBOARD-2024-001",
            "MechaMaster K95 RGB",
            "키보드",
            189000,
            42,
            "기계식 청축, N-Key Rollover, 풀 RGB 백라이트, 알루미늄 프레임, USB Type-C 탈착식 케이블",
            "MechaMaster",
            "2023-11-05",
            Arrays.asList("매크로 키 6개", "USB 패스스루", "팜레스트 포함", "전용 소프트웨어")
        ));

        PRODUCT_CATALOG.put("MOUSE-2024-001", new ProductInfo(
            "MOUSE-2024-001",
            "PrecisionPro Wireless Elite",
            "마우스",
            129000,
            67,
            "무선 2.4GHz + Bluetooth, 25600 DPI 센서, 8개 프로그래밍 가능 버튼, 충전식 배터리 70시간",
            "PrecisionPro",
            "2024-04-01",
            Arrays.asList("DPI 조절 버튼", "RGB 라이팅", "좌우손 대칭 디자인", "무게 조절 가능")
        ));

        PRODUCT_CATALOG.put("SSD-2024-001", new ProductInfo(
            "SSD-2024-001",
            "SpeedMax NVMe Gen5 2TB",
            "저장장치",
            459000,
            31,
            "2TB NVMe PCIe Gen5 x4, 읽기 12000MB/s, 쓰기 10000MB/s, DRAM 캐시, 5년 보증",
            "SpeedMax",
            "2024-05-12",
            Arrays.asList("방열판 포함", "TBW 1200TB", "AES-256 암호화", "저전력 모드")
        ));

        PRODUCT_CATALOG.put("HEADSET-2024-001", new ProductInfo(
            "HEADSET-2024-001",
            "AudioPro X7 Wireless ANC",
            "헤드셋",
            279000,
            19,
            "블루투스 5.3, 능동형 노이즈캔슬링, 40mm 드라이버, 접이식, 배터리 40시간",
            "AudioPro",
            "2024-03-28",
            Arrays.asList("멀티포인트 연결", "LDAC 코덱", "투명 모드", "음성 어시스턴트", "유선 연결 지원")
        ));
    }

    @Tool(description = "제품 ID로 상세한 제품 정보를 조회합니다. 제품의 전체 스펙, 가격, 재고, 특징을 확인할 수 있습니다.")
    public String getProductDetails(
            @ToolParam(description = "조회할 제품 ID (예: LAPTOP-2024-001, MONITOR-2024-001, KEYBOARD-2024-001)")
            String productId) {
        logger.info("[TOOL] getProductDetails 호출됨 - productId: {}", productId);

        ProductInfo product = PRODUCT_CATALOG.get(productId);
        if (product == null) {
            String result = "제품 ID '" + productId + "'를 찾을 수 없습니다.";
            logger.info("[TOOL] getProductDetails 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 제품 상세 정보 ===\n");
        result.append("제품명: ").append(product.name).append("\n");
        result.append("제품 ID: ").append(product.productId).append("\n");
        result.append("카테고리: ").append(product.category).append("\n");
        result.append("제조사: ").append(product.manufacturer).append("\n");
        result.append("출시일: ").append(product.releaseDate).append("\n");
        result.append("가격: ").append(String.format("%,d원", product.price)).append("\n");
        result.append("재고: ").append(product.stock).append("개\n");
        result.append("스펙: ").append(product.specs).append("\n");
        result.append("주요 기능:\n");
        for (String feature : product.features) {
            result.append("  - ").append(feature).append("\n");
        }

        logger.info("[TOOL] getProductDetails 결과 반환");
        return result.toString();
    }

    @Tool(description = "카테고리별로 제품 목록을 조회합니다. 각 제품의 기본 정보와 가격, 재고를 확인할 수 있습니다.")
    public String searchProductsByCategory(
            @ToolParam(description = "조회할 제품 카테고리 (예: 노트북, 모니터, 키보드, 마우스, 저장장치, 헤드셋)")
            String category) {
        logger.info("[TOOL] searchProductsByCategory 호출됨 - category: {}", category);

        List<ProductInfo> products = PRODUCT_CATALOG.values().stream()
                .filter(p -> p.category.equals(category))
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            String result = "카테고리 '" + category + "'에 해당하는 제품이 없습니다.";
            logger.info("[TOOL] searchProductsByCategory 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== ").append(category).append(" 카테고리 제품 목록 ===\n");
        for (ProductInfo product : products) {
            result.append("\n제품 ID: ").append(product.productId).append("\n");
            result.append("제품명: ").append(product.name).append("\n");
            result.append("제조사: ").append(product.manufacturer).append("\n");
            result.append("가격: ").append(String.format("%,d원", product.price)).append("\n");
            result.append("재고: ").append(product.stock).append("개\n");
            result.append("---\n");
        }

        logger.info("[TOOL] searchProductsByCategory 결과: {}개 제품 발견", products.size());
        return result.toString();
    }

    @Tool(description = "가격 범위 내의 제품을 검색합니다. 예산에 맞는 제품을 찾을 때 유용합니다.")
    public String searchProductsByPriceRange(
            @ToolParam(description = "최소 가격 (원)")
            int minPrice,
            @ToolParam(description = "최대 가격 (원)")
            int maxPrice) {
        logger.info("[TOOL] searchProductsByPriceRange 호출됨 - minPrice: {}, maxPrice: {}", minPrice, maxPrice);

        List<ProductInfo> products = PRODUCT_CATALOG.values().stream()
                .filter(p -> p.price >= minPrice && p.price <= maxPrice)
                .sorted(Comparator.comparingInt(p -> p.price))
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            String result = String.format("%,d원 ~ %,d원 범위의 제품이 없습니다.", minPrice, maxPrice);
            logger.info("[TOOL] searchProductsByPriceRange 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append(String.format("=== %,d원 ~ %,d원 범위 제품 ===\n", minPrice, maxPrice));
        for (ProductInfo product : products) {
            result.append("\n제품명: ").append(product.name).append("\n");
            result.append("제품 ID: ").append(product.productId).append("\n");
            result.append("카테고리: ").append(product.category).append("\n");
            result.append("가격: ").append(String.format("%,d원", product.price)).append("\n");
            result.append("재고: ").append(product.stock).append("개\n");
            result.append("---\n");
        }

        logger.info("[TOOL] searchProductsByPriceRange 결과: {}개 제품 발견", products.size());
        return result.toString();
    }

    @Tool(description = "재고가 있는 제품만 조회합니다. 즉시 구매 가능한 제품을 확인할 수 있습니다.")
    public String getAvailableProducts() {
        logger.info("[TOOL] getAvailableProducts 호출됨");

        List<ProductInfo> products = PRODUCT_CATALOG.values().stream()
                .filter(p -> p.stock > 0)
                .sorted(Comparator.comparingInt(p -> -p.stock)) // 재고 많은 순
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder();
        result.append("=== 재고 보유 제품 목록 ===\n");
        for (ProductInfo product : products) {
            result.append("\n제품명: ").append(product.name).append("\n");
            result.append("제품 ID: ").append(product.productId).append("\n");
            result.append("카테고리: ").append(product.category).append("\n");
            result.append("가격: ").append(String.format("%,d원", product.price)).append("\n");
            result.append("재고: ").append(product.stock).append("개\n");
            result.append("---\n");
        }

        logger.info("[TOOL] getAvailableProducts 결과: {}개 제품 반환", products.size());
        return result.toString();
    }

    @Tool(description = "제조사별 제품을 조회합니다. 특정 브랜드의 모든 제품을 확인할 수 있습니다.")
    public String searchProductsByManufacturer(
            @ToolParam(description = "제조사 이름 (예: TechPro, WorkMaster, UltraView, MechaMaster, PrecisionPro, SpeedMax, AudioPro)")
            String manufacturer) {
        logger.info("[TOOL] searchProductsByManufacturer 호출됨 - manufacturer: {}", manufacturer);

        List<ProductInfo> products = PRODUCT_CATALOG.values().stream()
                .filter(p -> p.manufacturer.equals(manufacturer))
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            String result = "제조사 '" + manufacturer + "'의 제품이 없습니다.";
            logger.info("[TOOL] searchProductsByManufacturer 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== ").append(manufacturer).append(" 제품 목록 ===\n");
        for (ProductInfo product : products) {
            result.append("\n제품명: ").append(product.name).append("\n");
            result.append("제품 ID: ").append(product.productId).append("\n");
            result.append("카테고리: ").append(product.category).append("\n");
            result.append("가격: ").append(String.format("%,d원", product.price)).append("\n");
            result.append("재고: ").append(product.stock).append("개\n");
            result.append("스펙: ").append(product.specs).append("\n");
            result.append("---\n");
        }

        logger.info("[TOOL] searchProductsByManufacturer 결과: {}개 제품 발견", products.size());
        return result.toString();
    }
}
