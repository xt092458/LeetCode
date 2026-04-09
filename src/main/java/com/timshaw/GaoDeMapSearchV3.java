package com.timshaw;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import okhttp3.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;

public class GaoDeMapSearchV3 {

    // 高德地图API Key（请替换为您的实际API Key）
    private static final String API_KEY = "d60e59aa63e8b78dfabb0aa1b373e436";
    // 高德地图地理编码API URL
    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";
    // 高德地图周边搜索API URL
    private static final String PLACE_AROUND_URL = "https://restapi.amap.com/v3/place/around";

    // 配置参数
    private static final int DEFAULT_RADIUS_SMALL = 1000; // 1000米范围
    private static final int DEFAULT_RADIUS_LARGE = 3000; // 3000米范围
    private static final String[] SMALL_KEYWORDS = {"小区", "学校", "超市"};
    private static final String LARGE_KEYWORD = "商场";

    public static void main(String[] args) {
        // 检查命令行参数
        if (args.length == 0) {
            System.out.println("使用说明:");
            System.out.println("  java GaoDeMapSearch \"地点1\" \"地点2\" ...");
            System.out.println("  示例: java GaoDeMapSearch \"北国超市(西美花街店)\" \"石家庄火车站\"");
            System.out.println("  未提供地点，将使用默认地点: \"北国超市(西美花街店)\"");
            args = new String[]{"河北省石家庄市长安区中山东路739号北国超市(益中店)","河北省石家庄市桥西区汇丰路8号北国超市(西美花街店)"};
        }

        // 获取所有地点
        List<String> locations = Arrays.asList(args);

        // 生成Excel文件
        generateExcelReport(locations);
    }

    /**
     * 生成Excel报告，包含多个地点的周边设施
     * @param locations 要搜索的地点列表
     */
    private static void generateExcelReport(List<String> locations) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("高德地图周边设施报告3.xlsx")) {

            // 为每个地点创建工作表
            for (String locationName : locations) {
                Sheet sheet = workbook.createSheet(formatSheetName(locationName));
                createSheetHeader(sheet);

                // 获取地点坐标
                String location = getCoordinates(locationName);
                if (location == null) {
                    System.out.println("警告: 无法获取地点坐标 - " + locationName);
                    continue;
                }

                System.out.println("正在搜索: " + locationName + " 周边设施...");

                // 搜索1000米范围内的小区、学校、超市
                List<String[]> smallResults = searchNearby(location, DEFAULT_RADIUS_SMALL, SMALL_KEYWORDS);
                // 搜索3000米范围内的商场
                List<String[]> largeResults = searchNearby(location, DEFAULT_RADIUS_LARGE, LARGE_KEYWORD);

                // 合并结果
                List<String[]> allResults = new ArrayList<>();
                allResults.addAll(smallResults);
                allResults.addAll(largeResults);

                // 填充数据
                int rowIndex = 1;
                for (String[] result : allResults) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(result[0]);
                    row.createCell(1).setCellValue(result[1]);
                    row.createCell(2).setCellValue(result[2]);
                }

                System.out.println("完成: " + locationName + " (" + (rowIndex - 1) + " 条记录)");
            }

            // 保存Excel文件
            workbook.write(fileOut);
            System.out.println("\nExcel文件已生成: 高德地图周边设施报告.xlsx");
            System.out.println("共包含 " + locations.size() + " 个地点，" +
                    (locations.stream().mapToInt(loc -> {
                        String locCoord = getCoordinates(loc);
                        if (locCoord == null) return 0;
                        return searchNearby(locCoord, DEFAULT_RADIUS_SMALL, SMALL_KEYWORDS).size()
                                + searchNearby(locCoord, DEFAULT_RADIUS_LARGE, LARGE_KEYWORD).size();
                    }).sum()) + " 条周边设施记录");

        } catch (IOException e) {
            System.out.println("生成Excel文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 格式化工作表名称（移除特殊字符，保留31个字符）
     * @param name 原始名称
     * @return 格式化后的工作表名称
     */
    private static String formatSheetName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll(" ", "_")
                .substring(0, Math.min(31, name.length()));
    }

    /**
     * 获取地点的经纬度坐标
     * @param locationName 地点名称
     * @return 经纬度字符串，格式为 "经度,纬度"
     */
    private static String getCoordinates(String locationName) {
        try {
            // 构建地理编码API请求URL
            String url = GEOCODE_URL + "?key=" + API_KEY + "&address=" +
                    java.net.URLEncoder.encode(locationName, "UTF-8");

            // 发送HTTP请求
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return parseGeoResponse(responseBody);
            } else {
                System.out.println("地理编码请求失败 (" + locationName + "): " + response.code());
            }
        } catch (Exception e) {
            System.out.println("地理编码请求异常 (" + locationName + "): " + e.getMessage());
        }

        return null;
    }

    /**
     * 解析地理编码API的响应
     * @param json API返回的JSON字符串
     * @return 经纬度字符串，格式为 "经度,纬度"
     */
    private static String parseGeoResponse(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if ("1".equals(jsonObject.get("status").getAsString())) {
                JsonArray geocodes = jsonObject.getAsJsonArray("geocodes");
                if (geocodes.size() > 0) {
                    JsonObject geo = geocodes.get(0).getAsJsonObject();
                    return geo.get("location").getAsString();
                }
            }
        } catch (Exception e) {
            System.out.println("解析地理编码响应失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 搜索指定地点周边的设施
     * @param location 经纬度坐标
     * @param radius 搜索范围（米）
     * @param keywords 搜索关键词（可以是数组或单个关键词）
     * @return 与关键词匹配的设施列表
     */
    private static List<String[]> searchNearby(String location, int radius, String... keywords) {
        List<String[]> results = new ArrayList<>();

        try {
            // 构建周边搜索API请求URL
            String keywordsStr = String.join("|", keywords);
            String url = PLACE_AROUND_URL + "?key=" + API_KEY + "&location=" + location +
                    "&radius=" + radius + "&keywords=" + keywordsStr + "&extensions=all";

            // 发送HTTP请求
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return parseResponse(responseBody, keywordsStr);
            } else {
                System.out.println("周边搜索请求失败: " + response.code());
            }
        } catch (Exception e) {
            System.out.println("周边搜索请求异常: " + e.getMessage());
        }

        return results;
    }

    /**
     * 解析周边搜索API的响应
     * @param json API返回的JSON字符串
     * @param keywords 搜索关键词
     * @return 解析后的设施信息列表
     */
    private static List<String[]> parseResponse(String json, String keywords) {
        List<String[]> results = new ArrayList<>();

        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if ("1".equals(jsonObject.get("status").getAsString())) {
                JsonArray pois = jsonObject.getAsJsonArray("pois");

                for (int i = 0; i < pois.size(); i++) {
                    JsonObject poi = pois.get(i).getAsJsonObject();
                    String name = poi.get("name").getAsString();
                    String distance = poi.get("distance").getAsString();

                    String type;
                    if (keywords.contains("小区")) {
                        type = "居民小区";
                    } else if (keywords.contains("学校")) {
                        type = "学校";
                    } else if (keywords.contains("超市")) {
                        type = "超市/便利店";
                    } else if (keywords.contains("商场")) {
                        type = "商场";
                    } else {
                        type = "其他";
                    }

                    results.add(new String[]{type, name, distance});
                }
            } else {
                System.out.println("API返回错误: " + jsonObject.get("info").getAsString());
            }
        } catch (Exception e) {
            System.out.println("解析周边搜索响应失败: " + e.getMessage());
        }

        return results;
    }

    /**
     * 创建工作表标题行
     * @param sheet 工作表
     */
    private static void createSheetHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("地点类型");
        headerRow.createCell(1).setCellValue("地点名称");
        headerRow.createCell(2).setCellValue("距离（米）");

        // 设置列宽
        sheet.setColumnWidth(0, 3500);
        sheet.setColumnWidth(1, 10000);
        sheet.setColumnWidth(2, 3500);

        // 添加样式
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 应用样式到标题行
        for (int i = 0; i < 3; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
    }
}