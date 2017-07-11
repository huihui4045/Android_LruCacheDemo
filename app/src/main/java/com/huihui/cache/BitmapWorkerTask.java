package com.huihui.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.huihui.cache.io.DiskLruCache;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.huihui.cache.StringUtils.hashKeyForDisk;

/**
 * Created by gavin
 * Time 2017/7/10  11:12
 * Email:molu_clown@163.com
 */

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    /**
     * 图片的URL地址
     */
    private String imageUrl;


    private CacheUtils mCacheUtils;

    private DownLaodCallBack downLaodCallBack;



    public BitmapWorkerTask(String imageUrl, CacheUtils mCacheUtils,DownLaodCallBack downLaodCallBack) {
        this.imageUrl = imageUrl;
        this.mCacheUtils = mCacheUtils;
        this.downLaodCallBack=downLaodCallBack;
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        imageUrl = params[0];
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        DiskLruCache.Snapshot snapShot = null;
        try {
            // 生成图片URL对应的key
            final String key = hashKeyForDisk(imageUrl);
            // 查找key对应的缓存
            snapShot = mCacheUtils.DiskLruCacheGet(key);
            if (snapShot == null) {
                // 如果没有找到对应的缓存，则准备从网络上请求数据，并写入缓存
                DiskLruCache.Editor editor = mCacheUtils.DiskLruCacheEdit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (HttpUtils.downloadUrlToStream(imageUrl, outputStream)) {

                        editor.commit();
                    } else {
                        editor.abort();
                    }
                }
                // 缓存被写入后，再次查找key对应的缓存
                snapShot = mCacheUtils.DiskLruCacheGet(key);
            }
            if (snapShot != null) {
                fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                fileDescriptor = fileInputStream.getFD();
            }
            // 将缓存数据解析成Bitmap对象
            Bitmap bitmap = null;
            if (fileDescriptor != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            }
            if (bitmap != null) {
                // 将Bitmap对象添加到内存缓存当中
                mCacheUtils.addBitmapToMemoryCache(params[0], bitmap);
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileDescriptor == null && fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        }


        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        downLaodCallBack.onPostExecute(bitmap);

    }

    public interface DownLaodCallBack {

        void onPostExecute(Bitmap bitmap);
    }
}
