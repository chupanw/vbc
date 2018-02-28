package edu.cmu.cs.vbc.benchmark;

/**
 * Useful for measuring steady state performance
 *
 * We need this Java version because of VarexJ
 *
 * @author chupanw
 */
public class SlidingWindown {
    private int capacity;
    private double[] measurements;
    private int size;

    private int firstElementIndex;
    private int nextElementIndex;

    public SlidingWindown(int s) {
        capacity = s;
        measurements = new double[capacity];
        size = 0;

        firstElementIndex = 0;
        nextElementIndex = 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public void add(double m) {
        if (size != capacity) size++;

        measurements[nextElementIndex] = m;

        int inc = (nextElementIndex + 1) % capacity;
        nextElementIndex = inc;
        if (inc == firstElementIndex)
            firstElementIndex = (firstElementIndex + 1) % capacity;
    }

    public double cov() {
        return stdev() / mean();
    }

    private double stdev() {
        double mean = mean();
        double s = 0.0;
        for (int i = 0; i < capacity; i++) {
            double diff = measurements[i] - mean;
            s += diff * diff;
        }
        return Math.sqrt(s / (capacity - 1));
    }

    public double mean() {
        double sum = 0.0;
        for (int i = 0; i < capacity; i++)
            sum += measurements[i];
        return sum / capacity;
    }
}
