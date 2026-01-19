package com.study.myspringstudydiary.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "diary_entries")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Diary(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    // 비즈니스 메서드
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateDiary(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // 연관관계 편의 메서드
    protected void setUser(User user) {
        this.user = user;
    }
}