package com.timshaw.sort;

public class BubbleSort {

    public static int[] bubbleSort(int[] arr){
        for(int i = 0; i<arr.length-1;i++){
            for(int j = 0; j<arr.length-i ; j++){
                if(arr[j] > arr[j+1]){
                    int tmp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = tmp;
                }
            }
        }
        return  arr;
    }
}
