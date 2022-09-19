package com.github.catvod.spider;

import com.github.catvod.parser.Base64Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;


public class Upyunso extends PushAgent {
    private static String b = "https://www.upyunso.com/";

    public static String getRealUrl(String url){
        if (url.contains("upyunso.com/download")) {
            url = Base64Utils.sendGet(url);
        }
        return url;
    }

    @Override
    public String searchContent(String key, boolean z) {
        try {
            fetchRule(false, 0);
            JSONArray jSONArray = new JSONArray();
            key = URLEncoder.encode(key);
            JSONArray arr = Base64Utils.getJSONByUrl("https://api.upyunso.com/search?keyword=" + key+"&page=1");
            if (arr.length() > 0) {
                String pic = "http://image.xinjun58.com/image/tv/ups.jpg";
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject v = arr.getJSONObject(i);
                    String id = v.getString("url");
                    String title = v.getString("title");
                    String remark = v.getString("remark");
                    v.put("vod_id", id + "$$$" + pic + "$$$" + title);
                    v.put("vod_name", title);
                    v.put("vod_remarks", remark);
                    v.put("vod_pic", pic);
                    jSONArray.put(v);
                }
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("list", jSONArray);
                return jSONObject2.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}