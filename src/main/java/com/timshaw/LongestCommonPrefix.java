package com.timshaw;

public class LongestCommonPrefix {
    public static String longestCommonPrefix(String[] strs) {
        if(strs == null || strs.length == 0){
            return "";
        }
        if(strs.length == 1){
            return strs[0];
        }
        StringBuilder sb = new StringBuilder("");
        for(int i = 0;i<strs[0].length();i++){
            String currentPrefix = String.valueOf(strs[0].charAt(i));
            boolean isContinue = false;
            for(int j =1;j<strs.length;j++){
                if(i<strs[j].length()) {
                    String eleChar = String.valueOf(strs[j].charAt(i));
                    if(!currentPrefix.equals(eleChar)){
                        break;
                    }else{
                        if(j == strs.length-1){
                            isContinue = true;
                        }
                        continue;
                    }
                }else{
                    break;
                }
            }
            if(isContinue){
                sb.append(currentPrefix);
            }else{
                break;
            }
        }
        return sb.toString();
    }

    public static void main(String[] args){
        String[] arr1 = {"flower","flow","flight"};
        String prefix1 = longestCommonPrefix(arr1);
        System.out.println(prefix1);

        String[] arr2 = {"dog","racecar","car"};
        String prefix2 = longestCommonPrefix(arr2);
        System.out.println(prefix2);
    }
}
