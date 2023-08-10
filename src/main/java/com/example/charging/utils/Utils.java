package com.example.charging.utils;

import com.opencsv.*;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.util.*;

import org.apache.commons.math3.util.Precision;

public class Utils {
    public Utils() {
    }

    // 参数：文件路径名， 一个 m x n 的 ArrayList， 表头
    // params: file path name. a 2 x n ArrayList, header
    public void writeToNewCSV(String fileName, List<String[]> list, String[] header) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(fileName));
            int n = list.size();
            writer.writeNext(header);

            for (int i = 0; i < n; i++) {
                writer.writeNext(list.get(i));
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将输入数字限制在三位小数
    public String limitToThreeDecimal(double n) {
        return String.valueOf(Precision.round(n, 3));
    }

    // 返回：离输入数字最近的区间
    public double convertToProperTime(double t) {
        double interval = 0.25;
        return Math.round(t / interval) * interval;
    }

    // 返回：如果输入的时刻大于24小时，将其转为第二天的时刻
    public double convertTimeToNextDay(double currTime) {
        return (currTime > 24) ? (currTime - 24) : currTime;
    }

    // 检查输入的时刻是否在车辆返回与离开时刻内
    public boolean timeIsInRange(double currTime, double returnTime, double leaveTime) {
        // 当两个时刻在同一天
        if (leaveTime > returnTime) {
            return (currTime >= returnTime && currTime <= leaveTime);
        } else { // 第二个时刻于第二天
            return  ((currTime >= returnTime && currTime < 24) || (currTime >= 0 && currTime <= leaveTime));
        }
    }

    public Time convertHoursToExactTime(double timeValue) {
        // Extracting hours, minutes, and seconds
        int hours = (int) timeValue;
        int minutes = (int) ((timeValue - hours) * 60);
        int seconds = (int) (((timeValue - hours) * 60 - minutes) * 60);
        String hourStr = String.valueOf(hours);
        String minStr = String.valueOf(minutes);
        String secStr = String.valueOf(seconds);

        // Creating a Time object
        return Time.valueOf(hourStr + ":" + minStr + ":" + secStr);
    }



//    public static void main(String[] args) {
//        Utils utils = new Utils();
//        System.out.println(utils.convertHoursToExactTime(23.34));
//    }
}
