package com.study.myspringstudydiary.dao;

import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Understanding;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MySQL based StudyLog DAO implementation
 *
 * JdbcTemplate usage:
 * - JDBC helper class provided by Spring
 * - Automatically manages Connection, Statement, etc.
 * - Converts SQL exceptions to Spring's DataAccessException
 * - Reduces boilerplate code
 */
@Repository
@Primary  // 이 구현체를 기본으로 사용
public class MySQLStudyLogDaoImpl implements StudyLogDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor injection
     * JdbcTemplate is automatically registered as Bean by Spring
     */
    public MySQLStudyLogDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("🔗 MySQLStudyLogDaoImpl 초기화 시작");
        System.out.println("DataSource: " + jdbcTemplate.getDataSource());

        // 실제 DB 연결 테스트
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("✅ DB Connection Test 성공! Result: " + result);

            // 테이블 존재 여부 확인
            String tableCheckSql = """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'diary_db'
                AND table_name = 'study_logs'
                """;
            Integer tableExists = jdbcTemplate.queryForObject(tableCheckSql, Integer.class);
            System.out.println("📊 study_logs 테이블 존재: " + (tableExists > 0 ? "YES" : "NO"));

            // 현재 레코드 수 확인
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM study_logs", Long.class);
            System.out.println("📝 현재 저장된 StudyLog 개수: " + count);
        } catch (Exception e) {
            System.err.println("❌ DB Connection Test 실패!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("========================================");
    }

    // ========== CREATE ==========

    @Override
    public StudyLog save(StudyLog studyLog) {
        String sql = """
            INSERT INTO study_logs (title, content, category, understanding, study_time, study_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        // KeyHolder: Object to receive auto-generated ID
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, studyLog.getTitle());
            ps.setString(2, studyLog.getContent());
            ps.setString(3, studyLog.getCategory().name());
            ps.setString(4, studyLog.getUnderstanding().name());
            ps.setInt(5, studyLog.getStudyTime());
            ps.setDate(6, Date.valueOf(studyLog.getStudyDate()));
            return ps;
        }, keyHolder);

        // Set the generated ID to StudyLog object
        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            studyLog.setId(generatedId.longValue());
        }

        return studyLog;
    }

    // ========== READ ==========

    @Override
    public Optional<StudyLog> findById(Long id) {
        String sql = "SELECT * FROM study_logs WHERE id = ?";

        try {
            StudyLog studyLog = jdbcTemplate.queryForObject(sql, studyLogRowMapper, id);
            return Optional.ofNullable(studyLog);
        } catch (Exception e) {
            // Return Optional.empty() when no data exists as exception occurs
            return Optional.empty();
        }
    }

    @Override
    public List<StudyLog> findAll() {
        String sql = "SELECT * FROM study_logs ORDER BY study_date DESC, id DESC";
        return jdbcTemplate.query(sql, studyLogRowMapper);
    }

    @Override
    public List<StudyLog> findByCategory(String category) {
        String sql = "SELECT * FROM study_logs WHERE category = ? ORDER BY study_date DESC, id DESC";
        return jdbcTemplate.query(sql, studyLogRowMapper, category);
    }

    @Override
    public List<StudyLog> findByCategory(Category category) {
        String sql = "SELECT * FROM study_logs WHERE category = ? ORDER BY study_date DESC, id DESC";
        return jdbcTemplate.query(sql, studyLogRowMapper, category.name());
    }

    @Override
    public List<StudyLog> findByStudyDate(LocalDate date) {
        String sql = "SELECT * FROM study_logs WHERE study_date = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, studyLogRowMapper, Date.valueOf(date));
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM study_logs WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM study_logs";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    // ========== UPDATE ==========

    @Override
    public StudyLog update(StudyLog studyLog) {
        String sql = """
            UPDATE study_logs
            SET title = ?, content = ?, category = ?, understanding = ?,
                study_time = ?, study_date = ?
            WHERE id = ?
            """;

        int updated = jdbcTemplate.update(sql,
                studyLog.getTitle(),
                studyLog.getContent(),
                studyLog.getCategory().name(),
                studyLog.getUnderstanding().name(),
                studyLog.getStudyTime(),
                studyLog.getStudyDate(),
                studyLog.getId());

        if (updated == 0) {
            throw new RuntimeException("Study log not found. ID: " + studyLog.getId());
        }

        return studyLog;
    }

    // ========== DELETE ==========

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM study_logs WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);
        return deleted > 0;
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM study_logs";
        jdbcTemplate.update(sql);
    }

    // ========== PRIVATE METHODS ==========

    /**
     * RowMapper: Converts each row of ResultSet to StudyLog object
     * Can be simply implemented with lambda expression
     */
    private final RowMapper<StudyLog> studyLogRowMapper = (rs, rowNum) -> {
        StudyLog studyLog = new StudyLog();
        studyLog.setId(rs.getLong("id"));
        studyLog.setTitle(rs.getString("title"));
        studyLog.setContent(rs.getString("content"));
        studyLog.setCategory(Category.valueOf(rs.getString("category")));
        studyLog.setUnderstanding(Understanding.valueOf(rs.getString("understanding")));
        studyLog.setStudyTime(rs.getInt("study_time"));
        studyLog.setStudyDate(rs.getDate("study_date").toLocalDate());
        return studyLog;
    };
}