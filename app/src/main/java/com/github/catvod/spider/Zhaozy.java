package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Zhaozy extends Spider {
    private PushAgent pushAgent;
    private static String b = "https://zhaoziyuan.me/";
    @Override
    public void init(Context context, String str) {
        super.init(context, str);
        pushAgent = new PushAgent();
        pushAgent.init(context, str);
    }
    @Override
    public String detailContent(List<String> list) {
        try {
            return pushAgent.detailContent(list);
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }
    @Override
    public String playerContent(String str, String str2, List<String> list) {
        return pushAgent.playerContent(str, str2, list);
    }

    private Pattern regexVid = Pattern.compile("(\\S+)");

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String url = "https://zhaoziyuan.me/so?filename=" + URLEncoder.encode(key);
            Document docs = Jsoup.parse(OkHttpUtil.string(url, null));
            JSONObject result = new JSONObject();
            JSONArray videos = new JSONArray();
            Elements list = docs.select("div.li_con div.news_text");
            String pic = "https://inews.gtimg.com/newsapp_bt/0/13263837859/1000";
            for (int i = 0; i < list.size(); i++) {
                Element doc = list.get(i);
                String title = doc.select("div.news_text a h3").text();
                if (title.contains(key)) {
                    String list1 = doc.select("div.news_text a").attr("href");
                    Matcher matcher = regexVid.matcher(list1);
                    if (matcher.find()) {
                        JSONObject v = new JSONObject();
                        String id = b + matcher.group(1);
                        String remark = doc.select("div.news_text a p").text();
                        remark = remark.replaceAll(".*收录时间：(.*)", "$1");
                        v.put("vod_id", id + "$$$" + pic + "$$$" + title);
                        v.put("vod_name", title);
                        v.put("vod_pic", pic);
                        v.put("vod_remarks", remark);
                        videos.put(v);
                    }
                }
            }
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }
}
