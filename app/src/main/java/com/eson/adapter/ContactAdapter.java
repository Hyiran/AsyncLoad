package com.eson.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eson.asyntask.R;
import com.eson.domain.Contact;
import com.eson.service.ContactService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Administrator on 2016/8/20.
 */
public class ContactAdapter extends BaseAdapter {
    private List<Contact> data; // 缓存数据
    private int listviewItem; // 条目id
    private File cache; // 缓存文件
    LayoutInflater layoutInflater;

    public ContactAdapter(Context context, List<Contact> data,
                          int listviewItem, File cache) {
        this.data = data;
        this.listviewItem = listviewItem;
        this.cache = cache;
        layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);// 获取布局填充服务
    }

    /**
     * 得到数据的总数
     */
    public int getCount() {
        return data.size();
    }

    /**
     * 根据数据索引得到集合所对应的数据
     */
    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * 当listView每显示一个条目的时候，都会调用这个方法
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1获取item,再得到控件
        // 2 获取数据
        // 3绑定数据到item
        ImageView imageView = null;
        TextView textView = null;

        if (convertView == null) {
            convertView = layoutInflater.inflate(listviewItem, null); // 获取条目的view对象
            imageView = (ImageView) convertView.findViewById(R.id.imageView);
            textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(new DataWrapper(imageView, textView));
        } else {
            DataWrapper dataWrapper = (DataWrapper) convertView.getTag();
            imageView = dataWrapper.imageView;
            textView = dataWrapper.textView;
        }
        Contact contact = data.get(position);
        //x界面显示图片和文字
        // 异步的加载图片 (线程池 + Handler ) ---> AsyncTask
        textView.setText(contact.name);
        asyncImageLoad(imageView, contact.image);
        return convertView;
    }

    private void asyncImageLoad(ImageView imageView, String path) {

        AsyncImageTask task = new AsyncImageTask(imageView);
        task.execute(path);

    }

    /**
     * 使用AsyncTask异步加载图片
     *
     * @author Administrator
     */
    private final class AsyncImageTask extends AsyncTask<String, Void, Uri> {
        private ImageView imageView;

        public AsyncImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Uri doInBackground(String... params) {// 子线程中执行的
            String imgUrl = params[0];
            try {

                return ContactService.getImage(imgUrl, cache);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Uri result) {// 运行在主线程
            super.onPostExecute(result);
            imageView.setImageURI(result);
            if (result != null && imageView != null)
                imageView.setImageURI(result);
        }


        //

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    /**
     * 使用Handler进行异步加载图片,采用普通方式异步的加载图片
     * @param imageView
     * @param path
     */
    private void asyncImageLoad2(final ImageView imageView, final String path) {
        final Handler handler = new Handler() {
            public void
            handleMessage(Message msg) {//运行在主线程中
                Uri uri = (Uri) msg.obj;
                if (uri != null && imageView != null) imageView.setImageURI(uri);
            }
        };

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Uri uri =
                            ContactService.getImage(path, cache);
                    handler.sendMessage(handler.obtainMessage(10, uri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    private final class DataWrapper {
        public ImageView imageView;
        public TextView textView;

        public DataWrapper(ImageView imageView, TextView textView) {
            this.imageView = imageView;
            this.textView = textView;
        }
    }
}
