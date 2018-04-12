package www.lptiyu.jason.musicdownloader.xutils3;

/**
 * Xutils返回回调
 */
public abstract class XUtilsRequestCallBack<T> {
    protected abstract void onSuccess(T t);

    protected abstract void onFailed(String errorMsg);
}
