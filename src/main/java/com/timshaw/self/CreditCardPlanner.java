package com.timshaw.self;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreditCardPlanner {

    // --- 在这里配置您的信用卡和商户信息 ---
    private static final List<String> CARDS = List.of(
            "工商银行", "建设银行", "农业银行", "中国银行", "邮政储蓄", "交通银行"
    );
    private static final List<String> MERCHANTS = List.of(
            "汽车", "养生", "料理", "茶社", "烟酒"
    );

    // --- 核心业务参数 ---
    private static final int CARDS_COUNT = CARDS.size();
    private static final int TRANSACTION_DAYS = 4; // 一个月刷4天 (4个周六)
    private static final int TRANSACTIONS_PER_CARD = 4;
    private static final int TOTAL_TRANSACTIONS = CARDS_COUNT * TRANSACTIONS_PER_CARD; // 24笔

    // --- 金额范围约束 ---
    private static final int MIN_AMOUNT = 401; // 范围下限
    private static final int MAX_AMOUNT = 599; // 范围上限

    private static final Random random = new Random();

    /**
     * 定义交易信息记录 (新增了 merchant 字段)
     */
    public record Transaction(String card, LocalDate date, String merchant, int amount) {}

    public static void main(String[] args) {
        // 生成刷卡计划
        List<Transaction> transactionPlan = generatePlanForCurrentMonth();

        // 格式化并打印计划
        if (transactionPlan != null) {
            printPlan(transactionPlan);
        }
    }

    /**
     * 生成当月的刷卡计划
     * @return 交易列表, 如果不满足条件则返回 null
     */
    public static List<Transaction> generatePlanForCurrentMonth() {
        // 1. 获取并筛选交易日期（必须有4个或更多周六）
        List<LocalDate> transactionSaturdays = selectTransactionSaturdays();
        if (transactionSaturdays == null) {
            return null; // 不满足条件，直接退出
        }

        // 2. 【新增逻辑】为每张卡预先分配好本月要消费的4个不同商户
        Map<String, List<String>> cardToMerchantsMap = generateCardToMerchantMap();

        // 3. 生成24个随机金额
        List<Integer> amounts = generateRandomAmounts();
        Collections.shuffle(amounts); // 打乱金额顺序，用于随机分配

        // 4. 【核心修改】以卡为中心，组合成最终的交易计划
        List<Transaction> plan = new ArrayList<>();
        int amountIndex = 0;

        // 遍历每一张卡
        for (String card : CARDS) {
            // 获取这张卡预分配的4个商户
            List<String> merchantsForCard = cardToMerchantsMap.get(card);
            // 获取这个月要交易的4个周六日期
            List<LocalDate> datesForCard = new ArrayList<>(transactionSaturdays);
            // 将日期也打乱，确保商户和日期的配对是随机的
            Collections.shuffle(datesForCard);

            // 为这张卡的4笔交易创建记录
            for (int i = 0; i < TRANSACTIONS_PER_CARD; i++) {
                LocalDate date = datesForCard.get(i);
                String merchant = merchantsForCard.get(i);
                int amount = amounts.get(amountIndex++);
                plan.add(new Transaction(card, date, merchant, amount));
            }
        }

        // 5. 按日期和卡名排序，使其更易读
        plan.sort(Comparator.comparing(Transaction::date).thenComparing(Transaction::card));
        return plan;
    }

    /**
     * 【新增方法】为每张卡从5个商户中随机选择4个不重复的商户
     */
    private static Map<String, List<String>> generateCardToMerchantMap() {
        Map<String, List<String>> cardToMerchants = new HashMap<>();
        for (String card : CARDS) {
            List<String> merchantsCopy = new ArrayList<>(MERCHANTS);
            Collections.shuffle(merchantsCopy);
            // 取打乱后列表的前4个
            cardToMerchants.put(card, merchantsCopy.subList(0, TRANSACTIONS_PER_CARD));
        }
        return cardToMerchants;
    }

    /**
     * 生成24个随机金额，总额不固定 (与之前版本相同)
     */
    private static List<Integer> generateRandomAmounts() {
        List<Integer> amounts = new ArrayList<>();
        for (int i = 0; i < TOTAL_TRANSACTIONS; i++) {
            int randomAmount;
            do {
                randomAmount = random.nextInt(MAX_AMOUNT - MIN_AMOUNT + 1) + MIN_AMOUNT;
            } while (randomAmount % 100 == 0);
            amounts.add(randomAmount);
        }
        return amounts;
    }

    /**
     * 从当月选择4个周六作为交易日 (与之前版本相同)
     */
    private static List<LocalDate> selectTransactionSaturdays() {
        // 当前日期是 2025-08-27
        YearMonth currentYearMonth = YearMonth.of(2025, 8);
        List<LocalDate> allSaturdays = new ArrayList<>();
        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = currentYearMonth.atDay(day);
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                allSaturdays.add(date);
            }
        }

        if (allSaturdays.size() < TRANSACTION_DAYS) {
            System.out.println("错误：本月 ("+currentYearMonth+") 少于 " + TRANSACTION_DAYS + " 个周六，无法生成满足条件的计划。");
            return null;
        }

        Collections.shuffle(allSaturdays);
        return allSaturdays.subList(0, TRANSACTION_DAYS);
    }

    /**
     * 打印刷卡计划 (更新了打印格式)
     */
    private static void printPlan(List<Transaction> plan) {
        if (plan == null || plan.isEmpty()) return;

        // 当前日期是 2025-08-27
        YearMonth currentYearMonth = YearMonth.of(2025, 8);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 (E)");
        System.out.println("====== " + currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")) + " 信用卡刷卡计划 (含商户) ======");
        System.out.println("------------------------------------------------------------------");

        long totalSum = 0;
        LocalDate currentDate = null;
        for (int i = 0; i < plan.size(); i++) {
            Transaction tx = plan.get(i);
            if (!tx.date().equals(currentDate)) {
                if (currentDate != null) System.out.println();
                currentDate = tx.date();
            }
            // 使用更长的格式化字符串以容纳商户名称
            System.out.printf("第 %2d 笔 | %s | %-6s | %-6s | 金额: %d 元%n",
                    i + 1,
                    tx.date().format(formatter),
                    tx.card(),
                    tx.merchant(), // 新增打印商户
                    tx.amount());
            totalSum += tx.amount();
        }

        System.out.println("------------------------------------------------------------------");
        System.out.println("计划总笔数: " + plan.size());
        System.out.println("计划总金额: " + totalSum + " 元");
        System.out.println("====== 计划生成完毕 ======");
    }
}