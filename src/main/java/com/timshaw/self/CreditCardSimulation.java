package com.timshaw.self;

import java.util.*;

public class CreditCardSimulation {

    public static void main(String[] args) {
        // 定义商户列表
        List<String> merchants = Arrays.asList(
                "汽车", "养生", "烟酒", "茶社", "料理", "酒店", "高尔夫", "珠宝"
        );

        // 随机数生成器
        Random random = new Random();

        // 模拟10个月的消费记录
        int totalMonths = 10;
        int monthlyTotal = 8000; // 每月总金额
        int totalSpent = 0;

        System.out.println("信用卡消费记录模拟：\n");

        for (int month = 1; month <= totalMonths; month++) {
            // 洗牌商户列表
            List<String> shuffledMerchants = new ArrayList<>(merchants);
            Collections.shuffle(shuffledMerchants, random);

            // 生成8笔消费记录
            List<Integer> amounts = new ArrayList<>();
            boolean success = false;

            while (!success) {
                amounts.clear();
                int sum = 0;

                // 生成前7笔金额（958-1042之间，非整百）
                for (int i = 0; i < 7; i++) {
                    int amount;
                    do {
                        // 生成958到1042之间的随机金额
                        amount = random.nextInt(85) + 958; // 1042 - 958 + 1 = 85
                    } while (amount % 100 == 0); // 排除整百金额
                    amounts.add(amount);
                    sum += amount;
                }

                // 计算第8笔金额
                int lastAmount = monthlyTotal - sum;

                // 检查第8笔金额是否符合条件
                if (lastAmount >= 701 && lastAmount <= 1299 && lastAmount % 100 != 0) {
                    amounts.add(lastAmount);
                    success = true;
                }
            }

            // 输出本月消费记录
            System.out.println("第 " + month + " 月消费记录：");
            for (int i = 0; i < 8; i++) {
                System.out.printf("商户: %s, 金额: %d\n",
                        shuffledMerchants.get(i), amounts.get(i));
            }

            // 累计总金额
            totalSpent += monthlyTotal;
            System.out.println("本月累计消费: " + monthlyTotal + "\n");
        }

        System.out.println("10个月总消费: " + totalSpent);
    }
}