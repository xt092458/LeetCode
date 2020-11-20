package com.timshaw.sort;

import java.util.Arrays;

public class HillSort {

    public static int[] hillSort(int[] arr){
        int n = arr.length;
        int h = 1;
        while(h<n/2){
            h=2*h+1;
        }

        while(h>=1){
            for(int i = h;i<n;i++){
                for(int j = i;j>=h;j-=h){
                    if(arr[j] < arr[j-h]){
                        int tmp = arr[j];
                        arr[j] = arr[j-h];
                        arr[j-h] = tmp;
                    }
                }
            }
            h/=2;
        }
        return arr;
    }

    public static void main(String[] args){
        int[] arr ={9,1,2,5,7,4,8,6,3,5};
        int[] newArr = hillSort(arr);
        System.out.println(Arrays.toString(newArr));
    }
}
