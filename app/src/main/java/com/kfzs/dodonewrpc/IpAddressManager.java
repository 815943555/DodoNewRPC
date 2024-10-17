package com.kfzs.dodonewrpc;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// IpAddressManager 类用于管理保存的IP地址，提供读取和检查功能
public class IpAddressManager {
    // 定义日志标签
    private static final String TAG = "r0posed_LSP";

    // 定义保存IP地址的文件路径
    private static final String IP_FILE_PATH = "/data/local/tmp/save";

    // 读取保存的IP地址
    public static String getSavedIpAddress(Context context) {
        // 创建指向保存IP地址文件的File对象
        File file = new File(IP_FILE_PATH);
        // 如果文件不存在，记录日志并返回null
        if (!file.exists()) {
            Log.e(TAG, "IP 地址文件不存在");
            return null;
        }
        // 用于存储读取的IP地址内容
        StringBuilder sb = new StringBuilder();
        // 使用 try-with-resources 读取文件内容
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            // 按行读取文件内容并拼接到 StringBuilder 中
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            // 如果发生IO异常，记录错误日志，并返回null
            Log.e(TAG, "读取 IP 地址时发生错误", e);
            return null;
        }
        // 返回读取的IP地址字符串
        return sb.toString();
    }

    // 验证是否设置了IP地址，如果未设置则提示用户
    public static boolean checkIpAddress(Context context) {
        // 调用 getSavedIpAddress 方法获取已保存的IP地址
        String ipAddress = getSavedIpAddress(context);
        // 如果IP地址为空，弹出提示并记录日志
        if (ipAddress == null || ipAddress.isEmpty()) {
            Toast.makeText(context, "IP 地址未设置，请检查！", Toast.LENGTH_LONG).show();
            Log.e(TAG, "IP 地址未设置，停止 Hook 操作");
            return false;
        }
        // 输出已读取的IP地址到日志中
        Log.d(TAG, "IP 地址: " + ipAddress);
        // 返回true表示IP地址已设置
        return true;
    }
}
