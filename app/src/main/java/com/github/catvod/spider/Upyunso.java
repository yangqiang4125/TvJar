package com.github.catvod.spider;

import com.github.catvod.parser.Base64Utils;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;


public class Upyunso extends PushAgent {
    private static String a = "(https:\\/\\/www.aliyundrive.com\\/s\\/[^\\\"]+)";
    private static String b = "https://www.upyunso.com/";
    private String pic = "http://image.xinjun58.com/image/tv/ups.jpg";

    @Override
    public String detailContent(List<String> list) {
        return getDetail(list, pic);
    }

    @Override
    public String searchContent(String key, boolean z) {
        try {
            JSONArray jSONArray = new JSONArray();
            key = URLEncoder.encode(key);
            JSONArray arr = Base64Utils.getJSONByUrl("https://api.upyunso1.com/search?keyword=" + key+"&page=1");
            if (arr.length() > 0) {

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject v = arr.getJSONObject(i);
                    String id = v.getString("url");
                    String title = v.getString("title");
                    String remark = v.optString("remark","");
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