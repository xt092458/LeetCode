package com.timshaw;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class CreditCardPlanGeneratorInt {

    // --- 1. 定义核心约束条件 ---
    private static final List<String> BANKS = List.of(
            "工商银行", "建设银行", "农业银行", "中国银行", "邮储银行", "交通银行", "招商银行"
    );
    private static final int TRANSACTION_COUNT = 21;
    private static final int CARD_USAGE_PER_CARD = 3;
    private static final int TOTAL_AMOUNT = 7000;

    // 定义一个记录类来存储单次刷卡计划的详情
    public record RepaymentDetail(
            LocalDate date,
            String dayOfWeek,
            String bankName,
            int amount, // 金额类型已从 BigDecimal 更改为 int
            String time
    ) {
        // 为了方便查看，重写toString方法
        @Override
        public String toString() {
            // 金额格式化从 "%.2f" 更改为 "%d"
            return String.format("日期: %s (%s), 银行: %-5s, 时间: %s, 金额: %d",
                    date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    dayOfWeek,
                    bankName,
                    time,
                    amount
            );
        }
    }

    /**
     * 主方法：生成还款计划
     * @return 按日期排序的21次刷卡计划列表
     */
    public List<RepaymentDetail> generatePlan() {
        // --- 2. 按顺序准备好满足约束的各项数据 ---
        List<Integer> amounts = generateIntegerAmounts();
        List<String> bankSchedule = generateBankSchedule();
        List<LocalDate> validDates = getValidWeekdays(LocalDate.now());

        if (validDates.size() < TRANSACTION_COUNT) {
            System.err.println("错误：未来工作日不足21天，无法生成计划。");
            return Collections.emptyList();
        }

        // --- 3. 组合数据，生成最终计划 ---
        List<RepaymentDetail> plan = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Locale locale = Locale.CHINA; // 用于输出中文的星期几

        for (int i = 0; i < TRANSACTION_COUNT; i++) {
            LocalDate date = validDates.get(i);
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);

            // 生成随机时间 (10:00 - 20:59)
            int hour = ThreadLocalRandom.current().nextInt(10, 21); // 10点到20点
            int minute = ThreadLocalRandom.current().nextInt(0, 60);
            String time = LocalTime.of(hour, minute).format(timeFormatter);

            plan.add(new RepaymentDetail(
                    date,
                    dayOfWeek,
                    bankSchedule.get(i),
                    amounts.get(i),
                    time
            ));
        }
        return plan;
    }

    /**
     * 步骤2a: 生成满足总额约束的【整数】金额列表 (已优化)
     */
    private List<Integer> generateIntegerAmounts() {
        List<Integer> amounts = new ArrayList<>();
        int baseAmount = TOTAL_AMOUNT / TRANSACTION_COUNT; // 333
        int remainder = TOTAL_AMOUNT % TRANSACTION_COUNT;  // 7

        // 1. 创建基础列表 (14个333, 7个334)
        for (int i = 0; i < TRANSACTION_COUNT; i++) {
            amounts.add(baseAmount);
        }
        for (int i = 0; i < remainder; i++) {
            amounts.set(i, amounts.get(i) + 1);
        }

        // 2. 随机化调整：进行多次“平衡转移”，增加随机性，但总和不变
        for (int i = 0; i < 200; i++) { // 增加迭代次数以获得更好的随机性
            int index1 = ThreadLocalRandom.current().nextInt(TRANSACTION_COUNT);
            int index2 = ThreadLocalRandom.current().nextInt(TRANSACTION_COUNT);
            int transferAmount = ThreadLocalRandom.current().nextInt(1, 30); // 转移1到29元

            // 确保转移后金额仍然是正数且合理
            if (index1 != index2 && amounts.get(index1) > transferAmount + 100) {
                amounts.set(index1, amounts.get(index1) - transferAmount);
                amounts.set(index2, amounts.get(index2) + transferAmount);
            }
        }

        // 3. 最终校验与修正：确保没有金额是整百数
        for (int i = 0; i < TRANSACTION_COUNT; i++) {
            if (amounts.get(i) % 100 == 0) {
                // 如果当前金额是整百，就从它这里拿1块钱给别人
                int targetIndex = (i + 1) % TRANSACTION_COUNT; // 给下一个，避免死循环
                amounts.set(i, amounts.get(i) - 1);
                amounts.set(targetIndex, amounts.get(targetIndex) + 1);
            }
        }

        // 4. 最后打乱顺序
        Collections.shuffle(amounts);
        return amounts;
    }

    /**
     * 步骤2b: 生成满足7天内不重复的银行卡刷卡顺序
     */
    private List<String> generateBankSchedule() {
        List<String> masterSchedule = new ArrayList<>();
        for (int i = 0; i < CARD_USAGE_PER_CARD; i++) {
            List<String> round = new ArrayList<>(BANKS);
            Collections.shuffle(round); // 每一轮（7次）的内部顺序都是随机的
            masterSchedule.addAll(round);
        }
        return masterSchedule;
    }

    /**
     * 步骤2c: 获取从指定日期开始的有效工作日
     */
    private List<LocalDate> getValidWeekdays(LocalDate startDate) {
        List<LocalDate> weekdays = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (weekdays.size() < TRANSACTION_COUNT) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                weekdays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        return weekdays;
    }

    // --- 主执行函数 ---
    public static void main(String[] args) {
        CreditCardPlanGeneratorInt generator = new CreditCardPlanGeneratorInt();
        List<RepaymentDetail> plan = generator.generatePlan();

        if (plan.isEmpty()) {
            return;
        }

        System.out.println("====== 本月信用卡消费计划 (金额为整数) ======");
        plan.forEach(System.out::println);

        // 验证总金额是否正确
        int finalSum = plan.stream()
                .mapToInt(RepaymentDetail::amount) // 使用 mapToInt 求和
                .sum();

        System.out.println("=========================================");
        System.out.printf("计划总金额: %d 元 (目标: 7000 元)\n", finalSum);
        System.out.println("====== 每次重新运行，都将生成全新的随机计划 ======");
    }
}

