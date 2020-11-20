package com.timshaw;

import com.sun.xml.internal.ws.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RomanToInt {
    public static int romanToInt(String s) {
        if(s == null || s.length() == 0){
            return 0;
        }
        Map romanMap = new HashMap<String,Integer>();
        romanMap.put("I",1);
        romanMap.put("V",5);
        romanMap.put("X",10);
        romanMap.put("L",50);
        romanMap.put("C",100);
        romanMap.put("D",500);
        romanMap.put("M",1000);
        if(s.length() == 1){
            return (Integer)romanMap.get(s);
        }
        boolean evenNumFlag = false;
        if(s.length() % 2 == 0 ){
            evenNumFlag = true;
        }
        Integer sum = 0;
        for(int i = 0;i<s.length();i+=2){
            Integer loopSum = 0;
            if(evenNumFlag){
                loopSum = calculateLoopSum(i,s,romanMap);
            }else{
                if(i == s.length()-1){
                    loopSum = (Integer)romanMap.get(String.valueOf(s.charAt(i)));
                }else {
                    loopSum = calculateLoopSum(i, s, romanMap);
                }
            }
            sum += loopSum;
        }
        return sum;
    }

    public static Integer calculateLoopSum(int i, String s, Map romanMap){
        Integer loopSum = 0;
        Integer current = (Integer)romanMap.get(String.valueOf(s.charAt(i)));
        Integer next = (Integer)romanMap.get(String.valueOf(s.charAt(i+1)));
        if(current < next){
            loopSum = next - current;
        }else{
            loopSum = next + current;
        }
        return loopSum;
    }

    public static void main(String[] args){
        String s1 = "III";
        Integer result1 = romanToInt2(s1);
        System.out.println(result1);

        String s2 = "IV";
        Integer result2 = romanToInt2(s2);
        System.out.println(result2);

        String s3 = "IX";
        Integer result3 = romanToInt2(s3);
        System.out.println(result3);

        String s4 = "LVIII";
        Integer result4 = romanToInt2(s4);
        System.out.println(result4);

        String s5 = "MCMXCIV";
        Integer result5 = romanToInt2(s5);
        System.out.println(result5);
    }

    public static int romanToInt2(String s){
        if(s == null || s.length() == 0){
            return 0;
        }
        Map romanMap = new HashMap<String,Integer>();
        romanMap.put("I",1);
        romanMap.put("V",5);
        romanMap.put("X",10);
        romanMap.put("L",50);
        romanMap.put("C",100);
        romanMap.put("D",500);
        romanMap.put("M",1000);
        if(s.length() == 1){
            return (Integer)romanMap.get(s);
        }
        Integer i = 0;
        Integer sum = 0;
        while(i < s.length()){
            Integer loopSum = 0;
            Integer current = (Integer) romanMap.get(String.valueOf(s.charAt(i)));
            if(i != s.length()-1) {
                Integer next = (Integer) romanMap.get(String.valueOf(s.charAt(i + 1)));
                if (current < next) {
                    loopSum = next - current;
                    i += 2;
                } else {
                    loopSum = current;
                    i++;
                }
            }else{
                loopSum = current;
                i++;
            }
            sum+=loopSum;
        }
        return sum;
    }

}
