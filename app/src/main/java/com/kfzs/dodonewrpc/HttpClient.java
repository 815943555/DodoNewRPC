package com.kfzs.dodonewrpc;

import android.util.Log; // 导入日志记录工具

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;


// 定义 HttpClient 类，用于处理带代理的 HTTP 请求
public class HttpClient {

    // 定义日志标签
    private static final String TAG = "r0posed_LSP";

    // 定义代理主机地址和端口
    private String proxyHost;
    private int proxyPort;

    // 构造方法，初始化代理主机地址和端口
    public HttpClient(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    // 发送 POST 请求的方法，传入请求 URL 和数据
    public String postRequest(String urlString, String data) {
        // 用于存储服务器响应数据
        StringBuilder response = new StringBuilder();
        try {
            // 创建 InetSocketAddress，指定代理的 IP 地址和端口
            InetSocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
            // 创建 HTTP 代理对象
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
            // 构造 URL 对象
            URL url = new URL(urlString);
            // 通过代理打开与目标服务器的连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            // 设置请求方法为 POST
            connection.setRequestMethod("POST");
            // 允许输出数据到服务器
            connection.setDoOutput(true);
            // 发送请求数据
            connection.getOutputStream().write(data.getBytes());

            // 获取服务器的响应码
            int responseCode = connection.getResponseCode();
            // 如果响应码为 200，表示请求成功
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取服务器返回的输入流
                InputStream inputStream = connection.getInputStream();
                // 将输入流内容转换为字符串并追加到响应字符串中
                response.append(readStream(inputStream));
                // 记录服务器返回的结果
                Log.d(TAG,"get result from server " + response.toString());
            } else {
                // 如果请求失败，记录状态码
                Log.d(TAG, "请求失败，状态码: " + responseCode);
            }
        } catch (Exception e) {
            // 捕获异常并记录错误日志
            Log.e(TAG, "POST请求失败: ", e);
        }
        // 返回服务器的响应内容
        return response.toString();
    }

    // 辅助方法，用于读取输入流中的数据并转换为字符串
    private String readStream(InputStream in) throws Exception {
        // 创建 ByteArrayOutputStream 来存储读取的数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len; // 用于记录每次读取的字节数
        byte[] buffer = new byte[1024]; // 1kb 大小的缓冲区
        // 循环读取输入流中的数据
        while ((len = in.read(buffer)) != -1) {
            // 将读取的数据写入 ByteArrayOutputStream
            baos.write(buffer, 0, len);
        }
        // 关闭输入流
        in.close();
        // 将 ByteArrayOutputStream 转换为字节数组，并转换为字符串
        return new String(baos.toByteArray());
    }
}
