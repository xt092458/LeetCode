package com.timshaw.self;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 信用卡刷卡计划生成器 (Java 11) - 强制总金额=15000版
 *
 * 关键修改点:
 * 1. 废除了严格的 (3, 3, 2) 分布约束，因为它与总金额 15000 冲突。
 * 2. 引入迭代搜索和修正策略，以强制生成的8个随机金额总和等于 15000。
 * 3. 所有其他约束（商户不重复、周末日期、非xx00结尾）保持不变。
 */
public class CreditCardPlanGeneratorFinal {

    private static final int TARGET_TOTAL_AMOUNT = 24000;
    private static final int NUM_TRANSACTIONS = 12;
    private static final int MIN_AMOUNT = 1000; // 1000不能用，所以用1001
    private static final int MAX_AMOUNT = 3000; // 3500也不能用，所以用3499

    // 1. 商户列表
    /*private static final List<String> ALL_MERCHANTS = List.of(
            "茶言1", "烟酒2", "川湘3", "华致4","会所5",
            "KTV6","东英7","徕卡8","华硕9","朝阳10"
    );*/

    private static final List<String> ALL_MERCHANTS = List.of(
            "母婴11", "云南12", "花卉13", "手机14","奢品15",
            "玩具16","图文17","木材18","体育19","茶言1","烟酒2","川湘3"
    );

    // 2. 日期范围定义 (使用当前时间2025年10月，但仍沿用9月14日-10月13日)
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 24);
    private static final LocalDate END_DATE = LocalDate.of(2026, 4, 23);
    private static final List<LocalDate> AVAILABLE_DATES = findWeekends(START_DATE, END_DATE);

    /**
     * 辅助方法：动态查找日期范围内的所有周六和周日
     */
    private static List<LocalDate> findWeekends(LocalDate startInclusive, LocalDate endInclusive) {
        long daysBetween = ChronoUnit.DAYS.between(startInclusive, endInclusive) + 1;

        return Stream.iterate(startInclusive, date -> date.plusDays(1))
                .limit(daysBetween)
                .filter(date -> {
                    DayOfWeek day = date.getDayOfWeek();
                    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || day == DayOfWeek.FRIDAY;
                })
                .collect(Collectors.toList());
    }

    static class Transaction {
        final LocalDate date;
        final String merchant;
        final int amount;

        Transaction(LocalDate date, String merchant, int amount) {
            this.date = date;
            this.merchant = merchant;
            this.amount = amount;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public String toString() {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return String.format("日期: %s (%s), 商户: %-4s, 金额: %d",
                    dtf.format(date),
                    date.getDayOfWeek().toString().substring(0, 3), // 简写星期几
                    merchant,
                    amount);
        }
    }

    /**
     * 生成8笔金额，总和强制等于 15000，且满足格式和不重复约束。
     *
     * 策略: 随机生成8个金额，然后通过一个循环迭代修正，直到它们的总和达到 15000。
     */
    private List<Integer> generateAmounts() {
        List<Integer> amounts = new ArrayList<>(NUM_TRANSACTIONS);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Set<Integer> uniqueCheck = new HashSet<>();

        int totalAttempts = 0;
        final int MAX_ATTEMPTS = 500; // 避免极端情况下的无限循环

        // 1. 尝试找到一组满足条件的初始金额
        while (totalAttempts < MAX_ATTEMPTS) {
            amounts.clear();
            uniqueCheck.clear();

            // a. 随机初始化8个金额
            for (int i = 0; i < NUM_TRANSACTIONS; i++) {
                int amount = random.nextInt(MIN_AMOUNT, MAX_AMOUNT + 1);

                // 强制不以 "00" 结尾
                while (amount % 100 == 0 || uniqueCheck.contains(amount)) {
                    amount = random.nextInt(MIN_AMOUNT, MAX_AMOUNT + 1);
                }
                amounts.add(amount);
                uniqueCheck.add(amount);
            }

            int currentSum = amounts.stream().mapToInt(Integer::intValue).sum();
            int difference = TARGET_TOTAL_AMOUNT - currentSum;

            // b. 修正金额以达到目标总和
            if (difference != 0) {
                // difference > 0 : 需要增加总金额
                // difference < 0 : 需要减少总金额

                // 尝试修正次数
                int fixAttempts = 0;
                while (Math.abs(difference) > 0 && fixAttempts < 100) {
                    // 随机选择一个索引进行修改
                    int indexToChange = random.nextInt(NUM_TRANSACTIONS);
                    int currentAmount = amounts.get(indexToChange);

                    // 随机决定步长 (1到50)
                    int step = random.nextInt(1, 51);

                    int newAmount = currentAmount;
                    int adjustment = 0;

                    if (difference > 0) { // 需要增加
                        adjustment = Math.min(difference, step);
                        newAmount = currentAmount + adjustment;
                    } else { // difference < 0 : 需要减少
                        adjustment = -Math.min(Math.abs(difference), step);
                        newAmount = currentAmount + adjustment;
                    }

                    // 检查新金额是否有效 (在范围内、非xx00结尾、且唯一)
                    if (newAmount >= MIN_AMOUNT && newAmount <= MAX_AMOUNT &&
                            newAmount % 100 != 0 &&
                            !uniqueCheck.contains(newAmount) &&
                            newAmount != currentAmount) { // 确保真的发生了变化

                        // 移除旧金额，更新新金额
                        uniqueCheck.remove(currentAmount);
                        amounts.set(indexToChange, newAmount);
                        uniqueCheck.add(newAmount);

                        difference -= adjustment;
                    }
                    fixAttempts++;
                }
            }

            // c. 检查是否成功达到目标总和
            if (difference == 0) {
                return amounts;
            }

            totalAttempts++;
        }

        throw new IllegalStateException("未能找到总和为 " + TARGET_TOTAL_AMOUNT + " 的8个有效金额组合，请增大 MAX_ATTEMPTS。");
    }

    /**
     * 生成完整的刷卡计划
     */
    public List<Transaction> generatePlan() {
        if (AVAILABLE_DATES.size() < NUM_TRANSACTIONS) {
            throw new RuntimeException("错误：在指定日期范围 " + START_DATE + " 到 " + END_DATE +
                    " 内，只找到了 " + AVAILABLE_DATES.size() +
                    " 个周末，不足8天。请扩大日期范围。");
        }

        // 1. 生成8个强制总和为15000的金额
        List<Integer> amounts = generateAmounts();

        // 2. 随机选择8个商户
        List<String> merchantsCopy = new ArrayList<>(ALL_MERCHANTS);
        Collections.shuffle(merchantsCopy);
        List<String> chosenMerchants = merchantsCopy.subList(0, NUM_TRANSACTIONS);

        // 3. 随机选择8个日期
        List<LocalDate> datesCopy = new ArrayList<>(AVAILABLE_DATES);
        Collections.shuffle(datesCopy);
        List<LocalDate> chosenDates = datesCopy.subList(0, NUM_TRANSACTIONS);

        // 4. 组合成8笔交易
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            transactions.add(new Transaction(
                    chosenDates.get(i),
                    chosenMerchants.get(i),
                    amounts.get(i)
            ));
        }

        // 5. 按日期排序，使输出更清晰
        transactions.sort(java.util.Comparator.comparing(Transaction::getDate));

        return transactions;
    }


    public static void main(String[] args) {
        System.out.println("约束变更：放弃了'金额均匀分布'约束，以满足'总金额必须等于15000'的要求。");
        System.out.println("动态计算日期范围: " + START_DATE + " 至 " + END_DATE);
        System.out.println("找到的可用周末天数: " + AVAILABLE_DATES.size());

        try {
            CreditCardPlanGeneratorFinal generator = new CreditCardPlanGeneratorFinal();
            List<Transaction> plan = generator.generatePlan();

            int totalAmount = 0;
            for (Transaction t : plan) {
                System.out.println(t);
                totalAmount += t.amount;
            }

            System.out.println("\n------------------------------");
            System.out.println("总刷卡次数: " + plan.size());
            System.out.println("总刷卡金额: " + totalAmount);
            System.out.println("金额组合是否唯一: " + (new HashSet<>(plan.stream().map(t -> t.amount).collect(Collectors.toList())).size() == 8));

        } catch (Exception e) {
            System.err.println("生成计划失败: " + e.getMessage());
        }
    }
}