package com.java.sjq.dataStructure.dynamicprogram;

import java.util.Arrays;

public class Knapsack {
    public static void main(String[] args) {
        double[] weights = {1.5, 2.5, 3.0, 4.5};
        double[] values = {10.0, 20.0, 30.0, 40.0};
        double capacity = 7.5;
        System.out.println("Maximum value: " + knapsack(weights, values, capacity));
    }

    public static double knapsack(double[] weights, double[] values, double capacity) {
        int n = weights.length;
        double[][] dp = new double[n + 1][(int) (capacity * 10) + 1];
        for (double[] row : dp) {
            Arrays.fill(row, -1.0);
        }
        return knapsackHelper(weights, values, capacity, n, dp);
    }

    private static double knapsackHelper(double[] weights, double[] values, double capacity, int n, double[][] dp) {
        if (n == 0 || capacity == 0) {
            return 0;
        }
        if (dp[n][(int) (capacity * 10)] != -1.0) {
            return dp[n][(int) (capacity * 10)];
        }
        if (weights[n - 1] > capacity) {
            dp[n][(int) (capacity * 10)] = knapsackHelper(weights, values, capacity, n - 1, dp);
            return dp[n][(int) (capacity * 10)];
        }
        double include = values[n - 1] + knapsackHelper(weights, values, capacity - weights[n - 1], n - 1, dp);
        double exclude = knapsackHelper(weights, values, capacity, n - 1, dp);
        dp[n][(int) (capacity * 10)] = Math.max(include, exclude);
        return dp[n][(int) (capacity * 10)];
    }
}

