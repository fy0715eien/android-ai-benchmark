package com.example.fy071.classifier.util;

public class AccuracyCalculator {
    private int length = 0;

    private int truePredict = 0;

    public double getAccuracy() {
        return (double) truePredict / length;
    }

    public void addResult(Boolean b) {
        if (b) {
            truePredict++;
        }
        length++;
    }
}
