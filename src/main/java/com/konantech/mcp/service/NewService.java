package com.konantech.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewService {

    private static final Logger logger = LoggerFactory.getLogger(NewService.class);

    // ===== 1. 날씨 정보 관련 =====
    private static final Map<String, WeatherInfo> WEATHER_DATA = new HashMap<>();

    static class WeatherInfo {
        String city;
        int temperature;
        int humidity;
        String condition; // SUNNY, CLOUDY, RAINY, SNOWY
        int windSpeed;
        String airQuality; // GOOD, MODERATE, BAD
        LocalDate date;

        WeatherInfo(String city, int temperature, int humidity, String condition,
                   int windSpeed, String airQuality, LocalDate date) {
            this.city = city;
            this.temperature = temperature;
            this.humidity = humidity;
            this.condition = condition;
            this.windSpeed = windSpeed;
            this.airQuality = airQuality;
            this.date = date;
        }
    }

    // ===== 2. 도서 관리 관련 =====
    private static final Map<String, Book> BOOK_CATALOG = new LinkedHashMap<>();

    static class Book {
        String isbn;
        String title;
        String author;
        String publisher;
        int year;
        String genre;
        int pages;
        boolean available;
        String description;

        Book(String isbn, String title, String author, String publisher, int year,
             String genre, int pages, boolean available, String description) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.year = year;
            this.genre = genre;
            this.pages = pages;
            this.available = available;
            this.description = description;
        }
    }

    // ===== 3. 음식 레시피 관련 =====
    private static final Map<String, Recipe> RECIPE_DATA = new HashMap<>();

    static class Recipe {
        String id;
        String name;
        String category; // KOREAN, WESTERN, CHINESE, JAPANESE, DESSERT
        int cookingTime; // minutes
        String difficulty; // EASY, MEDIUM, HARD
        List<String> ingredients;
        List<String> steps;
        int servings;

        Recipe(String id, String name, String category, int cookingTime, String difficulty,
               List<String> ingredients, List<String> steps, int servings) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.cookingTime = cookingTime;
            this.difficulty = difficulty;
            this.ingredients = ingredients;
            this.steps = steps;
            this.servings = servings;
        }
    }

    // ===== 4. 운동 기록 관련 =====
    private static final Map<String, ExerciseRecord> EXERCISE_RECORDS = new LinkedHashMap<>();
    private static int exerciseIdCounter = 1;

    static class ExerciseRecord {
        String id;
        String exerciseType; // RUNNING, CYCLING, SWIMMING, WEIGHT_TRAINING, YOGA
        int duration; // minutes
        int caloriesBurned;
        LocalDateTime recordedAt;
        String notes;

        ExerciseRecord(String id, String exerciseType, int duration, int caloriesBurned,
                      LocalDateTime recordedAt, String notes) {
            this.id = id;
            this.exerciseType = exerciseType;
            this.duration = duration;
            this.caloriesBurned = caloriesBurned;
            this.recordedAt = recordedAt;
            this.notes = notes;
        }
    }

    // ===== 5. 여행지 정보 관련 =====
    private static final Map<String, Destination> TRAVEL_DESTINATIONS = new HashMap<>();

    static class Destination {
        String id;
        String name;
        String country;
        String region;
        String bestSeason;
        List<String> attractions;
        int estimatedBudget; // per day in KRW
        String description;

        Destination(String id, String name, String country, String region, String bestSeason,
                   List<String> attractions, int estimatedBudget, String description) {
            this.id = id;
            this.name = name;
            this.country = country;
            this.region = region;
            this.bestSeason = bestSeason;
            this.attractions = attractions;
            this.estimatedBudget = estimatedBudget;
            this.description = description;
        }
    }

    // ===== 6. 영화 정보 관련 =====
    private static final Map<String, Movie> MOVIE_DATABASE = new HashMap<>();

    static class Movie {
        String id;
        String title;
        String director;
        int releaseYear;
        String genre;
        int runningTime; // minutes
        double rating; // out of 10
        List<String> cast;
        String synopsis;

        Movie(String id, String title, String director, int releaseYear, String genre,
              int runningTime, double rating, List<String> cast, String synopsis) {
            this.id = id;
            this.title = title;
            this.director = director;
            this.releaseYear = releaseYear;
            this.genre = genre;
            this.runningTime = runningTime;
            this.rating = rating;
            this.cast = cast;
            this.synopsis = synopsis;
        }
    }

    static {
        // 날씨 데이터 초기화
        WEATHER_DATA.put("서울", new WeatherInfo("서울", 18, 55, "CLOUDY", 12, "GOOD", LocalDate.now()));
        WEATHER_DATA.put("부산", new WeatherInfo("부산", 22, 65, "SUNNY", 8, "MODERATE", LocalDate.now()));
        WEATHER_DATA.put("제주", new WeatherInfo("제주", 20, 70, "RAINY", 15, "GOOD", LocalDate.now()));
        WEATHER_DATA.put("대구", new WeatherInfo("대구", 19, 50, "SUNNY", 10, "GOOD", LocalDate.now()));

        // 도서 데이터 초기화
        BOOK_CATALOG.put("978-1234567890", new Book(
            "978-1234567890",
            "클린 코드",
            "로버트 C. 마틴",
            "인사이트",
            2013,
            "프로그래밍",
            584,
            true,
            "애자일 소프트웨어 장인 정신의 대가가 전하는 코드 작성의 기술"
        ));

        BOOK_CATALOG.put("978-0987654321", new Book(
            "978-0987654321",
            "이펙티브 자바",
            "조슈아 블로크",
            "인사이트",
            2018,
            "프로그래밍",
            468,
            true,
            "자바 플랫폼 모범 사례 완벽 가이드"
        ));

        BOOK_CATALOG.put("978-1122334455", new Book(
            "978-1122334455",
            "해리 포터와 마법사의 돌",
            "J.K. 롤링",
            "문학수첩",
            1999,
            "판타지",
            359,
            false,
            "평범한 소년 해리 포터의 마법 세계 모험"
        ));

        // 레시피 데이터 초기화
        RECIPE_DATA.put("RECIPE-001", new Recipe(
            "RECIPE-001",
            "김치찌개",
            "KOREAN",
            30,
            "EASY",
            Arrays.asList("김치 300g", "돼지고기 200g", "두부 1/2모", "대파 1대", "고춧가루 1큰술", "된장 1큰술"),
            Arrays.asList(
                "돼지고기를 먹기 좋은 크기로 썰어주세요",
                "냄비에 김치와 돼지고기를 넣고 볶아주세요",
                "물을 붓고 고춧가루, 된장을 넣어주세요",
                "끓으면 두부와 대파를 넣고 10분 더 끓이세요"
            ),
            4
        ));

        RECIPE_DATA.put("RECIPE-002", new Recipe(
            "RECIPE-002",
            "까르보나라",
            "WESTERN",
            20,
            "MEDIUM",
            Arrays.asList("스파게티면 200g", "베이컨 100g", "달걀 2개", "파마산 치즈 50g", "후추", "소금"),
            Arrays.asList(
                "스파게티면을 삶아주세요",
                "베이컨을 바삭하게 볶아주세요",
                "달걀과 치즈를 섞어주세요",
                "삶은 면에 베이컨과 달걀 치즈 혼합물을 넣고 버무려주세요"
            ),
            2
        ));

        // 여행지 데이터 초기화
        TRAVEL_DESTINATIONS.put("DEST-001", new Destination(
            "DEST-001",
            "교토",
            "일본",
            "간사이",
            "봄(3-5월), 가을(9-11월)",
            Arrays.asList("금각사", "후시미이나리 신사", "기요미즈데라", "아라시야마 대나무숲"),
            150000,
            "일본 전통 문화가 살아있는 고도, 사찰과 정원이 아름다운 도시"
        ));

        TRAVEL_DESTINATIONS.put("DEST-002", new Destination(
            "DEST-002",
            "프라하",
            "체코",
            "중앙유럽",
            "봄(4-6월), 가을(9-10월)",
            Arrays.asList("프라하 성", "카를교", "구시가지 광장", "천문시계"),
            120000,
            "중세 건축물이 잘 보존된 동화 같은 도시"
        ));

        // 영화 데이터 초기화
        MOVIE_DATABASE.put("MOVIE-001", new Movie(
            "MOVIE-001",
            "인셉션",
            "크리스토퍼 놀란",
            2010,
            "SF/스릴러",
            148,
            8.8,
            Arrays.asList("레오나르도 디카프리오", "마리옹 코티야르", "조셉 고든-레빗"),
            "꿈 속에서 생각을 훔치는 특수 요원의 마지막 임무"
        ));

        MOVIE_DATABASE.put("MOVIE-002", new Movie(
            "MOVIE-002",
            "기생충",
            "봉준호",
            2019,
            "드라마/스릴러",
            132,
            8.6,
            Arrays.asList("송강호", "이선균", "조여정", "최우식"),
            "가난한 가족이 부유한 가정에 침투하면서 벌어지는 사건"
        ));

        MOVIE_DATABASE.put("MOVIE-003", new Movie(
            "MOVIE-003",
            "인터스텔라",
            "크리스토퍼 놀란",
            2014,
            "SF/드라마",
            169,
            8.7,
            Arrays.asList("매튜 맥커너히", "앤 해서웨이", "제시카 차스테인"),
            "멸망 위기의 지구를 구하기 위해 우주로 떠나는 탐험대의 이야기"
        ));

        MOVIE_DATABASE.put("MOVIE-004", new Movie(
            "MOVIE-004",
            "쇼생크 탈출",
            "프랭크 다라본트",
            1994,
            "드라마",
            142,
            9.3,
            Arrays.asList("팀 로빈스", "모건 프리먼"),
            "무고하게 수감된 은행가의 희망과 우정, 그리고 자유를 향한 여정"
        ));

        MOVIE_DATABASE.put("MOVIE-005", new Movie(
            "MOVIE-005",
            "어벤져스: 엔드게임",
            "안소니 루소, 조 루소",
            2019,
            "액션/SF",
            181,
            8.4,
            Arrays.asList("로버트 다우니 주니어", "크리스 에반스", "스칼렛 요한슨"),
            "타노스에 맞서 우주를 구하기 위한 어벤져스의 마지막 전투"
        ));

        MOVIE_DATABASE.put("MOVIE-006", new Movie(
            "MOVIE-006",
            "타이타닉",
            "제임스 카메론",
            1997,
            "로맨스/드라마",
            194,
            7.9,
            Arrays.asList("레오나르도 디카프리오", "케이트 윈슬렛"),
            "침몰하는 타이타닉호에서 피어난 비극적인 사랑 이야기"
        ));

        MOVIE_DATABASE.put("MOVIE-007", new Movie(
            "MOVIE-007",
            "다크 나이트",
            "크리스토퍼 놀란",
            2008,
            "액션/범죄",
            152,
            9.0,
            Arrays.asList("크리스찬 베일", "히스 레저", "아론 에크하트"),
            "조커와 맞서 싸우는 배트맨의 암흑기를 그린 걸작"
        ));

        MOVIE_DATABASE.put("MOVIE-008", new Movie(
            "MOVIE-008",
            "헤어질 결심",
            "박찬욱",
            2022,
            "미스터리/로맨스",
            138,
            7.3,
            Arrays.asList("박해일", "탕웨이"),
            "살인 사건 용의자에게 빠져드는 형사의 위태로운 사랑"
        ));

        MOVIE_DATABASE.put("MOVIE-009", new Movie(
            "MOVIE-009",
            "라라랜드",
            "데이미언 차젤레",
            2016,
            "뮤지컬/로맨스",
            128,
            8.0,
            Arrays.asList("라이언 고슬링", "엠마 스톤"),
            "꿈을 쫓는 피아니스트와 배우의 사랑과 성장 이야기"
        ));

        MOVIE_DATABASE.put("MOVIE-010", new Movie(
            "MOVIE-010",
            "살인의 추억",
            "봉준호",
            2003,
            "범죄/드라마",
            132,
            8.1,
            Arrays.asList("송강호", "김상경", "박해일"),
            "1980년대 한국을 뒤흔든 연쇄살인사건을 추적하는 형사들"
        ));

        MOVIE_DATABASE.put("MOVIE-011", new Movie(
            "MOVIE-011",
            "포레스트 검프",
            "로버트 저메키스",
            1994,
            "드라마/코미디",
            142,
            8.8,
            Arrays.asList("톰 행크스", "로빈 라이트", "게리 시니즈"),
            "순수한 마음으로 역사적 순간들을 겪어내는 한 남자의 감동적인 인생"
        ));

        MOVIE_DATABASE.put("MOVIE-012", new Movie(
            "MOVIE-012",
            "조커",
            "토드 필립스",
            2019,
            "범죄/드라마",
            122,
            8.4,
            Arrays.asList("호아킨 피닉스", "로버트 드 니로"),
            "광대에서 범죄자로 변해가는 한 남자의 어두운 심리 탐구"
        ));
    }

    // ==================== 1. 날씨 정보 Tools ====================

    @Tool(description = "특정 도시의 현재 날씨 정보를 조회합니다. 기온, 습도, 날씨 상태, 풍속, 대기질을 확인할 수 있습니다.")
    public String getWeatherInfo(
            @ToolParam(description = "도시 이름 (예: 서울, 부산, 제주, 대구)")
            String city) {
        logger.info("[TOOL] getWeatherInfo 호출됨 - city: {}", city);

        WeatherInfo weather = WEATHER_DATA.get(city);
        if (weather == null) {
            String result = "도시 '" + city + "'의 날씨 정보를 찾을 수 없습니다.";
            logger.info("[TOOL] getWeatherInfo 결과: {}", result);
            return result;
        }

        String conditionKr = getWeatherConditionKorean(weather.condition);
        String airQualityKr = getAirQualityKorean(weather.airQuality);

        StringBuilder result = new StringBuilder();
        result.append("=== ").append(city).append(" 날씨 정보 ===\n");
        result.append("날씨: ").append(conditionKr).append("\n");
        result.append("기온: ").append(weather.temperature).append("°C\n");
        result.append("습도: ").append(weather.humidity).append("%\n");
        result.append("풍속: ").append(weather.windSpeed).append("m/s\n");
        result.append("대기질: ").append(airQualityKr).append("\n");
        result.append("조회일: ").append(weather.date).append("\n");

        logger.info("[TOOL] getWeatherInfo 결과 반환");
        return result.toString();
    }

    // ==================== 2. 도서 관리 Tools ====================

    @Tool(description = "도서 목록을 조회합니다. 장르별로 필터링하거나 전체 목록을 볼 수 있습니다.")
    public String searchBooks(
            @ToolParam(description = "검색할 장르 (ALL, 프로그래밍, 판타지, 소설, 에세이 등)")
            String genre) {
        logger.info("[TOOL] searchBooks 호출됨 - genre: {}", genre);

        List<Book> books = BOOK_CATALOG.values().stream()
            .filter(b -> genre.equals("ALL") || b.genre.equals(genre))
            .collect(Collectors.toList());

        if (books.isEmpty()) {
            String result = "장르 '" + genre + "'에 해당하는 도서가 없습니다.";
            logger.info("[TOOL] searchBooks 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 도서 목록 ===\n");
        for (Book book : books) {
            result.append("\n제목: ").append(book.title).append("\n");
            result.append("저자: ").append(book.author).append("\n");
            result.append("출판사: ").append(book.publisher).append(" (").append(book.year).append("년)\n");
            result.append("장르: ").append(book.genre).append(" | 페이지: ").append(book.pages).append("쪽\n");
            result.append("대출 가능: ").append(book.available ? "O" : "X").append("\n");
            result.append("ISBN: ").append(book.isbn).append("\n");
            result.append("---\n");
        }

        logger.info("[TOOL] searchBooks 결과: {}권 발견", books.size());
        return result.toString();
    }

    @Tool(description = "ISBN으로 특정 도서의 상세 정보를 조회합니다.")
    public String getBookDetails(
            @ToolParam(description = "도서 ISBN (예: 978-1234567890)")
            String isbn) {
        logger.info("[TOOL] getBookDetails 호출됨 - isbn: {}", isbn);

        Book book = BOOK_CATALOG.get(isbn);
        if (book == null) {
            String result = "ISBN '" + isbn + "'에 해당하는 도서를 찾을 수 없습니다.";
            logger.info("[TOOL] getBookDetails 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 도서 상세 정보 ===\n");
        result.append("제목: ").append(book.title).append("\n");
        result.append("저자: ").append(book.author).append("\n");
        result.append("출판사: ").append(book.publisher).append("\n");
        result.append("출판년도: ").append(book.year).append("년\n");
        result.append("장르: ").append(book.genre).append("\n");
        result.append("페이지: ").append(book.pages).append("쪽\n");
        result.append("대출 가능: ").append(book.available ? "가능" : "불가능").append("\n");
        result.append("ISBN: ").append(book.isbn).append("\n");
        result.append("\n[소개]\n").append(book.description).append("\n");

        logger.info("[TOOL] getBookDetails 결과 반환");
        return result.toString();
    }

    // ==================== 3. 레시피 Tools ====================

    @Tool(description = "레시피를 검색합니다. 카테고리별로 필터링하거나 난이도별로 검색할 수 있습니다.")
    public String searchRecipes(
            @ToolParam(description = "카테고리 (ALL, KOREAN, WESTERN, CHINESE, JAPANESE, DESSERT)")
            String category) {
        logger.info("[TOOL] searchRecipes 호출됨 - category: {}", category);

        List<Recipe> recipes = RECIPE_DATA.values().stream()
            .filter(r -> category.equals("ALL") || r.category.equals(category))
            .collect(Collectors.toList());

        if (recipes.isEmpty()) {
            String result = "카테고리 '" + category + "'에 해당하는 레시피가 없습니다.";
            logger.info("[TOOL] searchRecipes 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 레시피 목록 ===\n");
        for (Recipe recipe : recipes) {
            result.append("\n[").append(recipe.id).append("] ").append(recipe.name).append("\n");
            result.append("카테고리: ").append(getCategoryKorean(recipe.category)).append("\n");
            result.append("난이도: ").append(getDifficultyKorean(recipe.difficulty));
            result.append(" | 조리시간: ").append(recipe.cookingTime).append("분");
            result.append(" | 인분: ").append(recipe.servings).append("인분\n");
            result.append("---\n");
        }

        logger.info("[TOOL] searchRecipes 결과: {}개 레시피 발견", recipes.size());
        return result.toString();
    }

    @Tool(description = "레시피 ID로 상세한 조리법을 조회합니다. 재료와 조리 단계를 확인할 수 있습니다.")
    public String getRecipeDetails(
            @ToolParam(description = "레시피 ID (예: RECIPE-001)")
            String recipeId) {
        logger.info("[TOOL] getRecipeDetails 호출됨 - recipeId: {}", recipeId);

        Recipe recipe = RECIPE_DATA.get(recipeId);
        if (recipe == null) {
            String result = "레시피 ID '" + recipeId + "'를 찾을 수 없습니다.";
            logger.info("[TOOL] getRecipeDetails 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== ").append(recipe.name).append(" 레시피 ===\n");
        result.append("카테고리: ").append(getCategoryKorean(recipe.category)).append("\n");
        result.append("난이도: ").append(getDifficultyKorean(recipe.difficulty)).append("\n");
        result.append("조리시간: ").append(recipe.cookingTime).append("분\n");
        result.append("인분: ").append(recipe.servings).append("인분\n\n");

        result.append("[재료]\n");
        for (String ingredient : recipe.ingredients) {
            result.append("- ").append(ingredient).append("\n");
        }

        result.append("\n[조리 순서]\n");
        for (int i = 0; i < recipe.steps.size(); i++) {
            result.append(i + 1).append(". ").append(recipe.steps.get(i)).append("\n");
        }

        logger.info("[TOOL] getRecipeDetails 결과 반환");
        return result.toString();
    }

    // ==================== 4. 운동 기록 Tools ====================

    @Tool(description = "운동 기록을 추가합니다. 운동 종류, 시간, 소모 칼로리를 기록할 수 있습니다.")
    public String recordExercise(
            @ToolParam(description = "운동 종류 (RUNNING, CYCLING, SWIMMING, WEIGHT_TRAINING, YOGA)")
            String exerciseType,
            @ToolParam(description = "운동 시간 (분)")
            int duration,
            @ToolParam(description = "소모 칼로리 (kcal)")
            int calories,
            @ToolParam(description = "메모 (선택사항)")
            String notes) {
        logger.info("[TOOL] recordExercise 호출됨 - type: {}, duration: {}분", exerciseType, duration);

        String id = "EX-" + String.format("%03d", exerciseIdCounter++);
        ExerciseRecord record = new ExerciseRecord(
            id, exerciseType, duration, calories, LocalDateTime.now(), notes
        );
        EXERCISE_RECORDS.put(id, record);

        String result = String.format("✓ 운동 기록이 추가되었습니다.\nID: %s\n운동: %s\n시간: %d분\n칼로리: %d kcal",
            id, getExerciseTypeKorean(exerciseType), duration, calories);

        logger.info("[TOOL] recordExercise 결과: {}", result);
        return result;
    }

    @Tool(description = "운동 기록을 조회합니다. 전체 또는 특정 운동 종류별로 필터링할 수 있습니다.")
    public String getExerciseHistory(
            @ToolParam(description = "운동 종류 필터 (ALL, RUNNING, CYCLING, SWIMMING, WEIGHT_TRAINING, YOGA)")
            String typeFilter) {
        logger.info("[TOOL] getExerciseHistory 호출됨 - filter: {}", typeFilter);

        List<ExerciseRecord> records = EXERCISE_RECORDS.values().stream()
            .filter(r -> typeFilter.equals("ALL") || r.exerciseType.equals(typeFilter))
            .sorted((a, b) -> b.recordedAt.compareTo(a.recordedAt))
            .collect(Collectors.toList());

        if (records.isEmpty()) {
            String result = "운동 기록이 없습니다.";
            logger.info("[TOOL] getExerciseHistory 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 운동 기록 ===\n");

        int totalDuration = 0;
        int totalCalories = 0;

        for (ExerciseRecord record : records) {
            result.append("\n[").append(record.id).append("] ");
            result.append(getExerciseTypeKorean(record.exerciseType)).append("\n");
            result.append("시간: ").append(record.duration).append("분 | ");
            result.append("칼로리: ").append(record.caloriesBurned).append(" kcal\n");
            result.append("일시: ").append(record.recordedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
            if (record.notes != null && !record.notes.isEmpty()) {
                result.append("메모: ").append(record.notes).append("\n");
            }
            result.append("---\n");

            totalDuration += record.duration;
            totalCalories += record.caloriesBurned;
        }

        result.append("\n총 운동시간: ").append(totalDuration).append("분\n");
        result.append("총 소모 칼로리: ").append(totalCalories).append(" kcal\n");

        logger.info("[TOOL] getExerciseHistory 결과: {}개 기록 반환", records.size());
        return result.toString();
    }

    // ==================== 5. 여행지 정보 Tools ====================

    @Tool(description = "여행지 정보를 조회합니다. 국가나 지역별로 검색할 수 있습니다.")
    public String searchDestinations(
            @ToolParam(description = "검색 키워드 (국가명, 도시명 등)")
            String keyword) {
        logger.info("[TOOL] searchDestinations 호출됨 - keyword: {}", keyword);

        List<Destination> destinations = TRAVEL_DESTINATIONS.values().stream()
            .filter(d -> d.name.contains(keyword) || d.country.contains(keyword) || d.region.contains(keyword))
            .collect(Collectors.toList());

        if (destinations.isEmpty()) {
            String result = "검색어 '" + keyword + "'에 해당하는 여행지가 없습니다.";
            logger.info("[TOOL] searchDestinations 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 여행지 검색 결과 ===\n");
        for (Destination dest : destinations) {
            result.append("\n[").append(dest.id).append("] ").append(dest.name).append("\n");
            result.append("위치: ").append(dest.country).append(" - ").append(dest.region).append("\n");
            result.append("최적 시즌: ").append(dest.bestSeason).append("\n");
            result.append("예상 경비: ").append(String.format("%,d원/일", dest.estimatedBudget)).append("\n");
            result.append("---\n");
        }

        logger.info("[TOOL] searchDestinations 결과: {}개 여행지 발견", destinations.size());
        return result.toString();
    }

    @Tool(description = "여행지 ID로 상세 정보를 조회합니다. 주요 관광명소와 설명을 확인할 수 있습니다.")
    public String getDestinationDetails(
            @ToolParam(description = "여행지 ID (예: DEST-001)")
            String destinationId) {
        logger.info("[TOOL] getDestinationDetails 호출됨 - id: {}", destinationId);

        Destination dest = TRAVEL_DESTINATIONS.get(destinationId);
        if (dest == null) {
            String result = "여행지 ID '" + destinationId + "'를 찾을 수 없습니다.";
            logger.info("[TOOL] getDestinationDetails 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== ").append(dest.name).append(" 여행 가이드 ===\n");
        result.append("위치: ").append(dest.country).append(" - ").append(dest.region).append("\n");
        result.append("최적 여행 시즌: ").append(dest.bestSeason).append("\n");
        result.append("예상 경비: ").append(String.format("%,d원/일", dest.estimatedBudget)).append("\n\n");

        result.append("[주요 관광명소]\n");
        for (String attraction : dest.attractions) {
            result.append("- ").append(attraction).append("\n");
        }

        result.append("\n[소개]\n").append(dest.description).append("\n");

        logger.info("[TOOL] getDestinationDetails 결과 반환");
        return result.toString();
    }

    // ==================== 6. 영화 정보 Tools ====================

    @Tool(description = "영화를 검색합니다. 장르별로 필터링하거나 전체 목록을 볼 수 있습니다.")
    public String searchMovies(
            @ToolParam(description = "장르 (ALL, SF, 드라마, 액션, 코미디, 스릴러 등)")
            String genre) {
        logger.info("[TOOL] searchMovies 호출됨 - genre: {}", genre);

        List<Movie> movies = MOVIE_DATABASE.values().stream()
            .filter(m -> genre.equals("ALL") || m.genre.contains(genre))
            .sorted((a, b) -> Double.compare(b.rating, a.rating))
            .collect(Collectors.toList());

        if (movies.isEmpty()) {
            String result = "장르 '" + genre + "'에 해당하는 영화가 없습니다.";
            logger.info("[TOOL] searchMovies 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 영화 목록 ===\n");
        for (Movie movie : movies) {
            result.append("\n[").append(movie.id).append("] ").append(movie.title).append("\n");
            result.append("감독: ").append(movie.director).append(" (").append(movie.releaseYear).append(")\n");
            result.append("장르: ").append(movie.genre).append(" | ");
            result.append("러닝타임: ").append(movie.runningTime).append("분\n");
            result.append("평점: ★ ").append(movie.rating).append("/10\n");
            result.append("---\n");
        }

        logger.info("[TOOL] searchMovies 결과: {}개 영화 발견", movies.size());
        return result.toString();
    }

    @Tool(description = "영화 ID 또는 제목으로 상세 정보를 조회합니다. 출연진, 줄거리 등을 확인할 수 있습니다.")
    public String getMovieDetails(
            @ToolParam(description = "영화 ID (예: MOVIE-001) 또는 영화 제목 (예: 인셉션, 기생충)")
            String movieIdOrTitle) {
        logger.info("[TOOL] getMovieDetails 호출됨 - movieIdOrTitle: {}", movieIdOrTitle);

        Movie movie = MOVIE_DATABASE.get(movieIdOrTitle);

        // ID로 찾지 못했으면 제목으로 검색
        if (movie == null) {
            movie = MOVIE_DATABASE.values().stream()
                .filter(m -> m.title.equalsIgnoreCase(movieIdOrTitle) || m.title.contains(movieIdOrTitle))
                .findFirst()
                .orElse(null);
        }

        if (movie == null) {
            String result = "영화 '" + movieIdOrTitle + "'를 찾을 수 없습니다.";
            logger.info("[TOOL] getMovieDetails 결과: {}", result);
            return result;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== ").append(movie.title).append(" ===\n");
        result.append("영화 ID: ").append(movie.id).append("\n");
        result.append("감독: ").append(movie.director).append("\n");
        result.append("개봉년도: ").append(movie.releaseYear).append("년\n");
        result.append("장르: ").append(movie.genre).append("\n");
        result.append("러닝타임: ").append(movie.runningTime).append("분\n");
        result.append("평점: ★ ").append(movie.rating).append("/10\n\n");

        result.append("[주요 출연진]\n");
        for (String actor : movie.cast) {
            result.append("- ").append(actor).append("\n");
        }

        result.append("\n[줄거리]\n").append(movie.synopsis).append("\n");

        logger.info("[TOOL] getMovieDetails 결과 반환");
        return result.toString();
    }

    // ==================== Helper Methods ====================

    private String getWeatherConditionKorean(String condition) {
        switch (condition) {
            case "SUNNY": return "맑음";
            case "CLOUDY": return "흐림";
            case "RAINY": return "비";
            case "SNOWY": return "눈";
            default: return condition;
        }
    }

    private String getAirQualityKorean(String quality) {
        switch (quality) {
            case "GOOD": return "좋음";
            case "MODERATE": return "보통";
            case "BAD": return "나쁨";
            default: return quality;
        }
    }

    private String getCategoryKorean(String category) {
        switch (category) {
            case "KOREAN": return "한식";
            case "WESTERN": return "양식";
            case "CHINESE": return "중식";
            case "JAPANESE": return "일식";
            case "DESSERT": return "디저트";
            default: return category;
        }
    }

    private String getDifficultyKorean(String difficulty) {
        switch (difficulty) {
            case "EASY": return "쉬움";
            case "MEDIUM": return "보통";
            case "HARD": return "어려움";
            default: return difficulty;
        }
    }

    private String getExerciseTypeKorean(String type) {
        switch (type) {
            case "RUNNING": return "러닝";
            case "CYCLING": return "사이클";
            case "SWIMMING": return "수영";
            case "WEIGHT_TRAINING": return "웨이트 트레이닝";
            case "YOGA": return "요가";
            default: return type;
        }
    }

    // ==================== 7. 역사적 사실 Tools ====================

    @Tool(description = "유명한 역사적 사건의 발생 연도를 조회합니다.")
    public String getHistoricalEventYear(
            @ToolParam(description = "역사적 사건명 (예: 한국전쟁, 프랑스혁명, 베를린장벽붕괴)")
            String eventName) {
        logger.info("[TOOL] getHistoricalEventYear 호출됨 - eventName: {}", eventName);

        String result;
        switch (eventName) {
            case "한국전쟁":
                result = "한국전쟁은 1950년 6월 25일에 발발하여 1953년 7월 27일 휴전협정이 체결되었습니다.";
                break;
            case "프랑스혁명":
                result = "프랑스혁명은 1789년에 시작되어 1799년까지 이어졌습니다. 1789년 7월 14일 바스티유 감옥 습격이 상징적 사건입니다.";
                break;
            case "베를린장벽붕괴":
                result = "베를린 장벽은 1989년 11월 9일에 붕괴되었으며, 이는 냉전 종식의 상징적 사건이었습니다.";
                break;
            case "제1차세계대전":
                result = "제1차 세계대전은 1914년 7월 28일에 시작되어 1918년 11월 11일에 종전되었습니다.";
                break;
            case "제2차세계대전":
                result = "제2차 세계대전은 1939년 9월 1일 독일의 폴란드 침공으로 시작되어 1945년 9월 2일 일본의 항복으로 종전되었습니다.";
                break;
            case "대한민국정부수립":
                result = "대한민국 정부는 1948년 8월 15일에 수립되었습니다.";
                break;
            case "3.1운동":
                result = "3.1운동은 1919년 3월 1일에 시작된 한국의 독립 만세 운동입니다.";
                break;
            case "미국독립선언":
                result = "미국 독립선언은 1776년 7월 4일에 발표되었습니다.";
                break;
            default:
                result = "'" + eventName + "' 사건에 대한 정보가 데이터베이스에 없습니다. 다른 사건명을 입력해주세요.";
        }

        logger.info("[TOOL] getHistoricalEventYear 결과: {}", result);
        return result;
    }

    @Tool(description = "유명 인물의 출생 및 사망 연도를 조회합니다. LLM이 이미 알고 있는 일반적인 역사 지식입니다.")
    public String getHistoricalFigureInfo(
            @ToolParam(description = "역사적 인물명 (예: 세종대왕, 나폴레옹, 아인슈타인)")
            String personName) {
        logger.info("[TOOL] getHistoricalFigureInfo 호출됨 - personName: {}", personName);

        String result;
        switch (personName) {
            case "세종대왕":
                result = "세종대왕(1397-1450)은 조선의 제4대 왕으로, 한글을 창제하고 과학 기술과 문화를 발전시킨 성군입니다.";
                break;
            case "나폴레옹":
                result = "나폴레옹 보나파르트(1769-1821)는 프랑스 제1제국의 황제로, 유럽 대륙을 정복하려 했던 군사 지도자입니다.";
                break;
            case "아인슈타인":
                result = "알베르트 아인슈타인(1879-1955)은 독일 출신의 물리학자로, 상대성이론을 제시하여 현대 물리학의 기초를 마련했습니다.";
                break;
            case "링컨":
                result = "에이브러햄 링컨(1809-1865)은 미국의 제16대 대통령으로, 노예제 폐지와 남북전쟁을 이끈 지도자입니다.";
                break;
            case "이순신":
                result = "이순신(1545-1598)은 조선시대의 명장으로, 임진왜란 때 거북선을 이용하여 왜군을 격퇴한 장군입니다.";
                break;
            case "간디":
                result = "마하트마 간디(1869-1948)는 인도의 독립운동가로, 비폭력 저항 운동을 통해 영국으로부터 독립을 이끌어냈습니다.";
                break;
            case "마르틴루터킹":
                result = "마틀 루터 킹 주니어(1929-1968)는 미국의 인권운동가로, 비폭력 시민 저항을 통해 흑인 인권 향상에 기여했습니다.";
                break;
            case "셰익스피어":
                result = "윌리엄 셰익스피어(1564-1616)는 영국의 극작가이자 시인으로, '햄릿', '로미오와 줄리엣' 등 불멸의 작품을 남겼습니다.";
                break;
            case "다윈":
                result = "찰스 다윈(1809-1882)은 영국의 생물학자로, 진화론과 자연선택설을 제시하여 생물학에 혁명을 일으켰습니다.";
                break;
            case "콜럼버스":
                result = "크리스토퍼 콜럼버스(1451-1506)는 이탈리아 출신 탐험가로, 1492년 아메리카 대륙을 발견했습니다.";
                break;
            default:
                result = "'" + personName + "' 인물에 대한 정보가 데이터베이스에 없습니다. 다른 인물명을 입력해주세요.";
        }

        logger.info("[TOOL] getHistoricalFigureInfo 결과: {}", result);
        return result;
    }
}
