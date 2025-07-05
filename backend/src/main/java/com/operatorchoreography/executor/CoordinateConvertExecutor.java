package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 坐标转换执行器
 * 执行经纬度坐标系转换操作
 * 支持WGS84、GCJ02、BD09坐标系之间的转换
 */
@Slf4j
@Component
public class CoordinateConvertExecutor implements BaseExecutor {

    private static final double PI = Math.PI;
    private static final double A = 6378245.0; // 长半轴
    private static final double EE = 0.00669342162296594323; // 偏心率平方

    public ExecutorResult convert(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行坐标转换: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            Double longitude = getDoubleValue(inputData.get("longitude"));
            Double latitude = getDoubleValue(inputData.get("latitude"));
            String fromCrs = (String) inputData.getOrDefault("fromCrs", "WGS84");
            String toCrs = (String) inputData.getOrDefault("toCrs", "GCJ02");
            
            if (longitude == null || latitude == null) {
                log.error("坐标参数为空: templateId={}, longitude={}, latitude={}", 
                        templateId, longitude, latitude);
                throw new IllegalArgumentException("经纬度坐标不能为空");
            }
            
            log.debug("坐标转换参数: templateId={}, 输入坐标=[{}, {}], 从{}转换到{}", 
                    templateId, longitude, latitude, fromCrs, toCrs);
            
            // 执行坐标转换
            double[] convertedCoords = convertCoordinate(longitude, latitude, fromCrs, toCrs);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("convertedLng", convertedCoords[0]);
            outputData.put("convertedLat", convertedCoords[1]);
            outputData.put("originalLng", longitude);
            outputData.put("originalLat", latitude);
            outputData.put("fromCrs", fromCrs);
            outputData.put("toCrs", toCrs);
            outputData.put("message", "坐标转换执行成功");
            
            log.info("坐标转换成功: templateId={}, 输入=[{}, {}], 输出=[{}, {}], {}->{}",
                    templateId, longitude, latitude, convertedCoords[0], convertedCoords[1], fromCrs, toCrs);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("坐标转换执行成功")
                                .withMetadata("fromCrs", fromCrs)
                                .withMetadata("toCrs", toCrs)
                                .withMetadata("originalCoords", "[" + longitude + ", " + latitude + "]")
                                .withMetadata("convertedCoords", "[" + convertedCoords[0] + ", " + convertedCoords[1] + "]");
            
        } catch (Exception e) {
            log.error("坐标转换执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("坐标转换执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("坐标转换执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行坐标转换
     */
    private double[] convertCoordinate(double lng, double lat, String fromCrs, String toCrs) {
        if (fromCrs.equals(toCrs)) {
            return new double[]{lng, lat};
        }
        
        // 转换逻辑：先转换到WGS84，再转换到目标坐标系
        double[] wgs84Coords;
        
        switch (fromCrs.toUpperCase()) {
            case "WGS84":
                wgs84Coords = new double[]{lng, lat};
                break;
            case "GCJ02":
                wgs84Coords = gcj02ToWgs84(lng, lat);
                break;
            case "BD09":
                double[] gcj02Coords = bd09ToGcj02(lng, lat);
                wgs84Coords = gcj02ToWgs84(gcj02Coords[0], gcj02Coords[1]);
                break;
            default:
                throw new IllegalArgumentException("不支持的源坐标系: " + fromCrs);
        }
        
        // 从WGS84转换到目标坐标系
        switch (toCrs.toUpperCase()) {
            case "WGS84":
                return wgs84Coords;
            case "GCJ02":
                return wgs84ToGcj02(wgs84Coords[0], wgs84Coords[1]);
            case "BD09":
                double[] gcj02Result = wgs84ToGcj02(wgs84Coords[0], wgs84Coords[1]);
                return gcj02ToBd09(gcj02Result[0], gcj02Result[1]);
            default:
                throw new IllegalArgumentException("不支持的目标坐标系: " + toCrs);
        }
    }
    
    /**
     * WGS84转GCJ02
     */
    private double[] wgs84ToGcj02(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[]{lng, lat};
        }
        
        double dlat = transformLat(lng - 105.0, lat - 35.0);
        double dlng = transformLng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - EE * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((A * (1 - EE)) / (magic * sqrtmagic) * PI);
        dlng = (dlng * 180.0) / (A / sqrtmagic * Math.cos(radlat) * PI);
        
        return new double[]{lng + dlng, lat + dlat};
    }
    
    /**
     * GCJ02转WGS84
     */
    private double[] gcj02ToWgs84(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[]{lng, lat};
        }
        
        double dlat = transformLat(lng - 105.0, lat - 35.0);
        double dlng = transformLng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - EE * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((A * (1 - EE)) / (magic * sqrtmagic) * PI);
        dlng = (dlng * 180.0) / (A / sqrtmagic * Math.cos(radlat) * PI);
        
        return new double[]{lng - dlng, lat - dlat};
    }
    
    /**
     * GCJ02转BD09
     */
    private double[] gcj02ToBd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * PI * 3000.0 / 180.0);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * PI * 3000.0 / 180.0);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[]{bd_lng, bd_lat};
    }
    
    /**
     * BD09转GCJ02
     */
    private double[] bd09ToGcj02(double lng, double lat) {
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI * 3000.0 / 180.0);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI * 3000.0 / 180.0);
        double gcj_lng = z * Math.cos(theta);
        double gcj_lat = z * Math.sin(theta);
        return new double[]{gcj_lng, gcj_lat};
    }
    
    /**
     * 纬度转换
     */
    private double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 
                     0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    
    /**
     * 经度转换
     */
    private double transformLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 
                     0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
    
    /**
     * 判断是否在中国境外
     */
    private boolean outOfChina(double lng, double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }
    
    /**
     * 安全获取Double值
     */
    private Double getDoubleValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
} 