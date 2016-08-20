package com.eson.asyntask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;

import com.eson.adapter.ContactAdapter;
import com.eson.domain.Contact;
import com.eson.service.ContactService;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity {
    ListView listView;
    File cache; // 缓存文件

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            listView.setAdapter(new ContactAdapter(MainActivity.this,
                    (List<Contact>) msg.obj, R.layout.listview_item, cache));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) this.findViewById(R.id.listView);

        cache = new File(Environment.getExternalStorageDirectory(), "cache"); // 实例化缓存文件
        if (!cache.exists())
            cache.mkdirs(); // 如果文件不存在，创建

        // 开一个线程来加载数据
        new Thread(new Runnable() {
            public void run() {
                try {
                    List<Contact> data = ContactService.getContacts();
                    // 通过handler来发送消息
                    handler.sendMessage(handler.obtainMessage(22, data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        // 删除缓存
        for (File file : cache.listFiles()) {
            file.delete();
        }
        cache.delete();
        super.onDestroy();
    }
}
