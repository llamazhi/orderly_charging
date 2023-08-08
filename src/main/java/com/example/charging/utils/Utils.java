package com.example.charging.utils;

import com.opencsv.*;

import java.io.FileWriter;
import java.io.IOException;
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
}
