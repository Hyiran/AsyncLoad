package com.eson.service;

import android.net.Uri;
import android.util.Xml;

import com.eson.domain.Contact;
import com.eson.util.MD5;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/20.
 */
public class ContactService {

    /**
     * 获取联系人
     *
     * @return
     */
    public static List<Contact> getContacts() throws Exception {
        // 服务器文件路径
        String path = "http://192.168.3.216:8080/ListTest/list.xml";
        HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
        conn.setConnectTimeout(5000);   //设置超时5秒
        conn.setRequestMethod("GET");   //设置请求方式
        if (conn.getResponseCode() == 200) {  //连接成功返回码200
            return parseXML(conn.getInputStream());
        }
        return null;
    }

    /**
     * 利用pull解析器对xml文件进行解析
     *
     * @param xml
     * @return
     * @throws Exception
     */
    private static List<Contact> parseXML(InputStream xml) throws Exception {
        List<Contact> contacts = new ArrayList<Contact>();
        Contact contact = null;
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(xml, "UTF-8");
        int event = pullParser.getEventType();  //取得开始文档语法
        while (event != XmlPullParser.END_DOCUMENT) {     //只要不等于文档结束事件，循环解析
            switch (event) {
                case XmlPullParser.START_TAG:       //开始标签
                    if ("contact".equals(pullParser.getName())) {
                        contact = new Contact();
                        contact.id = new Integer(pullParser.getAttributeValue(0));
                    } else if ("name".equals(pullParser.getName())) {
                        contact.name = pullParser.nextText();   //取得后面节点的文本值
                    } else if ("image".equals(pullParser.getName())) {
                        contact.image = pullParser.getAttributeValue(0);    //取得第一个属性的值
                    }
                    break;

                case XmlPullParser.END_TAG:     //结束标签
                    if ("contact".equals(pullParser.getName())) {
                        contacts.add(contact);  //将contact对象添加到集合中
                        contact = null;
                    }
                    break;
            }
            event = pullParser.next();  //去下一个标签
        }
        return contacts;
    }

    /**
     * 获取网络图片,如果图片存在于缓存中，就返回该图片，否则从网络中加载该图片并缓存起来
     *
     * @param path 图片路径
     * @return
     */
    public static Uri getImage(String path, File cache) throws Exception {
        String name = MD5.getMD5(path) + path.substring(path.lastIndexOf("."));
        File file = new File(cache, name);
        // 如果图片存在本地缓存目录，则不去服务器下载
        if (file.exists()) {
            return Uri.fromFile(file);//Uri.fromFile(path)这个方法能得到文件的URI
        } else {
            // 从网络上获取图片
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200) {

                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
                // 返回一个URI对象
                return Uri.fromFile(file);
            }
        }
        return null;
    }

}

