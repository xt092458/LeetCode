package com.timshaw.sort;

public class SelectSort {

    public static int[] selectSort(int[] arr){
        for(int i = 0;i<arr.length-1;i++){
            for(int j = i+1; j<arr.length;j++){
                if(arr[i] > arr[j]){
                    int tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                }
            }
        }
        return arr;
    }

    public static void main(String[] args){
        int[] arr = {4,6,8,7,9,2,10,1};
        int[] sortArr = selectSort(arr);
        System.out.println(sortArr);
    }
}
