package com.timshaw;

import java.util.Arrays;

public class ArrIndexSum {

    private static int[] getHelpArr(int[] arr){
        if(arr == null){
            return null;
        }
        if(arr.length == 0 || arr.length == 1){
            return arr;
        }
        int indexSum = 0;
        int[] helpArr = new int[arr.length];
        for(int i = 0; i < arr.length; i++){
            indexSum += arr[i];
            helpArr[i] = indexSum;
        }
        return helpArr;
    }

    public static int getIndexSum(int start, int end, int[] arr){
        int[] helpArr = getHelpArr(arr);
        return helpArr[end] - helpArr[start-1];
    }

    public static void main(String[] args) {
        int[] arr = {1,2,3,4,5,6,7,8,9,10};
        System.out.println(getIndexSum(2,4,arr));
    }
}
