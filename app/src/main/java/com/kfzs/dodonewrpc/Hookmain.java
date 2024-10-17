package com.kfzs.dodonewrpc;

import android.util.Log;
import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

// 实现Xposed框架的接口，用于在指定的应用加载时执行Hook操作
public class Hookmain implements IXposedHookLoadPackage {

    // 定义日志标签
    public String TAG = "r0posed_LSP";

    @Override
    // Xposed的回调方法，当目标应用加载时被触发
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // 检查当前加载的应用包名是否是目标应用 "com.dodonew.online"
        if (loadPackageParam.packageName.equals("com.dodonew.online")) {
            Log.d(TAG, "嘟嘟牛已選中");  // 日志输出，标记目标应用已被选中

            // 通过反射获取目标应用的上下文(Context)
            Context appContext = (Context) XposedHelpers.callMethod(
                    XposedHelpers.callStaticMethod(
                            XposedHelpers.findClass("android.app.ActivityThread", null),
                            "currentActivityThread"
                    ),
                    "getSystemContext"
            );

            // 调用IP地址管理器类，检查是否设置了IP地址
            if (!IpAddressManager.checkIpAddress(appContext)) {
                return;  // 如果IP地址未设置，停止后续的Hook操作
            }

            // Hook目标应用中RequestUtil类的encodeDesMap方法，拦截其参数并进行修改
            XposedHelpers.findAndHookMethod("com.dodonew.online.http.RequestUtil", loadPackageParam.classLoader, "encodeDesMap", java.lang.String.class, java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
                @Override
                // Hook方法调用前执行的操作
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String result = (String) param.args[0];  // 获取第一个参数的值
                    Log.d(TAG, "Request================>" + ": " + result);  // 日志输出请求信息

                    // 获取保存的IP地址
                    String ipAddress = IpAddressManager.getSavedIpAddress(appContext);
                    // 创建AsyncHttpClient实例，并设置IP地址和端口
                    AsyncHttpClient asyncHttpClient = new AsyncHttpClient(ipAddress, 8080);
                    // 构建转发服务器地址
                    String forwardServer = "http://" + ipAddress + ":20801/Request";
                    // 发送POST请求，携带原始请求数据
                    asyncHttpClient.sendPostRequest(forwardServer, result, response -> {
                        param.args[0] = response; // 使用响应数据更新方法参数
                    });
                }
            });

            // Hook目标应用中RequestUtil类的decodeDesJson方法，拦截其返回结果并进行修改
            XposedHelpers.findAndHookMethod("com.dodonew.online.http.RequestUtil", loadPackageParam.classLoader, "decodeDesJson", java.lang.String.class, java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
                @Override
                // Hook方法调用后执行的操作
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String result = (String) param.getResult();  // 获取方法的返回值
                    Log.d(TAG, "Response================>" + ": " + result);  // 日志输出响应信息

                    // 获取保存的IP地址
                    String ipAddress = IpAddressManager.getSavedIpAddress(appContext);
                    // 创建AsyncHttpClient实例，并设置IP地址和端口
                    AsyncHttpClient asyncHttpClient = new AsyncHttpClient(ipAddress, 8080);
                    // 构建转发服务器地址
                    String forwardServer = "http://" + ipAddress + ":20801/Response";
                    // 发送POST请求，携带原始响应数据
                    asyncHttpClient.sendPostRequest(forwardServer, result, response -> {
                        param.setResult(response); // 使用新的响应数据更新返回值
                    });
                }
            });
        }
    }
}
