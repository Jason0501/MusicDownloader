package www.lptiyu.jason.musicdownloader.xutils3;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.lang.reflect.Type;

import www.lptiyu.jason.musicdownloader.Htmlspirit;

/**
 * Created by Jason on 2016/8/2.
 */
public class XUtilsHelper {
    public static Gson gson = new Gson();

    public static <T> Callback.Cancelable post(RequestParams params, final XUtilsRequestCallBack<T> callBack,
                                               final Type type) {
        if (!NetworkUtils.checkIsNetworkConnected()) {
            if (callBack != null) {
                callBack.onFailed(NO_NET_ERROR_MESSAGE);
            }
            return null;
        }
        return x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String s = Htmlspirit.delHTMLTag(result);
                T t = gson.fromJson(s, type);
                if (callBack != null) {
                    callBack.onSuccess(t);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (callBack != null) {
                    callBack.onFailed(ex.getMessage());
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                if (callBack != null) {
                    callBack.onFailed(cex.getMessage());
                }
            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 下载回调接口
     */
    public interface IDownloadCallback {
        void success(File file);

        void progress(long total, long current, boolean isDownloading);

        void finished();

        void onError(String errMsg);
    }

    private final static String NO_NET_ERROR_MESSAGE = "网络已断开";
    private final static String FILE_PATH_NOT_EXIST = "文件路径不存在!";
    private final static String FILE_URL_NOT_EXIST = "文件链接不存在!";

    public static Callback.Cancelable downLoad(String fileUrl, String fileName, String fileSavePath,
                                               final IDownloadCallback callback) {
        if (callback == null) {
            return null;
        }
        if (!NetworkUtils.checkIsNetworkConnected()) {
            callback.onError(NO_NET_ERROR_MESSAGE);
            return null;
        }
        if (TextUtils.isEmpty(fileUrl)) {
            callback.onError(FILE_URL_NOT_EXIST);
            return null;
        }
        //xutils不支持非http协议
        RequestParams params;
        if (fileUrl.startsWith("http://"))
            params = new RequestParams(fileUrl);
        else
            params = new RequestParams("http://" + fileUrl);
        //设置断点续传
        params.setAutoResume(true);
        params.setSaveFilePath(fileSavePath + "/" + fileName);
        params.setAutoRename(false);
        params.setConnectTimeout(30000);//30s超时
        //进度条
        return x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                //进度条
                if (callback != null) {
                    callback.progress(total, current, isDownloading);
                }
            }

            @Override
            public void onSuccess(File file) {
                if (callback != null) {
                    callback.success(file);
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (callback != null) {
                    callback.onError(ex.getMessage());
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                if (callback != null) {
                    callback.finished();
                }
            }
        });
    }
}
