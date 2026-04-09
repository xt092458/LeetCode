package com.timshaw.self;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class CreditCardPlannerV2 {

    // ==========================================
    // --- 配置区域：所有的变量都在这里修改 ---
    // ==========================================

    // 1. 金额与频次控制
    private static final int TOTAL_TARGET_AMOUNT = 30000;
    private static final int COUNT_FROM_LOW = 3;   // 低号商户选几个
    private static final int COUNT_FROM_HIGH = 9;  // 高号商户选几个
    private static final int MIN_LIMIT = 1000;     // 单笔最低
    private static final int MAX_LIMIT_LOW = 2000; // 低号商户单笔最高
    private static final int MAX_LIMIT_HIGH = 3500;// 高号商户单笔最高

    // 2. 时间范围
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 14);
    private static final LocalDate END_DATE = LocalDate.of(2026, 10, 13);

    // 3. 允许刷卡的星期（周五、周六、周日）
    private static final Set<DayOfWeek> ALLOWED_DAYS = Set.of(
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    );

    // 4. 商户池
    private static final List<String> POOL_LOW = Arrays.asList(
            "茶言1", "烟酒2", "川湘3", "华致4", "会所5", "KTV6", "东英7", "徕卡8", "华硕9", "朝阳10"
    );
    private static final List<String> POOL_HIGH = Arrays.asList(
            "母婴11", "云南12", "花卉13", "手机14", "奢品15", "玩具16", "图文17", "木材18", "体育19"
    );

    // ==========================================
    // --- 核心逻辑区：非必要请勿修改 ---
    // ==========================================

    public static void main(String[] args) {
        try {
            generatePlan();
        } catch (Exception e) {
            System.err.println("方案生成失败: " + e.getMessage());
        }
    }

    public static void generatePlan() {
        // 1. 获取所有符合“周五六日”的日期
        List<LocalDate> validDates = getFilteredDates();
        int totalNeeded = COUNT_FROM_LOW + COUNT_FROM_HIGH;
        if (validDates.size() < totalNeeded) {
            throw new RuntimeException("日期范围内可用的周五六日不足 " + totalNeeded + " 天");
        }

        // 2. 筛选并随机化商户
        List<String> selectedMerchants = selectRandomMerchants();

        // 3. 计算并校验金额（确保不为整百，总额30000）
        int[] amounts = distributeAmounts(selectedMerchants);

        // 4. 组装并按日期排序
        List<Transaction> result = new ArrayList<>();
        for (int i = 0; i < totalNeeded; i++) {
            result.add(new Transaction(validDates.get(i), selectedMerchants.get(i), amounts[i]));
        }
        result.sort(Comparator.comparing(t -> t.date));

        // 5. 打印
        printResult(result);
    }

    private static List<LocalDate> getFilteredDates() {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = START_DATE; !date.isAfter(END_DATE); date = date.plusDays(1)) {
            if (ALLOWED_DAYS.contains(date.getDayOfWeek())) {
                dates.add(date);
            }
        }
        return dates;
    }

    private static List<String> selectRandomMerchants() {
        List<String> lowPart = new ArrayList<>(POOL_LOW);
        List<String> highPart = new ArrayList<>(POOL_HIGH);
        Collections.shuffle(lowPart);
        Collections.shuffle(highPart);

        List<String> finalSelection = new ArrayList<>();
        finalSelection.addAll(lowPart.subList(0, COUNT_FROM_LOW));
        finalSelection.addAll(highPart.subList(0, COUNT_FROM_HIGH));
        Collections.shuffle(finalSelection); // 再次洗牌，打乱商户顺序
        return finalSelection;
    }

    private static int[] distributeAmounts(List<String> merchants) {
        Random rand = new Random();
        int totalSwipes = merchants.size();
        int[] amounts = new int[totalSwipes];
        boolean solved = false;

        while (!solved) {
            long currentTotal = 0;
            // 初始分配
            for (int i = 0; i < totalSwipes; i++) {
                int max = isHighGroup(merchants.get(i)) ? MAX_LIMIT_HIGH : MAX_LIMIT_LOW;
                int val;
                do {
                    val = rand.nextInt(max - MIN_LIMIT + 1) + MIN_LIMIT;
                } while (val % 100 == 0);
                amounts[i] = val;
                currentTotal += val;
            }

            // 精准校准至 TOTAL_TARGET_AMOUNT
            long diff = TOTAL_TARGET_AMOUNT - currentTotal;
            for (int i = 0; i < totalSwipes && diff != 0; i++) {
                int max = isHighGroup(merchants.get(i)) ? MAX_LIMIT_HIGH : MAX_LIMIT_LOW;
                int step = diff > 0 ? 1 : -1;
                while (diff != 0) {
                    int next = amounts[i] + step;
                    if (next >= MIN_LIMIT && next <= max && next % 100 != 0) {
                        amounts[i] = next;
                        diff -= step;
                    } else {
                        break;
                    }
                }
            }
            if (diff == 0) solved = true;
        }
        return amounts;
    }

    private static boolean isHighGroup(String name) {
        // 简单正则：提取字符串中的数字，判断是否 >= 11
        return Integer.parseInt(name.replaceAll("\\D", "")) >= 11;
    }

    private static void printResult(List<Transaction> list) {
        System.out.println("----------------------------------------------");
        System.out.println(String.format("%-12s | %-10s | %-8s", "消费日期", "商户", "金额"));
        System.out.println("----------------------------------------------");
        for (Transaction t : list) {
            System.out.println(String.format("%-12s | %-10s | %-8d", t.date, t.name, t.amount));
        }
        System.out.println("----------------------------------------------");
        System.out.println("总计刷卡笔数: " + list.size() + " | 总计金额: " +
                list.stream().mapToInt(t -> t.amount).sum());
    }

    static class Transaction {
        LocalDate date; String name; int amount;
        Transaction(LocalDate d, String n, int a) { this.date = d; this.name = n; this.amount = a; }
    }
}