package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
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
        pushAgent.init(context, extend);
        if (extend != null) {
            if (!extend.startsWith("http")) {
                String[] arr = extend.split(";");
                this.ext = arr[1];
            }else {
                this.ext = extend;
            }
        }
    }

    protected void fetchRule(boolean flag) {
        try {
            if (flag || rule == null) {
                String json = OkHttpUtil.string(ext+"?t="+Time(), null);
                rule = new JSONObject(json);
            }
        } catch (Exception e) {
        }
    }
    private String getRuleVal(JSONObject o,String key, String defaultVal) {
        String v = o.optString(key);
        if (v.isEmpty() || v.equals("空"))
            return defaultVal;
        return v;
    }

    private String getRuleVal(JSONObject o,String key) {
        return getRuleVal(o,key, "");
    }

    protected static long Time() {
        return (System.currentTimeMillis() / 1000) + n;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            fetchRule(true);
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();
            String[] fenleis = getRuleVal(rule,"fenlei", "").split("#");
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

    @Override
    public String homeVideoContent() {
        try {
            fetchRule(true);
            JSONArray videos = new JSONArray();
            String[] fenleis = getRuleVal(rule, "fenlei", "").split("#");
            for (String fenlei : fenleis) {
                String[] info = fenlei.split("\\$");
                JSONObject data = category(info[1], "1", false, new HashMap<>());
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

    private JSONObject category(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            fetchRule(true);
            JSONArray videos = new JSONArray();

            JSONArray array = rule.getJSONArray(tid);
            JSONObject jsonObject = null, v = null;
            String url=null,name=null,pic=null;
            for (int i = 0; i < array.length(); i++) {
                jsonObject = array.getJSONObject(i);
                url = getRuleVal(jsonObject, "url");
                name = getRuleVal(jsonObject, "name");
                pic = getRuleVal(jsonObject, "pic");
                v = new JSONObject();
                v.put("vod_id", url + "$$$" + pic + "$$$" + name);
                v.put("vod_name", name);
                v.put("vod_pic", pic);
                v.put("vod_remarks", Misc.getWebName(url));
                videos.put(v);
            }

            JSONObject result = new JSONObject();
            int limit = 20;
            int page = Integer.parseInt(pg);
            result.put("page", page);
            int pageCount = videos.length() == limit ? page + 1 : page;
            result.put("pagecount", pageCount);
            result.put("limit", limit);
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
        JSONObject obj = category(tid, pg, filter, extend);
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

}