package com.kfzs.dodonewrpc;

import android.util.Log; // 导入用于日志记录的库

import com.kfzs.dodonewrpc.HttpClient; // 导入自定义的 HttpClient 类

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 定义 AsyncHttpClient 类，用于异步发送 HTTP 请求
public class AsyncHttpClient {
    // 定义日志标签
    private static final String TAG = "r0posed_LSP";

    // 声明一个 ExecutorService，用于管理线程池
    private ExecutorService executorService;

    // 声明一个 HttpClient，用于执行 HTTP 请求
    private HttpClient httpClient;

    // AsyncHttpClient 构造方法，初始化线程池和 HttpClient
    public AsyncHttpClient(String proxyHost, int proxyPort) {
        // 创建一个线程池，用于处理异步任务
        this.executorService = Executors.newCachedThreadPool();
        // 初始化 HttpClient，传入代理主机和端口
        this.httpClient = new HttpClient(proxyHost, proxyPort);
    }

    // 发送 POST 请求的异步方法，传入 URL、数据和回调接口
    public void sendPostRequest(String url, String data, ParamCallback callback) {
        // 创建 CountDownLatch 对象，初始计数为 1，用于同步线程
        CountDownLatch latch = new CountDownLatch(1);

        // 提交任务到线程池执行
        executorService.submit(() -> {
            // 使用 HttpClient 发送 POST 请求，并获取响应
            String response = httpClient.postRequest(url, data);
            // 调用回调方法，将响应传回
            callback.onResponse(response);
            // 任务完成，减少计数
            latch.countDown();
        });

        try {
            // 等待任务完成，即 CountDownLatch 的计数减为 0
            latch.await();
        } catch (InterruptedException e) {
            // 如果等待过程中被中断，记录错误日志
            Log.e(TAG, "请求被中断", e);
        }
    }

    // 定义回调接口，用于在请求完成后处理响应
    public interface ParamCallback {
        void onResponse(String response); // 接收 HTTP 响应
    }
}
