package com.kfzs.dodonewrpc;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "r0posed_LSP"; // 定义一个日志标签，方便在 Logcat 中查找日志

    EditText name;  // 声明 EditText 变量，用于接收用户输入的 IP 地址
    Button btnLogin;  // 声明 Button 变量，用于执行登录或提交操作

    @RequiresApi(api = Build.VERSION_CODES.R)  // 仅在 Android 11 (API 30) 及以上版本使用此方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // 调用父类的 onCreate 方法
        setContentView(R.layout.activity_main);  // 设置当前活动的布局文件为 activity_main

        name = findViewById(R.id.name);  // 通过布局文件中的 ID 找到 EditText 组件，用于输入 IP 地址
        btnLogin = findViewById(R.id.login);  // 通过布局文件中的 ID 找到 Button 组件，用于提交 IP 地址

        // 设置 Button 的点击事件监听器
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户在 EditText 中输入的 IP 地址
                String ip = name.getText().toString();

                // 检查输入的 IP 地址是否为空，如果为空则提示用户
                if (TextUtils.isEmpty(ip)) {
                    Toast.makeText(MainActivity.this, "请输入有效的 IP 地址", Toast.LENGTH_SHORT).show();  // 使用 Toast 提示用户输入
                    return;  // 如果输入为空，停止执行后续操作
                }

                // 创建一个 ArrayList，用于保存需要执行的 shell 命令
                ArrayList<String> cmds = new ArrayList<>();
                // 添加第一个 shell 命令，将用户输入的 IP 地址写入到 /data/local/tmp/save 文件中
                cmds.add("echo " + ip + " > /data/local/tmp/save");
                // 添加第二个 shell 命令，修改 /data/local/tmp/save 文件的权限为 744（即文件拥有者可读写执行，其他用户可读）
                cmds.add("chmod 744 /data/local/tmp/save");

                // 使用 ShellUtils 工具类执行上面定义的 shell 命令，并以 root 权限执行
                ShellUtils.CommandResult result = ShellUtils.execCommand(cmds, true, true);

                // 根据 shell 命令的执行结果，显示相应的提示
                if (result.result == 0) {
                    // 如果结果为 0，表示命令执行成功，显示成功提示
                    Toast.makeText(MainActivity.this, "設置成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 如果结果不为 0，表示命令执行失败，显示失败提示
                    Toast.makeText(MainActivity.this, "設置失败", Toast.LENGTH_SHORT).show();
                }

                // 记录日志，显示设置的 IP 地址
                Log.i(TAG, "设置的 IP = " + ip);
            }
        });
    }
}
