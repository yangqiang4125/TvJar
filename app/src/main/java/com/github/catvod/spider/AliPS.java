package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AliPS extends Spider {
    private PushAgent pushAgent;
    private static String b = "https://www.alipansou.com";
    private static Pattern a = Pattern.compile("(https:\\/\\/www.aliyundrive.com\\/s\\/[^\\\"]+)");

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        pushAgent = new PushAgent();
        pushAgent.init(context, extend);
    }

    @Override
    public String detailContent(List<String> list) {
        try {
            return pushAgent.detailContent(list);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String playerContent(String str, String str2, List<String> list) {
        return pushAgent.playerContent(str, str2, list);
    }

    protected static HashMap<String, String> sHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.62 Safari/537.36");
        return headers;
    }

    @Override
    public String searchContent(String key, boolean z) {
        try {
            HashMap hashMap = new HashMap();
            hashMap.put("7", "文件夹");
            hashMap.put("1", "视频");
            JSONArray jSONArray = new JSONArray();
            Iterator entries = hashMap.entrySet().iterator();
            String dx = null, pic = "https://inews.gtimg.com/newsapp_bt/0/13263837859/1000", title = "";
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                String sb2 = b + "/search?k=" + URLEncoder.encode(key) + "&t=" + (String) entry.getKey();
                Document doc = Jsoup.parse(OkHttpUtil.string(sb2, sHeaders()));
                Elements Data = doc.select("van-row a");
                for (int i = 0; i < Data.size(); i++) {
                    Element next = Data.get(i);
                    String filename = next.select("template div").text();
                    Pattern pattern = Pattern.compile("(时间: \\S+)");
                    Matcher matcher = pattern.matcher(filename);
                    if (!matcher.find())
                        continue;
                    String remark = matcher.group(1);
                    remark = remark.replace("时间: ", "");
                    if (filename.contains(key)) {
                        JSONObject v = new JSONObject();
                        String id = b + next.attr("href");
                        //[视频]第十九集 琉璃世界白雪红梅.mp4 时间: 2021-12-07 格式: mp4 大小: 6.2G
                        title = filename.replaceAll(" 时间: .*", "");
                        if (filename.contains("大小")) {
                            dx = filename.replaceAll(".*大小: (.*)", "$1");
                            remark = remark + " " + dx;
                        }
                        v.put("vod_id", id + "$$$" + pic + "$$$" + title);
                        v.put("vod_name", title);
                        v.put("remark", remark);
                        v.put("vod_pic", pic);
                        jSONArray.put(v);
                    }
                }
            }
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("list", jSONArray);
            return jSONObject2.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}