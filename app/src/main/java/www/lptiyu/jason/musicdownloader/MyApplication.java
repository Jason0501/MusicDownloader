package www.lptiyu.jason.musicdownloader;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.liulishuo.filedownloader.FileDownloader;

import org.xutils.x;

/**
 * Created by Jason on 2018/4/11.
 */

public class MyApplication extends Application {
    public static MyApplication getInstance() {
        return instance;
    }

    public static MyApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Utils.init(this);
        x.Ext.init(this);
        x.Ext.setDebug(false);
        FileDownloader.setupOnApplicationOnCreate(this);
    }
}
