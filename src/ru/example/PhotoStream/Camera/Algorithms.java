package ru.example.PhotoStream.Camera;

import java.util.Comparator;
import java.util.Random;

public class Algorithms {

    private static <T> int partition(T[] array, Comparator<T> comparator, int left, int right, int pivotIndex) {
        T tmp = array[pivotIndex];
        array[pivotIndex] = array[right];
        array[right] = tmp;
        T pivotValue = array[pivotIndex];
        int swapPosition = left;
        for (int i = left; i < right; i++) {
            if (comparator.compare(array[i], pivotValue) < 0) {
                tmp = array[i];
                array[i] = array[swapPosition];
                array[swapPosition] = tmp;
                swapPosition++;
            }
        }
        tmp = array[right];
        array[right] = array[swapPosition];
        array[swapPosition] = tmp;
        return swapPosition;
    }

    private static <T extends Comparable<T>> int partition(T[] array, int left, int right, int pivotIndex) {
        T tmp = array[pivotIndex];
        array[pivotIndex] = array[right];
        array[right] = tmp;
        T pivotValue = array[pivotIndex];
        int swapPosition = left;
        for (int i = left; i < right; i++) {
            if (array[i].compareTo(pivotValue) < 0) {
                tmp = array[i];
                array[i] = array[swapPosition];
                array[swapPosition] = tmp;
                swapPosition++;
            }
        }
        tmp = array[right];
        array[right] = array[swapPosition];
        array[swapPosition] = tmp;
        return swapPosition;
    }

    private static int partition(float[] array, int left, int right, int pivotIndex) {
        float tmp = array[pivotIndex];
        array[pivotIndex] = array[right];
        array[right] = tmp;
        float pivotValue = array[pivotIndex];
        int swapPosition = left;
        for (int i = left; i < right; i++) {
            if (array[i] < pivotValue) {
                tmp = array[i];
                array[i] = array[swapPosition];
                array[swapPosition] = tmp;
                swapPosition++;
            }
        }
        tmp = array[right];
        array[right] = array[swapPosition];
        array[swapPosition] = tmp;
        return swapPosition;
    }

    /**
     * Puts the least k elements in array's positions from 0 to k - 1 in any order, kth element is put strictly in the array[k-1].
     * @param array
     * @param comparator
     * @param k
     * @param <T> elements' type.
     */

    public static <T> void orderStatistics(T[] array, Comparator<T> comparator, int k) {
        Random random = new Random(System.currentTimeMillis());
        if (k < 0 || k >= array.length) {
            throw new RuntimeException("Wrong partition: k is " + k + " while array's length is " + array.length);
        }
        int left = 0, right = array.length - 1, pivotIndex;
        while (left < right) {
            pivotIndex = left + random.nextInt(right - left + 1);
            pivotIndex = partition(array, comparator, left, right, pivotIndex);
            if (pivotIndex == k - 1) {
                return;
            } else if (pivotIndex < k - 1) {
                left = pivotIndex + 1;
            } else {
                right = pivotIndex - 1;
            }
        }
    }

    /**
     * Puts the least k elements in array's positions from 0 to k - 1 in any order, kth element is put strictly in the array[k-1].
     * @param array
     * @param k
     * @param <T> elements' type.
     */
    public static <T extends Comparable<T>> void orderStatistics(T[] array, int k) {
        Random random = new Random(System.currentTimeMillis());
        if (k < 0 || k >= array.length) {
            throw new RuntimeException("Wrong partition: k is " + k + " while array's length is " + array.length);
        }
        int left = 0, right = array.length - 1, pivotIndex;
        while (left < right) {
            pivotIndex = left + random.nextInt(right - left + 1);
            pivotIndex = partition(array, left, right, pivotIndex);
            if (pivotIndex == k - 1) {
                return;
            } else if (pivotIndex < k - 1) {
                left = pivotIndex + 1;
            } else {
                right = pivotIndex - 1;
            }
        }
    }

    /**
     * Puts the least k elements in array's positions from 0 to k - 1 in any order, kth element is put strictly in the array[k-1].
     * @param array
     * @param k
     */
    public static void orderStatistics(float[] array, int k) {
        Random random = new Random(System.currentTimeMillis());
        if (k < 0 || k >= array.length) {
            throw new RuntimeException("Wrong partition: k is " + k + " while array's length is " + array.length);
        }
        int left = 0, right = array.length - 1, pivotIndex;
        while (left < right) {
            pivotIndex = left + random.nextInt(right - left + 1);
            pivotIndex = partition(array, left, right, pivotIndex);
            if (pivotIndex == k - 1) {
                return;
            } else if (pivotIndex < k - 1) {
                left = pivotIndex + 1;
            } else {
                right = pivotIndex - 1;
            }
        }
    }
}
