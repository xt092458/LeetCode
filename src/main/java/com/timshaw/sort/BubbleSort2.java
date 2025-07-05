package com.timshaw.sort;

import java.util.Arrays;

public class BubbleSort2 {

    public static void bubbleSort(int[] arr){
        if(arr == null || arr.length < 2){
            return;
        }
        for(int end = arr.length -1; end >0 ; end --){
            for(int i = 1 ; i <= end ; i++){
                if(arr[i] < arr[i-1]){
                    int temp = arr[i];
                    arr[i] = arr[i-1];
                    arr[i-1] = temp;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{1,3,5,7,9,8,4,6,2};
        bubbleSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}
