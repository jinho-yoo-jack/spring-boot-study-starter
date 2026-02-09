package com.study.myspringstudydiary.service;

import org.springframework.stereotype.Service;

@Service
public class CounterService {

    // ❌ 위험! 공유되는 인스턴스 변수
    private int count = 0;

    public int increment() {
        count = count + 1;  // 이 연산은 원자적이지 않음!
        return count;
    }

    public int getCount() {
        return count;
    }
}
