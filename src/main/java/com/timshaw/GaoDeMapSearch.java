package com.timshaw;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import okhttp3.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;

public class GaoDeMapSearch {

    // 高德地图API Key（请替换为您的实际API Key）
    private static final String API_KEY = "d60e59aa63e8b78dfabb0aa1b373e436";
    // 高德地图地理编码API URL
    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";
    // 高德地图周边搜索API URL
    private static final String PLACE_AROUND_URL = "https://restapi.amap.com/v3/place/around";

    public static void main(String[] args) {
        // 指定要搜索的地点名称
        //String locationName = "河北省石家庄市长安区中山东路739号北国超市(益中店)";
        //String locationName = "河北省石家庄市桥西区中山东路188号北国超市(北国商城店)";
        //String locationName = "河北省石家庄市长安区中山路育才街交叉口北国超市(先天下店)";
        //String locationName = "河北省石家庄市裕华区育才街265号北国超市(怀特店)";
        //String locationName = "河北省石家庄市裕华区翟营南大街386号北国超市(益东店)";
        //String locationName = "河北省石家庄市长安区胜利北街243号北国超市(天河店)";
        //String locationName = "河北省石家庄市裕华区天山大街学苑路交口北国超市(益新购物中心店)";
        //String locationName = "河北省石家庄市裕华区建华南大街北国超市(蓝山国际店)";
        String locationName = "河北省石家庄市桥西区槐安西路255号北国超市(勒泰店)";

        // 获取地点的经纬度坐标
        String location = getCoordinates(locationName);
        if (location == null) {
            System.out.println("无法获取地点坐标，请检查API Key或地点名称");
            return;
        }

        // 指定搜索范围（单位：米）
        int radius = 1000;
        // 指定搜索关键词
        List<String> keywords = Arrays.asList("小区");
        //List<String> keywords = Arrays.asList("学校");
        //List<String> keywords = Arrays.asList("超市");
        //List<String> keywords = Arrays.asList("商场");
        // 生成Excel表格
        generateExcelReport(location, radius, keywords);
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
                System.out.println("地理编码请求失败: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 搜索指定地点周边的设施
     * @param location 经纬度坐标
     * @param radius 搜索范围（米）
     * @param keyword 搜索关键词
     * @return 与关键词匹配的设施列表
     */
    private static List<String[]> searchNearby(String location, int radius, String keyword) {
        List<String[]> results = new ArrayList<>();

        try {
            // 构建周边搜索API请求URL
            String url = PLACE_AROUND_URL + "?key=" + API_KEY + "&location=" + location +
                    "&radius=" + radius + "&keywords=" + keyword + "&extensions=all";

            // 发送HTTP请求
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return parseResponse(responseBody, keyword);
            } else {
                System.out.println("周边搜索请求失败: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * 解析周边搜索API的响应
     * @param json API返回的JSON字符串
     * @param keyword 搜索关键词
     * @return 解析后的设施信息列表
     */
    private static List<String[]> parseResponse(String json, String keyword) {
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
                    if (keyword.equals("小区")) {
                        type = "居民小区";
                    } else if (keyword.equals("学校")) {
                        type = "学校";
                    } else if (keyword.equals("超市")) {
                        type = "超市/便利店";
                    } else {
                        type = "商场";
                    }

                    results.add(new String[]{type, name, distance});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * 生成Excel报告
     * @param location 经纬度坐标
     * @param radius 搜索范围（米）
     * @param keywords 搜索关键词列表
     */
    private static void generateExcelReport(String location, int radius, List<String> keywords) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("周边设施报告.xlsx")) {

            Sheet sheet = workbook.createSheet("周边设施");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("地址类型");
            headerRow.createCell(1).setCellValue("地点名称");
            headerRow.createCell(2).setCellValue("距离（米）");

            // 设置列宽
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 3000);

            int rowIndex = 1;
            for (String keyword : keywords) {
                List<String[]> results = searchNearby(location, radius, keyword);
                for (String[] result : results) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(result[0]);
                    row.createCell(1).setCellValue(result[1]);
                    row.createCell(2).setCellValue(result[2]);
                }
            }

            // 保存Excel文件
            workbook.write(fileOut);
            System.out.println("Excel文件已生成: 周边设施报告.xlsx");
            System.out.println("共找到 " + (rowIndex - 1) + " 个周边设施");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}