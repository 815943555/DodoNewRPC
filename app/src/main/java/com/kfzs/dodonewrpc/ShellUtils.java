package com.kfzs.dodonewrpc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * ShellUtils
 * <ul>
 * <strong>检查是否有 root 权限</strong>
 * <li>{@link ShellUtils#checkRootPermission()}</li>
 * </ul>
 * <ul>
 * <strong>执行命令</strong>
 * <li>{@link ShellUtils#execCommand(String, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String, boolean, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(List, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(List, boolean, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String[], boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String[], boolean, boolean)}</li>
 * </ul>
 */
public class ShellUtils {
    public static final String COMMAND_SU       = "su"; // root 权限命令
    public static final String COMMAND_SH       = "sh"; // 普通 shell 命令
    public static final String COMMAND_EXIT     = "exit\n"; // 退出 shell 的命令
    public static final String COMMAND_LINE_END = "\n"; // 命令结束标记
    public static String TAG = "r0posed_LSP";

    private ShellUtils() {
        throw new AssertionError();
    }

    /**
     * 检查是否有 root 权限
     *
     * @return 如果有 root 权限返回 true，否则返回 false
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

    /**
     * 执行 shell 命令，默认返回执行结果信息
     *
     * @param command 要执行的命令
     * @param isRoot 是否需要 root 权限执行
     * @return CommandResult 执行结果
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[] {command}, isRoot, true);
    }

    /**
     * 执行 shell 命令，默认返回执行结果信息
     *
     * @param commands 要执行的命令列表
     * @param isRoot 是否需要 root 权限执行
     * @return CommandResult 执行结果
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[] {}), isRoot, true);
    }

    /**
     * 执行 shell 命令，默认返回执行结果信息
     *
     * @param commands 要执行的命令数组
     * @param isRoot 是否需要 root 权限执行
     * @return CommandResult 执行结果
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    /**
     * 执行 shell 命令
     *
     * @param command 要执行的命令
     * @param isRoot 是否需要 root 权限执行
     * @param isNeedResultMsg 是否需要返回结果信息
     * @return CommandResult 执行结果
     */
    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[] {command}, isRoot, isNeedResultMsg);
    }

    /**
     * 执行 shell 命令
     *
     * @param commands 要执行的命令列表
     * @param isRoot 是否需要 root 权限执行
     * @param isNeedResultMsg 是否需要返回结果信息
     * @return CommandResult 执行结果
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[] {}), isRoot, isNeedResultMsg);
    }

    /**
     * 执行 shell 命令
     *
     * @param commands 要执行的命令数组
     * @param isRoot 是否需要 root 权限执行
     * @param isNeedResultMsg 是否需要返回结果信息
     * @return CommandResult 执行结果
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1; // 初始化命令执行结果，-1 表示失败
        if (commands == null || commands.length == 0) { // 如果命令为空或长度为 0
            return new CommandResult(result, null, null); // 返回执行失败的结果
        }

        Process process = null; // 声明进程
        BufferedReader successResult = null; // 用于保存命令执行成功的结果
        BufferedReader errorResult = null; // 用于保存命令执行失败的结果
        StringBuilder successMsg = new StringBuilder(); // 用于拼接成功结果信息
        StringBuilder errorMsg = new StringBuilder(); // 用于拼接失败结果信息
        DataOutputStream os = null; // 用于向 shell 进程发送命令

        try {
            // 根据是否需要 root 权限决定执行 su 或 sh
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream()); // 获取输出流
            for (String command : commands) { // 循环执行每个命令
                if (command == null) {
                    continue; // 如果命令为空，跳过
                }
                Log.d(TAG, "Executing command: " + command); // 打印执行的命令
                os.writeBytes(command + COMMAND_LINE_END); // 写入命令
                os.flush(); // 刷新输出流
            }
            os.writeBytes(COMMAND_EXIT); // 写入退出命令
            os.flush();

            result = process.waitFor(); // 等待命令执行完成，获取执行结果
            // 获取命令执行的结果信息
            if (isNeedResultMsg) { // 如果需要获取返回信息
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream())); // 获取成功的输出流
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream())); // 获取错误的输出流
                String s;
                while ((s = successResult.readLine()) != null) { // 读取成功的信息
                    successMsg.append(s).append("\n"); // 拼接成功信息
                }
                while ((s = errorResult.readLine()) != null) { // 读取错误的信息
                    errorMsg.append(s).append("\n"); // 拼接错误信息
                }
            }
        } catch (IOException e) { // 捕获 IO 异常
            Log.e(TAG, "IOException during command execution", e); // 打印异常日志
        } catch (InterruptedException e) { // 捕获中断异常
            Log.e(TAG, "Command execution interrupted", e); // 打印异常日志
        } finally {
            // 关闭相关流和销毁进程
            try {
                if (os != null) os.close(); // 关闭输出流
                if (successResult != null) successResult.close(); // 关闭成功结果流
                if (errorResult != null) errorResult.close(); // 关闭错误结果流
            } catch (IOException e) {
                Log.e(TAG, "Error closing resources", e); // 捕获关闭流时的异常
            }
            if (process != null) process.destroy(); // 销毁进程
        }
        return new CommandResult(result, successMsg.toString(), errorMsg.toString()); // 返回命令执行结果
    }

    /**
     * 命令执行结果类
     * <ul>
     * <li>{@link CommandResult#result} 代表命令的执行结果，0 表示成功，其他值表示错误，类似于 Linux shell 中的执行结果</li>
     * <li>{@link CommandResult#successMsg} 代表命令执行成功的返回信息</li>
     * <li>{@link CommandResult#errorMsg} 代表命令执行失败的错误信息</li>
     * </ul>
     */
    public static class CommandResult {
        /** 命令执行的结果 **/
        public int result;
        /** 命令成功执行的返回信息 **/
        public String successMsg;
        /** 命令执行失败的错误信息 **/
        public String errorMsg;

        // 只传入执行结果的构造方法
        public CommandResult(int result) {
            this.result = result;
        }

        // 传入执行结果、成功信息和错误信息的构造方法
        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
