package www.lptiyu.jason.musicdownloader.xutils3;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import www.lptiyu.jason.musicdownloader.MyApplication;

/**
 * EMAIL : danxionglei@foxmail.com
 * DATE : 16/5/18
 *
 * @author ldx
 */
public class NetworkUtils {

    /**
     * 判断是否有网
     *
     * @return
     */
    public static boolean checkIsNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isAvailable() && info.isConnected());
    }

    /**
     * 判断是否有wifi
     *
     * @return
     */
    public static boolean isWlanAvailable() {
        ConnectivityManager cm = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
