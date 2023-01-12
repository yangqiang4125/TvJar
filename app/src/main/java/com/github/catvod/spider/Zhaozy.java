package com.github.catvod.spider;

import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttp;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Zhaozy extends PushAgent {
    private static String b = "https://zhaoziyuan.la/";
    private Pattern regexVid = Pattern.compile("(\\S+)");

    private Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Misc.UaWinChrome);
        headers.put("Referer", b);
        headers.put("Cookie", getCookie());
        return headers;
    }

    private String getCookie() {
        if (Misc.zzy == null) {
            Map<String, String> params = new HashMap<>();
            params.put("username", "412594121@qq.com");
            params.put("password", "qq@4125");
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", Misc.UaWinChrome);
            headers.put("Referer", b + "login.html");
            headers.put("Origin", b);
            Map<String, List<String>> resp = new HashMap<>();
            OkHttp.post(b + "logiu.html", params, headers, resp);
            StringBuilder sb = new StringBuilder();
            for (String item : resp.get("set-cookie")) sb.append(item.split(";")[0]).append(";");
            Misc.zzy = sb.toString();
        }
        return Misc.zzy;
    }

    @Override
    public String detailContent(List<String> list) {
        try {
            String id =list.get(0);
            if (!id.contains("aliyundrive.com")) {
                String[] arr = id.split("\\$\\$\\$");
                Matcher matcher = Misc.regexAli.matcher(OkHttp.string(arr[0], getHeader()));
                if (!matcher.find()) return "";
                String uid = matcher.group(1) + "$$$" + arr[1] + "$$$" + arr[2];
                list.set(0, uid);
                return getDetail(list);
            }
            return getDetail(list);
        } catch (Exception e) {
            Misc.zzy = null;
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String url = b + "so?filename=" + URLEncoder.encode(key);
            Document docs = Jsoup.parse(OkHttpUtil.string(url, getHeader()));
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
                        //类别：文件夹 | 收录时间：2022-10-08 22:51
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
            Misc.zzy = null;
            SpiderDebug.log(e);
        }
        return "";
    }
}
