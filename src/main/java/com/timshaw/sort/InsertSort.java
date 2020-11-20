package com.timshaw.sort;

import java.util.Arrays;

public class InsertSort {
    public static int[] insertSort(int[] arr){
        for(int i=1;i<arr.length;i++){
            for(int j = i;j>0;j--){
                if(arr[j] <arr[j-1]){
                    int tmp = arr[j];
                    arr[j] = arr[j-1];
                    arr[j-1] = tmp;
                }
            }
        }
        return arr;
    }

    public static void main(String[] args){
        int[] arr = {4,3,2,10,12,1,5,6};
        int[] insertArr = insertSort(arr);
        System.out.println(Arrays.toString(insertArr));
    }
}
