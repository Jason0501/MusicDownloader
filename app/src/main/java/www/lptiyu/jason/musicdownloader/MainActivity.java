package www.lptiyu.jason.musicdownloader;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.reflect.TypeToken;

import org.xutils.http.RequestParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import www.lptiyu.jason.musicdownloader.xutils3.SearchResult;
import www.lptiyu.jason.musicdownloader.xutils3.SelectOneResult;
import www.lptiyu.jason.musicdownloader.xutils3.XUtilsHelper;
import www.lptiyu.jason.musicdownloader.xutils3.XUtilsRequestCallBack;
import www.lptiyu.jason.musicdownloader.xutils3.XUtilsUrls;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edit_input)
    EditText editInput;
    @BindView(R.id.btn_search)
    Button btnSearch;
    @BindView(R.id.recyclerView1)
    RecyclerView recyclerView1;
    @BindView(R.id.tvShow)
    TextView tvShow;
    private List<SearchResult> totallist1 = new ArrayList<>();
    private SearchAdapter adapter1;
    private boolean isStart;
    private boolean isComplete;
    private String musicName;
    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private ProgressDialog waitingDialog;
    private int total;
    private int current;
    private String dirPath;
    private String partPath = "Music_Downloader";
    private static Context context;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;
        verifyStoragePermissions();
    }

    public void verifyStoragePermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            initDir();
        }
    }

    private void initDir() {
        List<String> extSDCardPathList = getExtSDCardPathList();
        if (extSDCardPathList != null && extSDCardPathList.size() > 0) {
            Log.i("jason", extSDCardPathList.toString());
            dirPath = extSDCardPathList.get(0) + File.separator + partPath + File.separator;
        } else {
            ToastUtils.showShort("未找到SD卡目录或目录不可写");
        }
    }

    @OnClick({R.id.btn_search, R.id.btn_look})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_search:
                String input = editInput.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    return;
                }
                showWaitingDialog();
                loadSearch(input);
                break;
            case R.id.btn_look:
                ToastUtils.showShort("请选择" + partPath + "文件夹");
                startActivity(getFileBrowserIntent(new File(dirPath)));
                break;
        }
    }

    private void loadSearch(String input) {
        RequestParams params = new RequestParams(XUtilsUrls.SERVER_URL);
        params.addParameter("types", "search");
        params.addParameter("count", 20);
        params.addParameter("source", "kugou");
        params.addParameter("page", 1);
        params.addParameter("name", input);
        XUtilsHelper.post(params, new XUtilsRequestCallBack<List<SearchResult>>() {
            @Override
            protected void onSuccess(List<SearchResult> searchResult) {
                dismissWaitingDialog();
                if (searchResult != null) {
                    totallist1.clear();
                    totallist1.addAll(searchResult);
                    bindAdapter1();
                }
            }

            @Override
            protected void onFailed(String errorMsg) {
                dismissWaitingDialog();
                ToastUtils.showShort(errorMsg);
            }
        }, new TypeToken<List<SearchResult>>() {
        }.getType());
    }

    private void bindAdapter1() {
        recyclerView1.setVisibility(View.VISIBLE);
        if (adapter1 == null) {
            recyclerView1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            adapter1 = new SearchAdapter(totallist1);
            recyclerView1.setAdapter(adapter1);
            adapter1.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    SearchResult result = totallist1.get(position);
                    musicName = result.name;
                    showWaitingDialog();
                    loadDetail(result);
                }
            });
        } else {
            adapter1.notifyDataSetChanged();
        }
    }

    private void loadDetail(SearchResult result) {
        RequestParams params = new RequestParams(XUtilsUrls.SERVER_URL);
        params.addParameter("types", "url");
        params.addParameter("id", result.id);
        params.addParameter("source", "kugou");
        XUtilsHelper.post(params, new XUtilsRequestCallBack<SelectOneResult>() {
            @Override
            protected void onSuccess(SelectOneResult result) {
                dismissWaitingDialog();
                if (result != null) {
                    startDownload(result.url);
                }
            }

            @Override
            protected void onFailed(String errorMsg) {
                dismissWaitingDialog();
                ToastUtils.showShort(errorMsg);
            }
        }, new TypeToken<SelectOneResult>() {
        }.getType());
    }

    /**
     * 获取一个用于打开音频文件的intent
     *
     * @param file
     * @return
     */
    public static Intent getAudioFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    /**
     * 打开资源管理器
     *
     * @param file
     * @return
     */
    public static Intent getFileBrowserIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    private void initNotification(File file) {
        if (builder == null) {
            builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(musicName);
            builder.setAutoCancel(false);
            builder.setOngoing(true);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (file != null) {
            builder.setContentText("下载完成");
            PendingIntent pi = PendingIntent.getActivities(MainActivity.this, 0, new
                    Intent[]{getAudioFileIntent(file)}, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pi);
        } else {
            builder.setContentText("下载" + current * 100 / total + "%");
        }
        builder.setProgress(total, current, false);
        manager.notify(1, builder.build());
    }

    /**
     * 根据url截取文件后缀名
     *
     * @param fileUrl
     * @return
     */
    public static String getFileNameFromURL(String fileUrl) {
        if (fileUrl == null)
            return null;
        return fileUrl.substring(fileUrl.lastIndexOf('.'));
    }

    private void startDownload(String url) {
        String filename = musicName + getFileNameFromURL(url);
        ToastUtils.showShort("开始下载...");
        XUtilsHelper.downLoad(url, filename, dirPath, new XUtilsHelper.IDownloadCallback() {
            @Override
            public void success(File file) {
                ToastUtils.showLong("下载完成:" + file.getAbsolutePath());
                Log.i("jason", "文件保存目录：" + file.getAbsolutePath());
                isComplete = true;
                isStart = false;
                initNotification(file);
            }

            @Override
            public void progress(long total, long current, boolean isDownloading) {
                Log.i("jason", "current=" + current + ", total=" + total);
                MainActivity.this.total = (int) total;
                MainActivity.this.current = (int) current;
                initNotification(null);
                isStart = true;
                isComplete = false;
            }

            @Override
            public void finished() {
                isStart = false;
                isComplete = true;
            }

            @Override
            public void onError(String errMsg) {
                ToastUtils.showShort(errMsg);
                isComplete = false;
                isStart = false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isStart && !isComplete) {
            ToastUtils.showShort("下载任务将在后台执行...");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }

    public void showWaitingDialog() {
        showWaitingDialog(null);
    }

    public void showWaitingDialog(String message) {
        if (waitingDialog == null) {
            waitingDialog = new ProgressDialog(this);// 后期观察，如果有问题，this改为getParent()，防止出现is your activity
            // running问题
            waitingDialog.setCancelable(false);
            waitingDialog.setIndeterminate(true);
        }
        if (TextUtils.isEmpty(message)) {
            waitingDialog.setMessage("加载中，请稍等...");
        } else {
            waitingDialog.setMessage(message);
        }
        if (!isFinishing() && !waitingDialog.isShowing()) {
            waitingDialog.show();
        }
    }

    public void dismissWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
        }
    }

    /**
     * 获取外置SD卡路径以及TF卡的路径
     * <p>
     * 返回的数据：paths.get(0)肯定是外置SD卡的位置，因为它是primary external storage.
     *
     * @return 所有可用于存储的不同的卡的位置，用一个List来保存
     */
    public List<String> getExtSDCardPathList() {
        List<String> paths = new ArrayList();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
        boolean isEquals = extFileStatus.equals(Environment.MEDIA_MOUNTED);
        boolean isExists = extFile.exists();
        boolean isDirectory = extFile.isDirectory();
        boolean canWrite = extFile.canWrite();
        String absolutePath = extFile.getAbsolutePath();
//        tvShow.setText("isEquals=" + isEquals + ", isExists=" + isExists + ", isDirectory=" + isDirectory + ", " +
//                "canWrite=" + canWrite + ", absolutePath=" + absolutePath);
        if (isEquals && isExists && isDirectory && canWrite) {
            //外置SD卡的路径
            paths.add(absolutePath);
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard isExists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(absolutePath);
                if (equalsToPrimarySD) {
                    continue;
                }
                //扩展存储卡即TF卡或者SD卡路径
                paths.add(mountPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }
}
