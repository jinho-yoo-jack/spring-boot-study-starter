package com.study.myspringstudydiary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static class Cup {
        private String name;
        private int size;
        private int maxiumSize;

        public Cup(String name, int size, int maxiumSize) {
            this.name = name;
            this.size = size;
            this.maxiumSize = maxiumSize;
        }

        public int addCup(int size) {
            return this.size += size;
        }
    }

    public static class ImmutableCup {
        private final String name;
        private final int size;
        private final int maxiumSize;

        public ImmutableCup(String name, int size, int maxiumSize) {
            this.name = name;
            this.size = size;
            this.maxiumSize = maxiumSize;
        }

        public ImmutableCup addCup(int size) {
            return new ImmutableCup(name, this.size + size, maxiumSize);
        }
    }

    public static void main(String[] args) {
        Cup c = new Cup("Water", 1, 10);
        c.addCup(2);
        ImmutableCup c0 = new ImmutableCup("Water", 1, 10);
        ImmutableCup c1 = c0.addCup(10);

    }
}