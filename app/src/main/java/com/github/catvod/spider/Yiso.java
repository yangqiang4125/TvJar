package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Yiso extends Spider {
    private static final Pattern aliyun = Pattern.compile("(https://www.aliyundrive.com/s/[^\"]+)");
    @Override
    public void init(Context context, String extend) {
        super.init(context);
        if (Misc.pushAgent == null) {
            Misc.pushAgent = new PushAgent();
            Misc.pushAgent.init(context, extend);
        }
    }

    @Override
    public String detailContent(List<String> list) {
        try {
            return Misc.pushAgent.getDetail(list);
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    public String playerContent(String str, String str2, List<String> list) {
        return Misc.pushAgent.player(str, str2, list);
    }

    protected static HashMap<String, String> sHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 12; V2049A Build/SP1A.210812.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/103.0.5060.129 Mobile Safari/537.36");
        headers.put("Referer", "https://yiso.fun/");
        return headers;
    }

    private Pattern regexVid = Pattern.compile("(\\S+)");

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            HashMap hashMap = new HashMap();
            HashMap<String, String> LT = sHeaders();
            String url = "https://yiso.fun/api/search?name=" + URLEncoder.encode(key) + "&from=ali";

            String content = OkHttpUtil.string(url, LT);
            JSONObject data = new JSONObject(content);
            JSONArray list = data.getJSONObject("data").getJSONArray("list");

            JSONObject result = new JSONObject();
            JSONArray videos = new JSONArray();

            for (int i = 0; i < list.length(); i++) {
                JSONObject jSONObject = list.getJSONObject(i);
                String sourceName = jSONObject.getJSONArray("fileInfos").getJSONObject(0).getString("fileName");
                String remark = jSONObject.getString("gmtCreate");
                String id = jSONObject.getString("url");

                JSONObject v = new JSONObject();
                String cover = "https://f.haocew.com/image/tv/yiso.jpg";
                v.put("vod_name", sourceName);
                v.put("vod_remarks", remark);
                v.put("vod_id", id + "$$$" + cover + "$$$" + sourceName);
                v.put("vod_pic", cover);
                videos.put(v);
            }
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

}