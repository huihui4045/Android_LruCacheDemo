package com.huihui.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gavin
 * Time 2017/7/10  11:55
 * Email:molu_clown@163.com
 */

public class PhotoWallAdapter extends ArrayAdapter<String> {

    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private Set<BitmapWorkerTask> taskCollection;

    /**
     * GridView的实例
     */
    private GridView mPhotoWall;

    private CacheUtils mCacheUtils;

    /**
     * 记录每个子项的高度。
     */
    private int mItemHeight = 0;


    public PhotoWallAdapter(Context context, int resource, String[] objects,
                            GridView photoWall) {
        super(context, resource, objects);


        mPhotoWall = photoWall;
        taskCollection = new HashSet<BitmapWorkerTask>();

        mCacheUtils = new CacheUtils(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);

        mItemHeight = point.x / 4;


    }

    /**
     * 将缓存记录同步到journal文件中。
     */
    public void fluchCache() {

        mCacheUtils.fluchCache();
    }

    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask task : taskCollection) {
                task.cancel(false);
            }
        }
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        final String url = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.photo_layout, null);
        } else {
            view = convertView;
        }
        final ImageView imageView = (ImageView) view.findViewById(R.id.photo);

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

        layoutParams.height = mItemHeight;

        layoutParams.width = mItemHeight;

        // 给ImageView设置一个Tag，保证异步加载图片时不会乱序
        imageView.setTag(url);
        imageView.setImageResource(R.mipmap.ic_launcher);
        loadBitmaps(imageView, url);
        return view;


    }

    /**
     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象，
     * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会开启异步线程去下载图片。
     */
    public void loadBitmaps(ImageView imageView, final String imageUrl) {
        try {
            Bitmap bitmap = mCacheUtils.getBitmapFromMemoryCache(imageUrl);
            if (bitmap == null) {
                BitmapWorkerTask task = new BitmapWorkerTask(imageUrl, mCacheUtils, new BitmapWorkerTask.DownLaodCallBack() {
                    @Override
                    public void onPostExecute(Bitmap bitmap) {

                        // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。
                        ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
                        if (imageView != null && bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                        taskCollection.remove(this);

                    }
                });
                taskCollection.add(task);
                task.execute(imageUrl);
            } else {
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
