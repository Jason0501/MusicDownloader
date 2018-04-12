package www.lptiyu.jason.musicdownloader;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import www.lptiyu.jason.musicdownloader.xutils3.SearchResult;

/**
 * Created by Jason on 2018/2/28.
 */

public class SearchAdapter extends BaseQuickAdapter<SearchResult, BaseViewHolder> {
    public SearchAdapter(@Nullable List<SearchResult> data) {
        super(R.layout.item, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, SearchResult item) {
        List<String> artist = item.artist;
        StringBuilder sb = new StringBuilder();
        if (artist != null) {
            int size = item.artist.size();
            for (int i = 0; i < size; i++) {
                sb.append(item.artist.get(i));
                if (i != size - 1) {
                    sb.append("-");
                }
            }
        }
        holder.setText(R.id.text1, item.name + "    " + sb.toString());
    }
}
