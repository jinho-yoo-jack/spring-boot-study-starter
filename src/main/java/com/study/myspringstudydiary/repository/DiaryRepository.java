package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.entity.Diary;
import com.study.myspringstudydiary.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Page<Diary> findByUser(User user, Pageable pageable);

    Page<Diary> findByUserId(Long userId, Pageable pageable);

    List<Diary> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT d FROM Diary d JOIN FETCH d.user WHERE d.id = :id")
    Optional<Diary> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT d FROM Diary d WHERE d.user.id = :userId AND d.createdAt BETWEEN :startDate AND :endDate")
    List<Diary> findByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT d FROM Diary d WHERE d.title LIKE %:keyword% OR d.content LIKE %:keyword%")
    Page<Diary> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Long countByUserId(Long userId);
}