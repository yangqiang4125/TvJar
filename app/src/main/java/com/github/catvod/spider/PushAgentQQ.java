package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class PushAgentQQ extends Spider {
    private static String douban_api_host = "https://frodo.douban.com/api/v2";
    private static String apikey = "0ac44ae016490db2204ce0a042db2916";
    public static JSONObject getUrls(){
        String _urls = "{\n" +
                "                \"search\": \"/search/weixin\",\n" +
                "                \"search_agg\": \"/search\",\n" +
                "\n" +
                "                \"movie_recommend\": \"/movie/recommend\",\n" +
                "                \"movie_tag\": \"/movie/tag\",\n" +
                "                \"tv_tag\": \"/tv/tag\",\n" +
                "\n" +
                "                \"movie_search\": \"/search/movie\",\n" +
                "                \"tv_search\": \"/search/movie\",\n" +
                "                \"book_search\": \"/search/book\",\n" +
                "                \"group_search\": \"/search/group\",\n" +
                "\n" +
                "                \"movie_showing\": \"/subject_collection/movie_showing/items\",\n" +
                "                \"movie_hot_gaia\": \"/subject_collection/movie_hot_gaia/items\",\n" +
                "                \"movie_soon\": \"/subject_collection/movie_soon/items\",\n" +
                "                \"movie_top250\": \"/subject_collection/movie_top250/items\",\n" +
                "                \"movie_scifi\": \"/subject_collection/movie_scifi/items\",\n" +
                "                \"movie_comedy\": \"/subject_collection/movie_comedy/items\",\n" +
                "                \"movie_action\": \"/subject_collection/movie_action/items\",\n" +
                "                \"movie_love\": \"/subject_collection/movie_love/items\",\n" +
                "                \"tv_hot\": \"/subject_collection/tv_hot/items\",\n" +
                "                \"tv_domestic\": \"/subject_collection/tv_domestic/items\",\n" +
                "                \"tv_american\": \"/subject_collection/tv_american/items\",\n" +
                "                \"tv_japanese\": \"/subject_collection/tv_japanese/items\",\n" +
                "                \"tv_korean\": \"/subject_collection/tv_korean/items\",\n" +
                "                \"tv_animation\": \"/subject_collection/tv_animation/items\",\n" +
                "                \"tv_variety_show\": \"/subject_collection/tv_variety_show/items\",\n" +
                "                \"tv_chinese_best_weekly\": \"/subject_collection/tv_chinese_best_weekly/items\",\n" +
                "                \"tv_global_best_weekly\": \"/subject_collection/tv_global_best_weekly/items\",\n" +
                "\n" +
                "                \"show_hot\": \"/subject_collection/show_hot/items\",\n" +
                "                \"show_domestic\": \"/subject_collection/show_domestic/items\",\n" +
                "                \"show_foreign\": \"/subject_collection/show_foreign/items\",\n" +
                "\n" +
                "                \"book_bestseller\": \"/subject_collection/book_bestseller/items\",\n" +
                "                \"book_top250\": \"/subject_collection/book_top250/items\",\n" +
                "                \"book_fiction_hot_weekly\": \"/subject_collection/book_fiction_hot_weekly/items\",\n" +
                "                \"book_nonfiction_hot_weekly\": \"/subject_collection/book_nonfiction_hot_weekly/items\",\n" +
                "\n" +
                "                \"music_single\": \"/subject_collection/music_single/items\",\n" +
                "\n" +
                "                \"movie_rank_list\": \"/movie/rank_list\",\n" +
                "                \"movie_year_ranks\": \"/movie/year_ranks\",\n" +
                "                \"book_rank_list\": \"/book/rank_list\",\n" +
                "                \"tv_rank_list\": \"/tv/rank_list\",\n" +
                "\n" +
                "                \"movie_detail\": \"/movie/\",\n" +
                "                \"movie_rating\": \"/movie/%s/rating\",\n" +
                "                \"movie_photos\": \"/movie/%s/photos\",\n" +
                "                \"movie_trailers\": \"/movie/%s/trailers\",\n" +
                "                \"movie_interests\": \"/movie/%s/interests\",\n" +
                "                \"movie_reviews\": \"/movie/%s/reviews\",\n" +
                "                \"movie_recommendations\": \"/movie/%s/recommendations\",\n" +
                "                \"movie_celebrities\": \"/movie/%s/celebrities\",\n" +
                "\n" +
                "                \"tv_detail\": \"/tv/\",\n" +
                "                \"tv_rating\": \"/tv/%s/rating\",\n" +
                "                \"tv_photos\": \"/tv/%s/photos\",\n" +
                "                \"tv_trailers\": \"/tv/%s/trailers\",\n" +
                "                \"tv_interests\": \"/tv/%s/interests\",\n" +
                "                \"tv_reviews\": \"/tv/%s/reviews\",\n" +
                "                \"tv_recommendations\": \"/tv/%s/recommendations\",\n" +
                "                \"tv_celebrities\": \"/tv/%s/celebrities\",\n" +
                "\n" +
                "                \"book_detail\": \"/book/\",\n" +
                "                \"book_rating\": \"/book/%s/rating\",\n" +
                "                \"book_interests\": \"/book/%s/interests\",\n" +
                "                \"book_reviews\": \"/book/%s/reviews\",\n" +
                "                \"book_recommendations\": \"/book/%s/recommendations\",\n" +
                "\n" +
                "                \"music_detail\": \"/music/\",\n" +
                "                \"music_rating\": \"/music/%s/rating\",\n" +
                "                \"music_interests\": \"/music/%s/interests\",\n" +
                "                \"music_reviews\": \"/music/%s/reviews\",\n" +
                "                \"music_recommendations\": \"/music/%s/recommendations\",\n" +
                "        }";

        try {
            JSONObject jsonObject = new JSONObject(_urls);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getDouban(String key,String sort) throws JSONException {
        if(sort==null)sort="U";
        JSONObject ju = getUrls();
        String url = douban_api_host + ju.getString(key)+"?sort="+sort+"&start=0&count=120&apikey=" + apikey + "&channel=Douban";
        String json = OkHttpUtil.string(url, getHeaderDB());
        System.out.println(json);
        JSONArray jSONArray = new JSONArray();
        JSONObject jo = new JSONObject(json),o1 = null,op1=null,vo=null;

        JSONArray ay = jo.getJSONArray("subject_collection_items");
        String remark = "", title = "",pic="";

        for (int i = 0; i < ay.length(); i++) {
            JSONObject v = ay.getJSONObject(i);
            vo = new JSONObject();
            title = v.getString("title");
            o1 = v.getJSONObject("rating");
            op1 = v.getJSONObject("pic");
            pic = op1.optString("normal", "");
            remark = o1.optString("value", "暂无评分");

            vo.put("vod_id", "https://www.lgyy.cc/vodplay/45421-1-1.html" + "$$$" + pic + "$$$" + title);
            vo.put("vod_name", title);
            vo.put("vod_remarks", remark);
            vo.put("vod_pic", pic);
            jSONArray.put(vo);
        }
        return jSONArray;
    }



    protected JSONObject rule = null;
    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        if (extend != null && !extend.equals("")) {
            if (extend.startsWith("http")) {
                Misc.jsonUrl = extend;
            }
        }
        PushAgent.fetchRule(false, 0);
    }


    protected static HashMap<String, String> getHeaderDB() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Host","frodo.douban.com");
        headers.put("Connection", "Keep-Alive");
        headers.put("Referer", "https://servicewechat.com/wx2f9b06c1de1ccfca/84/page-frame.html");
        headers.put("content-type", "application/json");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36 MicroMessenger/7.0.9.501 NetType/WIFI MiniProgramEnv/Windows WindowsWechat");
        return headers;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            fetchRule(true);
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();
            String[] fenleis = PushAgent.getRuleVal(rule,"fenlei", "").split("#");
            for (String fenlei : fenleis) {
                String[] info = fenlei.split("\\$");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type_name", info[0]);
                jsonObject.put("type_id", info[1]);
                classes.put(jsonObject);
            }
            result.put("class", classes);
            return result.toString();
        } catch (
                Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }
    protected JSONObject fetchRule(boolean flag) {
        rule = PushAgent.fetchRule(flag, 0);
        return rule;
    }
    @Override
    public String homeVideoContent() {
        try {
            JSONObject jo = PushAgent.fetchRule(true,1);
            JSONArray videos = new JSONArray();
            String[] fenleis = PushAgent.getRuleVal(jo, "fenlei", "").split("#");
            for (String fenlei : fenleis) {
                String[] info = fenlei.split("\\$");
                JSONObject data = category(info[1], "1", false, new HashMap<>(),jo);
                if (data != null) {
                    JSONArray vids = data.optJSONArray("list");
                    if (vids != null) {
                        for (int i = 0; i < vids.length() && i < 5; i++) {
                            videos.put(vids.getJSONObject(i));
                        }
                    }
                }
                if (videos.length() >= 30)
                    break;
            }
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    private JSONObject category(String tid, String pg, boolean filter, HashMap<String, String> extend,JSONObject jo) {
        try {
            JSONArray videos = new JSONArray();
            String url=null,name=null,pic=null;
            JSONObject jsonObject = null, v = null;
            if (tid.equals("bili")) {
                String json = OkHttpUtil.string("https://api.bilibili.com/x/web-interface/ranking/v2?rid=0&type=all", null);
                JSONObject j = new JSONObject(json);
                JSONObject o = j.getJSONObject("data");
                JSONArray array = o.getJSONArray("list");
                for (int i = 0; i < array.length(); i++) {
                    jsonObject = array.getJSONObject(i);
                    url = jsonObject.optString("short_link", "");
                    name = jsonObject.optString("title", "");
                    pic = jsonObject.optString("pic", "");
                    v = new JSONObject();
                    v.put("vod_id", url + "$$$" + pic + "$$$" + name);
                    v.put("vod_name", name);
                    v.put("vod_pic", pic);
                    v.put("vod_remarks", "");
                    videos.put(v);
                }
            }else if (tid.startsWith("douban-")) {
                String [] arr = tid.split("\\-");
                String key = arr[1];
                String sort = null;
                if(arr.length>2)sort = arr[2];
                videos = getDouban(key, sort);
            }else{
                if (jo == null) jo = PushAgent.fetchRule(true,1);
                JSONArray array = jo.getJSONArray(tid);
                for (int i = 0; i < array.length(); i++) {
                    jsonObject = array.getJSONObject(i);
                    url = PushAgent.getRuleVal(jsonObject, "url");
                    name = PushAgent.getRuleVal(jsonObject, "name");
                    pic = PushAgent.getRuleVal(jsonObject, "pic");
                    if(pic.equals("")) pic = Misc.getWebName(url, 1);
                    v = new JSONObject();
                    v.put("vod_id", url + "$$$" + pic + "$$$" + name);
                    v.put("vod_name", name);
                    v.put("vod_pic", pic);
                    v.put("vod_remarks", Misc.getWebName(url,0));
                    videos.put(v);
                }
            }
            JSONObject result = new JSONObject();
            result.put("page", pg);
            result.put("pagecount", 1);
            result.put("limit", Integer.MAX_VALUE);
            result.put("total", Integer.MAX_VALUE);
            result.put("list", videos);
            return result;
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return null;
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        JSONObject obj = category(tid, pg, filter, extend,null);
        return obj != null ? obj.toString() : "";
    }

    @Override
    public String detailContent(List<String> list) {
        return PushAgent.getDetail(list);
    }


    @Override
    public String playerContent(String str, String str2, List<String> list) {
        return PushAgent.player(str, str2, list);
    }

    @Override
    public  String searchContent(String key, boolean quick) {
        JSONObject result = new JSONObject();
        JSONArray videos = new JSONArray();
        try {
            fetchRule(true);
            String url = "",webUrl,detailRex,siteUrl,siteName;
            JSONArray siteArray = rule.getJSONArray("sites");
            for (int j = 0; j < siteArray.length(); j++) {
                JSONObject site = siteArray.getJSONObject(j);
                siteUrl = site.optString("site");
                siteName = site.optString("name");
                detailRex = siteUrl+site.optString("detailRex","/vod/%s.html");
                webUrl = siteUrl + "/index.php/ajax/suggest?mid=1&wd="+key;

                JSONObject data = new JSONObject(OkHttpUtil.string(webUrl, Misc.Headers(0)));
                JSONArray vodArray = data.getJSONArray("list");
                for (int i = 0; i < vodArray.length(); i++) {
                    JSONObject vod = vodArray.getJSONObject(i);
                    String name = vod.optString("name").trim();
                    String id = vod.optString("id").trim();
                    String pic = vod.optString("pic").trim();
                    pic = Misc.fixUrl(webUrl, pic);

                    url = detailRex.replace("%s",id);
                    JSONObject v = new JSONObject();
                    v.put("vod_id", url + "$$$" + pic + "$$$" + name);
                    v.put("vod_name", "["+siteName+"]"+name);
                    v.put("vod_pic", pic);
                    v.put("vod_remarks", "");
                    videos.put(v);
                }
            }
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            if (videos.length() > 0) {
                try {
                    result.put("list", videos);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
                return result.toString();
            }
        }
        return "";
    }
}