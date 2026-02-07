package com.study.myspringstudydiary;

import com.study.myspringstudydiary.service.CounterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class CounterServiceTest {

    @Autowired
    private CounterService counterService;

    @Test
    void 동시성_문제_재현() throws InterruptedException {
        int numberOfThreads = 100;
        int incrementsPerThread = 1000;

        // 100개의 스레드가 동시에 시작하도록 설정
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counterService.increment();
                }
                latch.countDown();
            });
        }

        // 모든 스레드가 완료될 때까지 대기
        latch.await();
        executor.shutdown();

        // 기대값: 100 * 1000 = 100,000
        // 실제값: 100,000보다 작은 값 (예: 97,543)
        System.out.println("기대값: " + (numberOfThreads * incrementsPerThread));
        System.out.println("실제값: " + counterService.getCount());

        // 테스트는 실패할 가능성이 높음!
        // assertEquals(100000, counterService.getCount());
    }
}

