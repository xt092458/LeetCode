package com.timshaw.sort;

import java.util.Arrays;

public class InsertSort2 {

    public static void insertSort(int[] arr){
        if(arr == null || arr.length < 2) return;
        int length = arr.length;
        for (int i = 1; i < length; i++) {
            for(int j = i ; j > 0 ; j-- ){
                if(arr[j] < arr[j-1]){
                    int temp = arr[j];
                    arr[j] = arr[j-1];
                    arr[j-1] = temp;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{3,4,5,1,2};
        insertSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}
