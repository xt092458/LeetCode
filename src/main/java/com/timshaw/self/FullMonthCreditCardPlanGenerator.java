package com.timshaw.self;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class FullMonthCreditCardPlanGenerator {

    // --- 1. 定义核心约束条件 ---
    private static final List<String> BANKS = List.of(
            "工商银行", "建设银行", "农业银行", "中国银行", "邮储银行", "交通银行", "招商银行"
    );
    private static final int TRANSACTION_COUNT = 35;
    private static final int CARD_USAGE_PER_CARD = 5;
    private static final int TOTAL_AMOUNT = 7000;

    // 定义一个记录类来存储单次刷卡计划的详情
    public record RepaymentDetail(
            LocalDateTime dateTime,
            String bankName,
            int amount
    ) {
        public String toFormattedString() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dayOfWeek = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);
            String time = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

            return String.format("日期: %s (%s), 银行: %-5s, 时间: %s, 金额: %d",
                    dateTime.format(dateFormatter),
                    dayOfWeek,
                    bankName,
                    time,
                    amount
            );
        }
    }

    /**
     * 主方法：生成还款计划
     * @return 按日期和时间排序的35次刷卡计划列表
     */
    public List<RepaymentDetail> generatePlan() {
        List<Integer> amounts = generateIntegerAmounts();
        List<String> bankSchedule = generateBankSchedule();
        List<LocalDate> dateSlots = generateDateSlotsCoveringAllWeekdays(); // 使用新的日期生成算法

        if (dateSlots.size() < TRANSACTION_COUNT) {
            System.err.println("错误：工作日不足，无法生成计划。");
            return Collections.emptyList();
        }

        List<RepaymentDetail> preliminaryPlan = new ArrayList<>();
        for (int i = 0; i < TRANSACTION_COUNT; i++) {
            preliminaryPlan.add(new RepaymentDetail(
                    dateSlots.get(i).atStartOfDay(),
                    bankSchedule.get(i),
                    amounts.get(i)
            ));
        }

        List<RepaymentDetail> optimizedPlan = distributeEvenly(preliminaryPlan);
        return assignFinalRandomTimes(optimizedPlan);
    }

    /**
     * 【已优化】步骤2c: 生成35个日期卡槽，确保覆盖目标月份的所有工作日
     */
    private List<LocalDate> generateDateSlotsCoveringAllWeekdays() {
        List<LocalDate> finalSlots = new ArrayList<>();
        List<LocalDate> uniqueWeekdaysForMonth = new ArrayList<>();

        // 为下一个完整月份生成计划，这样更干净、更实用
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfTargetMonth = today.plusMonths(1).withDayOfMonth(1);
        LocalDate dayIterator = firstDayOfTargetMonth;
        String targetMonthName = firstDayOfTargetMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"));
        System.out.printf("正在为【%s】生成计划...\n", targetMonthName);


        // 1. 找出目标月份中所有的工作日 (周一到周五)
        while (dayIterator.getMonth() == firstDayOfTargetMonth.getMonth()) {
            DayOfWeek day = dayIterator.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                uniqueWeekdaysForMonth.add(dayIterator);
            }
            dayIterator = dayIterator.plusDays(1);
        }

        if (uniqueWeekdaysForMonth.isEmpty()) {
            System.err.println("错误：目标月份中未找到任何工作日。");
            return Collections.emptyList();
        }

        // 2. 将所有找到的唯一工作日都至少加入一次，确保全覆盖
        finalSlots.addAll(uniqueWeekdaysForMonth);

        // 3. 计算还需要补充多少笔交易才能达到35笔
        int remainingSlots = TRANSACTION_COUNT - finalSlots.size();
        if (remainingSlots > 0) {
            // 为了随机地在某些天安排第二笔交易，先打乱唯一工作日列表
            List<LocalDate> tempPickList = new ArrayList<>(uniqueWeekdaysForMonth);
            Collections.shuffle(tempPickList);

            // 从打乱的列表中循环挑选，补足剩余的交易卡槽
            for (int i = 0; i < remainingSlots; i++) {
                finalSlots.add(tempPickList.get(i % tempPickList.size()));
            }
        } else if (remainingSlots < 0) {
            // 如果某个月份工作日超过35天(不可能)，则随机选取35天
            Collections.shuffle(finalSlots);
            return finalSlots.subList(0, TRANSACTION_COUNT);
        }

        // 4. 最后，将包含重复日期的完整列表按时间排序
        Collections.sort(finalSlots);

        return finalSlots;
    }

    /**
     * 步骤2a: 生成满足总额约束的【整数】金额列表 (平均200)
     */
    private List<Integer> generateIntegerAmounts() {
        List<Integer> amounts = new ArrayList<>();
        for (int i = 0; i < TRANSACTION_COUNT; i++) {
            amounts.add(200);
        }

        for (int i = 0; i < 300; i++) {
            int index1 = ThreadLocalRandom.current().nextInt(TRANSACTION_COUNT);
            int index2 = ThreadLocalRandom.current().nextInt(TRANSACTION_COUNT);
            int transferAmount = ThreadLocalRandom.current().nextInt(1, 50);

            if (index1 != index2 && amounts.get(index1) > transferAmount + 50) {
                amounts.set(index1, amounts.get(index1) - transferAmount);
                amounts.set(index2, amounts.get(index2) + transferAmount);
            }
        }

        for (int i = 0; i < TRANSACTION_COUNT; i++) {
            if (amounts.get(i) % 100 == 0) {
                int targetIndex = (i + 1) % TRANSACTION_COUNT;
                amounts.set(i, amounts.get(i) + 1);
                amounts.set(targetIndex, amounts.get(targetIndex) - 1);
            }
        }
        Collections.shuffle(amounts);
        return amounts;
    }

    /**
     * 步骤2b: 生成基础的银行卡刷卡顺序 (每张5次)
     */
    private List<String> generateBankSchedule() {
        List<String> masterSchedule = new ArrayList<>();
        for (int i = 0; i < CARD_USAGE_PER_CARD; i++) {
            masterSchedule.addAll(BANKS);
        }
        Collections.shuffle(masterSchedule);
        return masterSchedule;
    }

    /**
     * 步骤4: 智能分布算法，解决同一天刷同一张卡的问题
     */
    private List<RepaymentDetail> distributeEvenly(List<RepaymentDetail> plan) {
        for (int sweep = 0; sweep < 20; sweep++) { // 增加扫描次数以应对更复杂的场景
            boolean swappedInSweep = false;
            for (int i = 0; i < plan.size(); i++) {
                for (int j = i + 1; j < plan.size(); j++) {
                    RepaymentDetail item1 = plan.get(i);
                    RepaymentDetail item2 = plan.get(j);

                    if (item1.dateTime().toLocalDate().equals(item2.dateTime().toLocalDate()) &&
                            item1.bankName().equals(item2.bankName())) {

                        int k = ThreadLocalRandom.current().nextInt(plan.size());
                        if (k != i && k != j && isSwapSafe(plan, j, k)) {
                            Collections.swap(plan, j, k);
                            swappedInSweep = true;
                        }
                    }
                }
            }
            if (!swappedInSweep) {
                break; // 如果一轮扫描没有任何交换，说明已达稳定状态
            }
        }
        return plan;
    }

    /**
     * 辅助方法：检查一次交换是否安全（即不会产生新的冲突）
     */
    private boolean isSwapSafe(List<RepaymentDetail> plan, int swapFromIndex, int swapToIndex) {
        RepaymentDetail itemToMove = plan.get(swapFromIndex);
        RepaymentDetail itemToBeReplaced = plan.get(swapToIndex);

        // 检查 itemToMove 移动到新位置后，是否会与新日期的其他卡冲突
        for (RepaymentDetail detail : plan) {
            if (detail.dateTime().toLocalDate().equals(itemToBeReplaced.dateTime().toLocalDate()) &&
                    detail.bankName().equals(itemToMove.bankName())) {
                return false;
            }
        }
        // 检查 itemToBeReplaced 移动到旧位置后，是否会与旧日期的其他卡冲突
        for (RepaymentDetail detail : plan) {
            if (detail.dateTime().toLocalDate().equals(itemToMove.dateTime().toLocalDate()) &&
                    detail.bankName().equals(itemToBeReplaced.bankName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 步骤5: 为计划列表分配最终的随机时间并排序
     */
    private List<RepaymentDetail> assignFinalRandomTimes(List<RepaymentDetail> plan) {
        List<RepaymentDetail> finalPlan = new ArrayList<>();
        plan.stream()
                .collect(Collectors.groupingBy(detail -> detail.dateTime().toLocalDate()))
                .forEach((date, detailsOnSameDay) -> {
                    List<LocalTime> times = new ArrayList<>();
                    while (times.size() < detailsOnSameDay.size()) {
                        int hour = ThreadLocalRandom.current().nextInt(10, 21);
                        int minute = ThreadLocalRandom.current().nextInt(0, 60);
                        LocalTime newTime = LocalTime.of(hour, minute);
                        if (!times.contains(newTime)) {
                            times.add(newTime);
                        }
                    }
                    Collections.sort(times);

                    for (int i = 0; i < detailsOnSameDay.size(); i++) {
                        RepaymentDetail oldDetail = detailsOnSameDay.get(i);
                        finalPlan.add(new RepaymentDetail(
                                LocalDateTime.of(date, times.get(i)),
                                oldDetail.bankName(),
                                oldDetail.amount()
                        ));
                    }
                });

        finalPlan.sort(Comparator.comparing(RepaymentDetail::dateTime));
        return finalPlan;
    }

    // --- 主执行函数 ---
    public static void main(String[] args) {
        FullMonthCreditCardPlanGenerator generator = new FullMonthCreditCardPlanGenerator();
        List<RepaymentDetail> plan = generator.generatePlan();

        if (plan.isEmpty()) {
            return;
        }

        System.out.println("====== 本月信用卡消费计划 (全工作日覆盖版) ======");
        plan.forEach(detail -> System.out.println(detail.toFormattedString()));

        int finalSum = plan.stream().mapToInt(RepaymentDetail::amount).sum();

        System.out.println("=========================================");
        System.out.printf("计划总金额: %d 元 (目标: 7000 元)\n", finalSum);
        System.out.println("====== 每次重新运行，都将生成全新的随机计划 ======");
    }
}
