package www.lptiyu.jason.musicdownloader.xutils3;

import java.util.List;

/**
 * Created by Jason on 2018/4/11.
 */

public class SearchResult {

    /**
     * id : 1ed2927a2b1399ce86e11c9c40f22d29
     * name : 大地
     * artist : ["BEYOND"]
     * album : 秘密警察
     * url_id : 1ed2927a2b1399ce86e11c9c40f22d29
     * pic_id : 1ed2927a2b1399ce86e11c9c40f22d29
     * lyric_id : 1ed2927a2b1399ce86e11c9c40f22d29
     * source : kugou
     */

    public String id;
    public String name;
    public String album;
    public String url_id;
    public String pic_id;
    public String lyric_id;
    public String source;
    public List<String> artist;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl_id() {
        return url_id;
    }

    public void setUrl_id(String url_id) {
        this.url_id = url_id;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public String getLyric_id() {
        return lyric_id;
    }

    public void setLyric_id(String lyric_id) {
        this.lyric_id = lyric_id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getArtist() {
        return artist;
    }

    public void setArtist(List<String> artist) {
        this.artist = artist;
    }
}
