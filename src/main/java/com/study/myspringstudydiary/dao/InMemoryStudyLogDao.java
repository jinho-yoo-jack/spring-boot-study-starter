package com.study.myspringstudydiary.dao;

import com.study.myspringstudydiary.common.Page;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Category;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * StudyLogDao의 메모리 기반 구현체 (In-Memory Implementation)
 *
 * DIP(Dependency Inversion Principle) 준수:
 * - 고수준 모듈(Service)이 저수준 모듈(InMemoryStudyLogDao)에 의존하지 않음
 * - 둘 다 추상화(StudyLogDao 인터페이스)에 의존
 * - 구현체는 언제든 교체 가능 (InMemory → MySQL → MongoDB 등)
 *
 * 사용 목적:
 * - 개발 초기 단계나 테스트 환경에서 사용
 * - 데이터베이스 없이 빠르게 프로토타이핑 가능
 * - 단위 테스트 시 유용
 *
 * @Repository 어노테이션:
 * - Spring Bean으로 등록
 * - 데이터 접근 계층 명시
 * - 데이터 접근 예외를 Spring의 DataAccessException으로 변환
 *
 * @Primary 어노테이션:
 * - 동일한 타입의 Bean이 여러 개일 때 우선 선택
 * - 현재 InMemoryStudyLogDao와 MySQLStudyLogDaoImpl이 둘 다 StudyLogDao 구현
 * - Service에서 StudyLogDao 주입 시 이 클래스가 우선 주입됨
 */
@Repository
public class InMemoryStudyLogDao implements StudyLogDao {

    // 데이터 저장소 (실제 DB 대신 Map 사용)
    private final Map<Long, StudyLog> database = new HashMap<>();

    // ID 자동 증가를 위한 시퀀스
    private final AtomicLong sequence = new AtomicLong(1);

    // ========== CREATE ==========

    @Override
    public StudyLog save(StudyLog studyLog) {
        // ID가 없으면 새로운 ID 부여
        if (studyLog.getId() == null) {
            studyLog.setId(sequence.getAndIncrement());
        }

        // Map에 저장
        database.put(studyLog.getId(), studyLog);

        return studyLog;
    }

    // ========== READ ==========

    @Override
    public Optional<StudyLog> findById(Long id) {
        // Map에서 ID로 조회하고 Optional로 감싸서 반환
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<StudyLog> findAll() {
        // Map의 모든 값을 리스트로 변환하여 반환
        // 최신 순으로 정렬
        return database.values().stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudyLog> findByCategory(String category) {
        try {
            Category categoryEnum = Category.valueOf(category.toUpperCase());
            return findByCategory(categoryEnum);
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<StudyLog> findByCategory(Category category) {
        return database.values().stream()
                .filter(log -> log.getCategory() == category)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudyLog> findByStudyDate(LocalDate date) {
        return database.values().stream()
                .filter(log -> log.getStudyDate().equals(date))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return database.containsKey(id);
    }

    @Override
    public long count() {
        return database.size();
    }

    // ========== PAGING ==========

    @Override
    public Page<StudyLog> findAllWithPaging(int page, int size) {
        List<StudyLog> allLogs = new ArrayList<>(database.values());
        allLogs.sort((a, b) -> b.getId().compareTo(a.getId()));

        int totalElements = allLogs.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        if (fromIndex >= totalElements) {
            return new Page<>(new ArrayList<>(), page, size, totalElements);
        }

        List<StudyLog> content = allLogs.subList(fromIndex, toIndex);
        return new Page<>(content, page, size, totalElements);
    }

    @Override
    public Page<StudyLog> findByCategoryWithPaging(Category category, int page, int size) {
        List<StudyLog> categoryLogs = database.values().stream()
                .filter(log -> log.getCategory() == category)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());

        int totalElements = categoryLogs.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        if (fromIndex >= totalElements) {
            return new Page<>(new ArrayList<>(), page, size, totalElements);
        }

        List<StudyLog> content = categoryLogs.subList(fromIndex, toIndex);
        return new Page<>(content, page, size, totalElements);
    }

    @Override
    public Page<StudyLog> findByDateWithPaging(LocalDate date, int page, int size) {
        List<StudyLog> dateLogs = database.values().stream()
                .filter(log -> log.getStudyDate().equals(date))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());

        int totalElements = dateLogs.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        if (fromIndex >= totalElements) {
            return new Page<>(new ArrayList<>(), page, size, totalElements);
        }

        List<StudyLog> content = dateLogs.subList(fromIndex, toIndex);
        return new Page<>(content, page, size, totalElements);
    }

    @Override
    public Page<StudyLog> searchWithPaging(String titleKeyword, Category category,
                                          LocalDate startDate, LocalDate endDate,
                                          int page, int size) {
        List<StudyLog> filteredLogs = database.values().stream()
                .filter(log -> {
                    boolean matches = true;

                    // 제목 키워드 필터
                    if (titleKeyword != null && !titleKeyword.isBlank()) {
                        matches = log.getTitle().contains(titleKeyword);
                    }

                    // 카테고리 필터
                    if (matches && category != null) {
                        matches = log.getCategory() == category;
                    }

                    // 시작 날짜 필터
                    if (matches && startDate != null) {
                        matches = !log.getStudyDate().isBefore(startDate);
                    }

                    // 종료 날짜 필터
                    if (matches && endDate != null) {
                        matches = !log.getStudyDate().isAfter(endDate);
                    }

                    return matches;
                })
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());

        int totalElements = filteredLogs.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        if (fromIndex >= totalElements) {
            return new Page<>(new ArrayList<>(), page, size, totalElements);
        }

        List<StudyLog> content = filteredLogs.subList(fromIndex, toIndex);
        return new Page<>(content, page, size, totalElements);
    }

    // ========== UPDATE ==========

    @Override
    public StudyLog update(StudyLog studyLog) {
        if (studyLog.getId() == null) {
            throw new IllegalArgumentException("수정할 학습 일지의 ID가 없습니다.");
        }
        if (!database.containsKey(studyLog.getId())) {
            throw new IllegalArgumentException(
                "해당 학습 일지를 찾을 수 없습니다. (id: " + studyLog.getId() + ")");
        }

        // updatedAt 갱신
        studyLog.setUpdatedAt(java.time.LocalDateTime.now());

        database.put(studyLog.getId(), studyLog);
        return studyLog;
    }

    // ========== DELETE ==========

    @Override
    public boolean deleteById(Long id) {
        // Map.remove()는 삭제된 값을 반환, 없으면 null 반환
        StudyLog removed = database.remove(id);
        return removed != null;
    }

    @Override
    public void deleteAll() {
        database.clear();
        // 테스트 용도로 시퀀스도 초기화
        sequence.set(1);
    }

    // ========== 생명주기 콜백 ==========

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("📦 InMemoryStudyLogDao 초기화 완료!");
        System.out.println("   - StudyLogDao 인터페이스 구현");
        System.out.println("   - 메모리 기반 데이터 저장소(HashMap) 준비");
        System.out.println("   - 페이징 기능 지원");
        System.out.println("   - ID 생성기 준비됨");
        System.out.println("   - DIP 원칙 준수");
        System.out.println("========================================");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("========================================");
        System.out.println("🧹 InMemoryStudyLogDao 정리 중...");
        System.out.println("   - 저장된 데이터 수: " + database.size());
        System.out.println("   - 마지막 ID: " + (sequence.get() - 1));
        database.clear();  // 데이터 정리
        System.out.println("   - 메모리 데이터 정리 완료!");
        System.out.println("========================================");
    }
}