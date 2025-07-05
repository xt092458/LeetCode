package com.timshaw.sort;

import java.util.Arrays;

public class selectSort2 {

    public static void selectSort(int[] arr){
        if(arr==null||arr.length==0){
            return;
        }
        if(arr.length==1){ return;}
        int n = arr.length;
        for(int i=0;i<n-1;i++){
            int minIndex = i;
            for(int j=i+1;j<n;j++){
                minIndex = arr[j] < arr[minIndex] ? j : minIndex;
            }
            swap(arr,minIndex,i);
        }
    }

    public static void swap(int[] arr,int minInex,int i){
        int temp = arr[minInex];
        arr[minInex] = arr[i];
        arr[i] = temp;
    }
    public static void main(String[] args) {
        int[] arr = new int[]{9,5,8,5,2,0,4,1,3,6,7};
        selectSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}
