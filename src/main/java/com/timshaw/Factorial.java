package com.timshaw;

public class Factorial {

    public static long inner(long n){
        long innerResult = 1;
        for(long i = 1 ; i <= n ; i++){
            innerResult *= i;
        }
        return innerResult;
    }

    public static long factorial(long n){
        if(n <= 0) return 0;
        if(n == 1) return 1;
        long factorial = 0;
        for(long i = 1 ; i <= n ; i++){
            long innerResult = inner(i);
            factorial += innerResult;
        }
        return factorial;
    }

    public static void main(String[] args) {
        long n = 5;
        System.out.println(f2(n));
    }

    public static long f2(long n){
        long result = 0;
        long current = 1;
        for(long i = 1 ; i <= n ; i++){
            current *= i;
            result += current;
        }
        return result;
    }
}
