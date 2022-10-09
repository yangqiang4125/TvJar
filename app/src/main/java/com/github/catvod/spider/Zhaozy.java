package com.github.catvod.spider;

import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Zhaozy extends PushAgent {
    private static String b = "https://zhaoziyuan.me/";
    protected static HashMap<String, String> sHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.62 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-encoding", "gzip, deflate, br");
        headers.put("Accept-language", "zh-SG,zh;q=0.9,en-GB;q=0.8,en;q=0.7,zh-CN;q=0.6");
        headers.put("Referer", b);
        return headers;
    }
    private Pattern regexVid = Pattern.compile("(\\S+)");

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String url = "https://zhaoziyuan.me/so?filename=" + URLEncoder.encode(key);
            Document docs = Jsoup.parse(OkHttpUtil.string(url, sHeaders()));
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
                        v.put("vod_name", title.trim());
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
