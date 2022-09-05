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
    private PushAgent pushAgent;
    private static String j = "";
    private static final long n = 0;
    protected String ext = null;
    protected JSONObject rule = null;
    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        pushAgent = new PushAgent();
        if (extend != null) {
            if (!extend.startsWith("http")) {
                String[] arr = extend.split(";");
                this.ext = arr[1];
                pushAgent.jsonUrl = arr[1];
            }else {
                this.ext = extend;
            }
        }
        pushAgent.init(context, extend);
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            fetchRule(true);
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();
            String[] fenleis = pushAgent.getRuleVal(rule,"fenlei", "").split("#");
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
        rule = pushAgent.fetchRule(flag, 0);
        return rule;
    }
    @Override
    public String homeVideoContent() {
        try {
            JSONObject jo = pushAgent.fetchRule(true,1);
            JSONArray videos = new JSONArray();
            String[] fenleis = pushAgent.getRuleVal(jo, "fenlei", "").split("#");
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
            if (jo == null) jo = pushAgent.fetchRule(true,1);
            JSONArray videos = new JSONArray();
            JSONArray array = jo.getJSONArray(tid);
            JSONObject jsonObject = null, v = null;
            String url=null,name=null,pic=null;
            for (int i = 0; i < array.length(); i++) {
                jsonObject = array.getJSONObject(i);
                url = pushAgent.getRuleVal(jsonObject, "url");
                name = pushAgent.getRuleVal(jsonObject, "name");
                pic = pushAgent.getRuleVal(jsonObject, "pic");
                if(pic.equals("")) pic = Misc.getWebName(url, 1);
                v = new JSONObject();
                v.put("vod_id", url + "$$$" + pic + "$$$" + name);
                v.put("vod_name", name);
                v.put("vod_pic", pic);
                v.put("vod_remarks", Misc.getWebName(url,0));
                videos.put(v);
            }

            JSONObject result = new JSONObject();
            int limit = 20;int total = videos.length();
            int page = Integer.parseInt(pg);
            result.put("page", page);
            int pageCount = (int)Math.ceil((double)total/limit);
            result.put("pagecount", pageCount);
            result.put("limit", limit);
            result.put("total", total);
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
        return pushAgent.detailContent(list);
    }


    @Override
    public String playerContent(String str, String str2, List<String> list) {
        return pushAgent.playerContent(str, str2, list);
    }

    @Override
    public  String searchContent(String key, boolean quick) {
        if (key.equals("000")) {
            PushAgent.type=0;
        }if (key.equals("111")) {
            PushAgent.type=1;
        }
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