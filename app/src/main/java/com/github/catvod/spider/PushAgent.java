package com.github.catvod.spider;

import android.content.Context;
import android.net.UrlQuerySanitizer;
import android.text.TextUtils;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OKCallBack;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**6161-自动生成阿里云链接 61-默认阿里云播放 2-默认4K播放
 * TODO(阿里云 各种详情页面解析)
 * @author yangqiang
 * @date 2022/9/19 9:50
 */

public class PushAgent extends Spider {
    private static long timeToken = 0;
    private static int zi=0;
    private static String accessToken = "";
    private static String k4 = "1";//是否开启简写集数
    private static String szRegx = "";//集数数字正则匹配
    private static Map<String, String> shareToken = new HashMap<>();
    private static Map<String, Long> shareExpires = new HashMap<>();
    private static final Map<String, Map<String, String>> videosMap = new HashMap<>();
    private static final ReentrantLock rLock = new ReentrantLock();
    private static final String SiteUrl = "https://api.aliyundrive.com";
    public static Pattern regexAli = Pattern.compile("(https://www.aliyundrive.com/s/[^\"]+)");
    public static Pattern regexAliFolder = Pattern.compile("www.aliyundrive.com/s/([^/]+)(/folder/([^/]+))?");
    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        if (extend != null && !extend.equals("")) {
            if (extend.startsWith("http")) {
                Misc.jsonUrl = extend;
            }else Misc.refreshToken = extend;
        }
        fetchRule(false,0);
    }

    public static void getToken(String token){
        if(Misc.refreshToken.equals("")){
            if (token.startsWith("http")) {
                Misc.refreshToken = OkHttpUtil.string(token, null);
            }else Misc.refreshToken = token;
        }
    }

    public static JSONObject fetchRule(boolean flag,int t) {
        try {
            if (flag || Misc.siteRule == null) {
                String json = OkHttpUtil.string(Misc.jsonUrl+"?t="+Time(), null);
                JSONObject jo = new JSONObject(json);
                if(t==0) {
                    String[] fenleis = getRuleVal(jo,"fenlei", "").split("#");
                    for (String fenlei : fenleis) {
                        String[] info = fenlei.split("\\$");
                        jo.remove(info[1]);
                    }
                    Misc.siteRule = jo;
                    String tk = Misc.siteRule.optString("token","");
                    if(!tk.equals("")){
                        getToken(tk);
                    }
                    Misc.type = Misc.siteRule.optInt("ua", 1);
                    Misc.btype = Misc.siteRule.optString("btype", "N");
                    Misc.apikey = Misc.siteRule.optString("apikey", "0ac44ae016490db2204ce0a042db2916");
                    k4 = Misc.siteRule.optString("4k", "1");
                    szRegx =  Misc.siteRule.optString("szRegx", ".*(Ep|EP|E|第)(\\d+)[\\.|集]?.*");
                }
                return jo;
            }
        } catch (JSONException e) {
        }
        return Misc.siteRule;
    }

    public static String getRuleVal(JSONObject o,String key, String defaultVal) {
        String v = o.optString(key);
        if (v.isEmpty() || v.equals("空"))
            return defaultVal;
        return v;
    }

    public static String getRuleVal(JSONObject o,String key) {
        return getRuleVal(o,key, "");
    }

    public static long Time() {
        return (System.currentTimeMillis() / 1000);
    }


    private static HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36");
        headers.put("Referer", "https://www.aliyundrive.com/");
        return headers;
    }

    private static HashMap<String, String> sHeaders(boolean flag) {
        String chrome = flag ? Misc.UaWinChrome : Misc.MoAgent;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", chrome);
        return headers;
    }

    private static String postJson(String url, String jsonStr, Map<String, String> headerMap) {
        OKCallBack.OKCallBackString callback = new OKCallBack.OKCallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {

            }
        };
        OkHttpUtil.postJson(OkHttpUtil.defaultClient(), url, jsonStr, headerMap, callback);
        return callback.getResult();
    }

    public static void getRefreshTk() {
        long timeSys = Time();
        if (accessToken.isEmpty() || timeToken - timeSys <= 600) {
            try {
                JSONObject json = new JSONObject();
                json.put("refresh_token", Misc.refreshToken);
                JSONObject response = new JSONObject(postJson("https://api.aliyundrive.com/token/refresh", json.toString(), getHeaders()));
                accessToken = response.getString("token_type") + " " + response.getString("access_token");
                timeToken = response.getLong("expires_in") + timeSys;
            } catch (JSONException e) {
                SpiderDebug.log(e);
            }
        }
    }

    private static synchronized String getShareTk(String shareId, String sharePwd) {
        synchronized (PushAgent.class) {
            try {
                long timeSys = Time();
                String token = shareToken.get(shareId);
                Long expires = shareExpires.get(shareId);
                if (!TextUtils.isEmpty(token) && expires - timeSys > 600) {
                    return token;
                }
                JSONObject json = new JSONObject();
                json.put("share_id", shareId);
                json.put("share_pwd", sharePwd);
                JSONObject response = new JSONObject(postJson("https://api.aliyundrive.com/v2/share_link/get_share_token", json.toString(), getHeaders()));
                String string = response.getString("share_token");
                shareExpires.put(shareId, timeSys + response.getLong("expires_in"));
                shareToken.put(shareId, string);
                return string;
            } catch (JSONException e) {
                SpiderDebug.log(e);
                Init.show("來晚啦，该分享已失效。");
                return "";
            }
        }
    }

    public static Object[] loadsub(String url) {
        try {
            return new Object[]{200, "application/octet-stream", new ByteArrayInputStream(OkHttpUtil.string(url, getHeaders()).getBytes())};
        } catch (Exception e2) {
            e2.printStackTrace();
            SpiderDebug.log(e2);
            return null;
        }
    }

    public static Object[] File(Map<String, String> params) {
        try {
            String shareId = params.get("share_id");
            return new Object[]{200, "application/octet-stream", new ByteArrayInputStream(getVideoUrl(shareId, getShareTk(shareId, ""), params.get("file_id")).getBytes())};
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return null;
        }
    }

    public static Object[] ProxyMedia(Map<String, String> params) {
        try {
            String shareId = params.get("share_id");
            String fileId = params.get("file_id");
            String mediaId = params.get("media_id");
            String shareToken = getShareTk(shareId, "");
            ReentrantLock reentrantLock = rLock;
            reentrantLock.lock();
            String url = videosMap.get(fileId).get(mediaId);
            if (new Long(new UrlQuerySanitizer(url).getValue("x-oss-expires")) - Time() <= 60) {
                getVideoUrl(shareId, shareToken, fileId);
                url = videosMap.get(fileId).get(mediaId);
            }
            reentrantLock.unlock();

            OKCallBack.OKCallBackDefault callback = new OKCallBack.OKCallBackDefault() {
                @Override
                public void onFailure(Call call, Exception e) {

                }

                @Override
                public void onResponse(Response response) {

                }
            };
            OkHttpUtil.get(OkHttpUtil.defaultClient(), url, null, getHeaders(),callback);
            return new Object[]{200, "video/MP2T", callback.getResult().body().byteStream()};
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return null;
        }
    }


    public static Object[] vod(Map<String, String> map) {
        String type = map.get("type");
        if (type.equals("m3u8")) {
            return File(map);
        }
        if (type.equals("media")) {
            return ProxyMedia(map);
        }
        return null;
    }
    private static String getVideoUrl(String shareId, String shareToken, String fileId) {
        try {
            if(zi>1) return "";
            getRefreshTk();
            JSONObject json = new JSONObject();
            json.put("share_id", shareId);
            json.put("category", "live_transcoding");
            json.put("file_id", fileId);
            json.put("template_id", "");
            HashMap<String, String> Headers = getHeaders();
            Headers.put("x-share-token", shareToken);
            Headers.put("authorization", accessToken);
            JSONObject jSONObject3 = new JSONObject(postJson("https://api.aliyundrive.com/v2/file/get_share_link_video_preview_play_info", json.toString(), Headers));
            JSONArray playList = jSONObject3.getJSONObject("video_preview_play_info").getJSONArray("live_transcoding_task_list");
            String videoUrl = "";
            String[] orders = new String[]{"FHD", "HD", "SD"};
            for (String or : orders) {
                for (int i = 0; i < playList.length(); i++) {
                    JSONObject obj = playList.getJSONObject(i);
                    if (obj.optString("template_id").equals(or)) {
                        videoUrl = obj.getString("url");
                        break;
                    }
                }
                if (!videoUrl.isEmpty())
                    break;
            }
            if (videoUrl.isEmpty() && playList.length() > 0) {
                videoUrl = playList.getJSONObject(0).getString("url");
            }
            Map<String, List<String>> respHeaderMap = new HashMap<>();
            OkHttpUtil.stringNoRedirect(videoUrl, getHeaders(), respHeaderMap);
            String url = OkHttpUtil.getRedirectLocation(respHeaderMap);
            String medias = OkHttpUtil.string(url, getHeaders());
            String site = url.substring(0, url.lastIndexOf("/")) + "/";
            ArrayList<String> lists = new ArrayList<>();
            Map<String, String> video = new HashMap<>();
            String[] split = medias.split("\n");
            int j = 0;
            for (int i = 0; i < split.length; i++) {
                String vod = split[i];
                if (vod.contains("x-oss-expires")) {
                    j++;
                    video.put("" + j, site + vod);
                    vod = Proxy.localProxyUrl() + "?do=ali&type=media&share_id=" + shareId + "&file_id=" + fileId + "&media_id=" + j;
                }
                lists.add(vod);
            }
            zi=0;
            videosMap.put(fileId, video);
            return TextUtils.join("\n", lists);
        } catch (Exception e2) {
            timeToken = 0;zi++;
            return getVideoUrl(shareId, shareToken, fileId);
        }
    }

    private static String getOriginalVideoUrl(String shareId, String shareToken, String fileId, String category) {
        try {
            getRefreshTk();
            HashMap<String, String> json = getHeaders();
            json.put("x-share-token", shareToken);
            json.put("authorization", accessToken);
            if (category.equals("video")) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("share_id", shareId);
                jSONObject.put("category", "live_transcoding");
                jSONObject.put("file_id", fileId);
                jSONObject.put("template_id", "");
                JSONObject jSONObject2 = new JSONObject(postJson("https://api.aliyundrive.com/v2/file/get_share_link_video_preview_play_info", jSONObject.toString(), json));
                shareId = jSONObject2.getString("share_id");
                fileId = jSONObject2.getString("file_id");
            }
            JSONObject jSONObject3 = new JSONObject();
            if (category.equals("video")) {
                jSONObject3.put("expire_sec", 600);
                jSONObject3.put("file_id", fileId);
                jSONObject3.put("share_id", shareId);
            }else if (category.equals("audio")) {
                jSONObject3.put("share_id", shareId);
                jSONObject3.put("get_audio_play_info", true);
                jSONObject3.put("file_id", fileId);
            }
            return new JSONObject(postJson("https://api.aliyundrive.com/v2/file/get_share_link_download_url", jSONObject3.toString(), json)).getString("download_url");
        } catch (Exception e) {
            timeToken = 0;
            SpiderDebug.log(e);
            return "";
        }
    }


    public static void listFiles(Map<String, String> map, String shareId, String shareToken, String fileId,String _url,String name) {
        try {
            String url = "https://api.aliyundrive.com/adrive/v3/file/list";
            HashMap<String, String> headers = getHeaders();
            headers.put("x-share-token", shareToken);
            JSONObject json = new JSONObject();
            json.put("image_thumbnail_process", "image/resize,w_160/format,jpeg");
            json.put("image_url_process", "image/resize,w_1920/format,jpeg");
            json.put("limit", 200);
            json.put("order_by", "updated_at");
            json.put("order_direction", "DESC");
            json.put("parent_file_id", fileId);
            json.put("share_id", shareId);
            json.put("video_thumbnail_process", "video/snapshot,t_1000,f_jpg,ar_auto,w_300");
            String marker = "";
            ArrayList<String> arrayList = new ArrayList<>();
            for(int i=1;i<=50;i++) {
                if (i >1 && marker.isEmpty())
                    break;
                json.put("marker", marker);
                JSONObject data = new JSONObject(postJson(url, json.toString(), headers));
                JSONArray items = data.getJSONArray("items");

                for (int j = 0; j < items.length(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    if (item.getString("type").equals("folder")) {
                        arrayList.add(item.getString("file_id"));
                    } else {
                        //vnd.rn-realmedia-vbr 为rmvb格式
                        if (item.getString("mime_type").contains("video")||item.getString("mime_type").contains("vnd.rn-realmedia-vbr")) {
                            String replace = item.getString("name").replace("#", "_").replace("$", "_");
                            map.put(replace, shareId + "+" + shareToken + "+" + item.getString("file_id")+"+"+item.getString("category")+"+"+_url+"+"+name);
                        }
                    }
                }
                marker = data.getString("next_marker");
            }

            for (String item : arrayList) {
                try {
                    listFiles(map, shareId, shareToken, item,_url,name);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                    return;
                }
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
    }

    public static int cs(String msg1,String regx){
        String msg = msg1.replaceAll(regx, "⑩");
        Matcher ma = Misc.matcher("⑩", msg);//指定字符串出现的次数
        int c = 0;
        while (ma.find()) {
            c++;
        }
        return c;
    }

    public static String getBstr(String ss,boolean f){
        String s = ss;
        if(!f) s = s.replaceFirst("1080", "");
        return s;
    }

    public static  Map<String, String> getBx(List<String> list,Map<String, String> map,String type,boolean f){
        String iname="",rname="",zname="";
        String regx = szRegx;
        Matcher ma = null;
        boolean flag = false;
        String ss = list.get(0);
        String s0 = getBstr(ss, f);
        if(!s0.equals(ss)) flag = true;
        int c = cs(s0,"\\d+");
        Map<String, String> m = new HashMap<>();
        for (String name : list) {
            if (type.isEmpty()||name.contains(type)) {
                zname = name;
                if (Misc.matcher(regx, name).find()) {
                    iname = name.replaceAll(regx, "$2");
                }else {
                    name = name.replace("mp4", "").replace("4K","").replace("4k","").replace("1080P","").replace("1080p","");
                    if (c==1) {
                        if(flag) rname = getBstr(name,f);
                        else rname = name;
                        ma = Misc.matcher("\\d+", rname);
                        while (ma.find()) {
                            iname = ma.group();
                        }
                    }else if(Misc.matcher(".*(\\d+)集.*", name).find()){
                        iname = name.replaceAll(".*(\\d+)集.*", "$1");
                    }else if(Misc.matcher("(\\d+).*", name).find()){
                        iname = name.replaceAll(".*?(\\d+).*", "$1");
                    }else {
                        iname = name;
                    }
                }
                if(iname.contains(".")&&iname.length()>5) iname = iname.substring(0, iname.lastIndexOf("."));
                if(Misc.isNumeric(iname)&&iname.length()==1)iname="0"+iname;
                m.put(iname, map.get(zname));
            }
        }
        return m;
    }


    public static String getAliContent(String url,JSONObject vodAtom) {
        try {
            Matcher matcher = regexAliFolder.matcher(url);
            if (!matcher.find()) {
                return "";
            }
            String shareId = matcher.group(1);
            String fileId = matcher.groupCount() == 3 ? matcher.group(3) : "";
            JSONObject json = new JSONObject();
            json.put("share_id", shareId);
            JSONObject shareLinkJson = new JSONObject(postJson("https://api.aliyundrive.com/adrive/v3/share_link/get_share_by_anonymous", json.toString(), getHeaders()));
            JSONArray fileInfoLists = shareLinkJson.getJSONArray("file_infos");
            if (fileInfoLists.length() == 0) {
                return "";
            }
            JSONObject fileInfo = null;
            if (!TextUtils.isEmpty(fileId)) {
                for (int i = 0; i < fileInfoLists.length(); i++) {
                    JSONObject item = fileInfoLists.getJSONObject(i);
                    if (item.getString("file_id").equals(item.getString("file_id"))) {
                        fileInfo = item;
                        break;
                    }
                }
            } else {
                fileInfo = fileInfoLists.getJSONObject(0);
                fileId = fileInfo.getString("file_id");
            }
            String _name = vodAtom.optString("vod_name", "");
            if (_name.equals("")) {
                _name = shareLinkJson.getString("share_name");
                vodAtom.put("vod_name", _name);
            }
            vodAtom.put("type_name", "阿里云盘");
            ArrayList<String> vodItems = new ArrayList<>();
            ArrayList<String> vodItems3 = new ArrayList<>();
            if (!fileInfo.getString("type").equals("folder")) {
                if (!fileInfo.getString("type").equals("file") || !fileInfo.getString("category").equals("video")) {
                    return "";
                }
                fileId = "root";
            }
            String shareTk = getShareTk(shareId, "");
            Map<String, String> omap = new LinkedHashMap<>();
            listFiles(omap, shareId, shareTk, fileId, url, _name);
            Map<String, String> hashMap = new LinkedHashMap<>();
            hashMap.putAll(omap);
            Map<String, String> nmap = new HashMap<>();
            ArrayList<String> arrayList2 = new ArrayList<>(hashMap.keySet());
            ArrayList<String> arrayList3 = new ArrayList<>();
            arrayList3.addAll(arrayList2);
            String s = TextUtils.join("#", arrayList2);
            String type = "";boolean f = false;
            if (s.contains("4K")) {
                type = "4K";
            }else if (s.contains("4k")) {
                type = "4k";
            }else if (s.contains("1080")) {
                if(!s.contains("1079"))type = "1080";
                else f = true;
            }
            String xfrom = "";
            String from = "AliYun%$$$4K原画&";

            if (!k4.equals("0")) {
                nmap = getBx(arrayList2, hashMap, type,f);
                arrayList2 = new ArrayList<>(nmap.keySet());
                xfrom = "$$$AliYun原视频序";
            }else nmap = hashMap;

            from = from.replace("%", type).replace("&",xfrom);
            from = from+"$$$AliYun原视频";
            Collections.sort(arrayList2);
            for (String item : arrayList2) {
                vodItems.add(item + "$" + nmap.get(item));
            }
            if(vodItems.size()>0){
                ArrayList<String> playLists = new ArrayList<>();
                playLists.add(TextUtils.join("#", vodItems));
                playLists.add(TextUtils.join("#", vodItems));

                ArrayList<String> arrayList4 = new ArrayList<>();
                arrayList4.addAll(arrayList3);
                if(!xfrom.isEmpty()){
                    Collections.sort(arrayList3);
                    for (String item : arrayList3) {
                        vodItems3.add(item + "$" + omap.get(item));
                    }
                    playLists.add(TextUtils.join("#", vodItems3));
                }

                ArrayList<String> vodItems4 = new ArrayList<>();
                for (String item : arrayList4) {
                    vodItems4.add(item + "$" + omap.get(item));
                }
                playLists.add(TextUtils.join("#", vodItems4));

                vodAtom.put("vod_play_url", TextUtils.join("$$$", playLists));
                vodAtom.put("vod_play_from", from);
            }
            JSONObject result = new JSONObject();
            JSONArray list = new JSONArray();
            list.put(vodAtom);
            result.put("list", list);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    @Override
    public String detailContent(List<String> list) {
        return getDetail(list);
    }

    public static String getDetail(List<String> list){
        return getDetail(list, null);
    }
    public static String getDetail(List<String> list,String pic) {
        try {
            String url = list.get(0).trim();
            String[] idInfo = url.split("\\$\\$\\$");
            if (idInfo.length > 0)  url = idInfo[0].trim();
            url = Misc.getRealUrl(url);
            if(pic==null) {
                if (idInfo.length>1&&!idInfo[1].equals("")) {
                    pic = idInfo[1].trim();
                }
                if(pic==null) pic = Misc.getWebName(url, 1);
            }
            String VodName = null,director = "",actor = "",desc = "";
            if (idInfo.length > 1) {
                idInfo[0]=url;
                idInfo[1]=pic;
                if (idInfo.length == 3) {
                    VodName = idInfo[2];
                }
            }
            Matcher matcher = null;
            List<String> vodItems = new ArrayList<>();
            ArrayList<String> aslist = new ArrayList<>();
            JSONArray lists = new JSONArray();
            String typeName = Misc.getWebName(url, 0);
            JSONObject vodAtom = new JSONObject();
            vodAtom.put("vod_id", url);
            vodAtom.put("vod_pic", pic);
            vodAtom.put("type_name", typeName);
            vodAtom.put("vod_content", url);
            vodAtom.put("vod_area", Misc.tip());
            if (Misc.isVip(url) && !url.contains("qq.com") && !url.contains("mgtv.com")) {
                Elements playListA = null;
                Document doc = Jsoup.parse(OkHttpUtil.string(url, Misc.Headers(0,url)));
                String baseUrl = url.replaceAll("(^https?://.*?)(:\\d+)?/.*$", "$1");//https://www.dyk9.com
                if (url.contains("bilibili.com/bangumi/play/ep")) {//第一集地址
                    String nids = url.replaceAll(".*/ep(\\d+).*", "$1");
                    int nid = Integer.parseInt(nids);
                    Elements el = doc.select(".ep-list-progress");
                    if(!el.isEmpty()){
                        String ptext = doc.select(".ep-list-progress").text();
                        String t = ptext.replaceAll("\\d+/(\\d+)", "$1");
                        int z = Integer.parseInt(t);
                        for (int i = 0; i < z; i++) {
                            int x = nid+i;
                            String id = baseUrl+"/bangumi/play/ep"+x+"/";
                            String name = i+1+"";
                            vodItems.add(name + "$" + id);
                        }
                        String playList = TextUtils.join("#", vodItems);
                        vodAtom.put("vod_play_url", playList);
                    }
                } else {
                    vodAtom.put("vod_play_url", "立即播放$" + url);
                }
                VodName = doc.select("head > title").text();
                JSONObject result = new JSONObject();
                vodAtom.put("vod_name", VodName);
                vodAtom.put("vod_play_from", "jx");
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            } else if (Misc.isVip(url) && url.contains("qq.com")) {
                JSONObject result = new JSONObject();
                if (url.contains("m.v.")) {
                    String cid = url.replaceAll("https://m.v.qq.com/x/m/play\\?cid=(\\w+)&.*", "$1");
                    String vid = url.replaceAll("https://m.v.qq.com/x/m/play\\?cid=\\w+&vid=(\\w+)", "$1");
                    url = "https://v.qq.com/x/cover/" + cid + "/" + vid + ".html";
                }
                Document doc = Jsoup.parse(OkHttpUtil.string(url, sHeaders(true)));
                VodName = doc.select("head > title").text();
                Elements playListA = doc.select("div.episode-list-rect__item");
                if (playListA.isEmpty()) {
                    playListA = doc.select("div.episode-list-hor .episode-item");
                }
                if (!playListA.isEmpty()) {
                    for (int j = 0; j < playListA.size(); j++) {
                        Element vod = playListA.get(j);
                        String img = vod.select("img").attr("src");
                        if(img.equals("")||!img.contains("trailerlite")){
                            String a = vod.select("div").attr("data-vid");
                            String b = vod.select("div").attr("data-cid");
                            String id = "https://v.qq.com/x/cover/" + b + "/" + a + ".html";
                            String name = vod.select("div span").text();
                            vodItems.add(name + "$" + id);
                        }
                    }
                    String playList = TextUtils.join("#", vodItems);
                    vodAtom.put("vod_play_url", playList);
                } else {
                    vodAtom.put("vod_play_url", "立即播放$" + url);
                }

                String remarks = doc.select(".intro-wrapper__update-desc").text();
                vodAtom.put("vod_name", VodName);
                vodAtom.put("vod_remarks", remarks);
                vodAtom.put("vod_play_from", "jx");
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            } else if (Misc.isVip(url) && url.contains("mgtv.com")) {
                JSONObject result = new JSONObject();
                Pattern mgtv = Pattern.compile("https://\\S+mgtv.com/b/(\\d+)/(\\d+).html.*");
                Matcher mgtv1 = mgtv.matcher(url);
                if (mgtv1.find()) {
                    String Ep = "https://pcweb.api.mgtv.com/episode/list?video_id=" + mgtv1.group(2);
                    JSONObject Data = new JSONObject(OkHttpUtil.string(Ep, Misc.Headers(0)));
                    VodName = Data.getJSONObject("data").getJSONObject("info").getString("title");
                    JSONArray a = new JSONArray(Data.getJSONObject("data").getString("list"));
                    if (a.length() > 0) {
                        for (int i = 0; i < a.length(); i++) {
                            JSONObject jObj = a.getJSONObject(i);
                            String isnew = jObj.getString("isnew");
                            String isvip = jObj.getString("isvip");
                            if (!(isnew.equals("2")&&isvip.equals("0"))) {
                                VodName = jObj.getString("t4");
                                String id = jObj.getString("video_id");
                                String VodId = "https://www.mgtv.com/b/" + mgtv1.group(1) + "/" + id + ".html";
                                vodItems.add(VodName + "$" + VodId);
                            }
                        }
                        String playList = TextUtils.join("#", vodItems);
                        vodAtom.put("vod_play_url", playList);
                    } else {
                        vodAtom.put("vod_play_url", "立即播放$" + url);
                    }
                }
                vodAtom.put("vod_name", VodName);
                vodAtom.put("vod_play_from", "jx");
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            } else if (Misc.isVideoFormat(url)) {
                JSONObject result = new JSONObject();
                vodAtom.put("vod_name", typeName);
                vodAtom.put("type_name", "直连");
                vodAtom.put("vod_play_from", "player");
                vodAtom.put("vod_play_url", "立即播放$" + url);
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            } else if (url.startsWith("magnet")) {
                matcher = Pattern.compile("(^|&)dn=([^&]*)(&|$)").matcher(URLDecoder.decode(url));
                if (matcher.find()) {
                    VodName = matcher.group(2);
                }
                JSONObject result = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", !VodName.equals("") ? VodName : url);
                vodAtom.put("vod_pic", "https://pic.rmb.bdstatic.com/bjh/1d0b02d0f57f0a42201f92caba5107ed.jpeg");
                vodAtom.put("type_name", "磁力链接");
                vodAtom.put("vod_content", url);
                vodAtom.put("vod_play_from", "magnet");
                vodAtom.put("vod_play_url", "立即播放$" + url);
                lists.put(vodAtom);
                result.put("list", list);
                return result.toString();
            } else if (regexAli.matcher(url).find()) {
                if(VodName!=null) vodAtom.put("vod_name", VodName);
                vodAtom.put("vod_id", TextUtils.join("$$$",idInfo));
                return getAliContent(url,vodAtom);
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                Document doc = null;
                String baseUrl = url.replaceAll("(^https?://.*?)(:\\d+)?/.*$", "$1");//https://www.dyk9.com
                Pattern urlder = Pattern.compile(".*(\\d+.html|\\d+/)$");
                Pattern urlder1 = Pattern.compile(".*-?(\\d+)/");
                String content=null,uri=null,a=null,b=null,hz="",text=null,prefxs=null,detailRex=null;
                boolean fb = true;
                Matcher mh = null;
                if(url.endsWith("/")) hz = "/";
                else {
                    hz=url.replaceAll(".*(\\..*)", "$1");
                    if(hz.length()>6)hz="";
                }

                if(!url.contains("-")&&hz.length()>0){
                    String site2 = fetchRule(false,0).optString("site2", "");
                    if (site2.contains(typeName)) {//https://www.dyk9.com/vod/detail/11203.html 详情页面再点击一次之后 才有播放地址
                        doc = Jsoup.parse(OkHttpUtil.string(url, Misc.Headers(0,url)));
                        content = doc.body().html();
                        hz=url.replaceAll(".*(\\..*)", "$1");
                        detailRex = url.replaceAll(".*/(\\d+)\\..*", "$1");
                        mh = Pattern.compile("href=\"(.*/"+detailRex+"-.*"+hz+")\"").matcher(content);
                        while (mh.find()&&fb){
                            fb=false;
                            url = baseUrl+mh.group(1);
                        }
                        setVideoDesc(vodAtom, content);
                    }
                }

                doc = Jsoup.parse(OkHttpUtil.string(url, Misc.Headers(0,url)));
                VodName = doc.select("head > title").text();
                doc.select("div.playon").remove();
                content = doc.body().html();//[\u4e00-\u9fa5]+
                if(content.equals("")) return "";
                if(urlder.matcher(url).find()){//集合多个视频
                    //String prefxUrl = url.replace(".html", "");
                    //prefxUrl = url.replaceAll("(.*)-\\d+", "$1");
                    //prefxUrl = prefxUrl.replace(baseUrl, "");//  /vod/play/70631-1
                    if(!url.contains("-")){
                        detailRex = url.replaceAll(".*/(\\d+)\\..*", "$1");
                        detailRex = "href(.*"+detailRex+"-\\d+-\\d+."+hz+")\"";
                    }else if(url.split("-").length<2&&urlder1.matcher(url).find()){
                        detailRex = url.replaceAll(".*-(\\d+)"+hz, "$1");
                        detailRex = "href(.*"+detailRex+"-\\d+-\\d+."+hz+")\"";
                    }
                    if (detailRex!=null) {
                        String _con = content,u="";
                        aslist = Misc.subContent(content, "<a", "播放</a>");
                        if (!aslist.isEmpty()) {
                            _con = aslist.get(0);
                            // class="fed-deta-play fed-rims-info fed-btns-info fed-btns-green fed-col-xs4" href="/p/540541-2-1.html">在线
                            u = _con.replaceAll(".*href=\"(.*"+hz+")\".*","$1");
                        }else {
                            mh = Pattern.compile(detailRex).matcher(_con);
                            while (mh.find()&&fb){
                                fb=false;
                                u = mh.group(1);
                                u = u.replaceAll(".*\"(.*)","$1");
                            }
                        }
                        url = baseUrl+u;
                    }
                    //https://dyxs13.com/paly-47817-10-1/
                    if (!hz.equals("")) {
                        prefxs= url.replaceAll("(.*)-\\d+-\\d+"+hz, "$1");
                    }else prefxs= url.replaceAll("(.*)-\\d+-\\d+", "$1");

                    prefxs = prefxs.replace(baseUrl, "");//  /vod/play/70631
                    prefxs = prefxs.replace(".html", "");
                    ArrayList<String> playList = new ArrayList<>();
                    for (int i = 0; i < 12; i++) {
                        fb = false;
                        if(!content.contains(prefxs+"-"+i+"-"))continue;
                        Map<String, String> m = new LinkedHashMap<>();
                        Matcher mat = Pattern.compile("href=\"("+prefxs+"-"+i+"-\\d+"+hz+").*?/a>").matcher(content);
                        while (mat.find()){
                            uri = mat.group(1);
                            a = "<"+mat.group(0);
                            text=a.replaceAll("<[^>]+>",""); //过滤html标签
                            text = text.replaceAll("&amp;|&nbsp;", "");
                            if(text.equals(""))text="其他";
                            uri=baseUrl + uri;
                            if(m.containsKey(uri)){
                                b = m.get(uri);
                                if(b.contains("线路")||b.contains("一集")||b.contains("上集")||b.contains("下集")||(!b.contains("集")&&!b.contains("第"))||Misc.isNumeric(b)){
                                    m.remove(uri);
                                    m.put(uri, text);
                                }
                            }else m.put(uri, text);
                        }

                        if(m.containsKey(url)) {
                            if(VodName.equals("")){
                                b = a.replaceAll(".*title=\"(.*)\".*","$1");
                                if (!b.startsWith("<")) {
                                    b = b.replace("播放", "");
                                    VodName = b;
                                } else VodName = m.get(url);
                            }
                            fb=true;
                        }
                        vodItems = new ArrayList<>();
                        for (String key : m.keySet()) {
                            vodItems.add(m.get(key) + "$" + key);
                        }
                        if(fb) playList.add(0, TextUtils.join("#", vodItems));
                        else playList.add(TextUtils.join("#", vodItems));
                    }
                    ArrayList<String> playFrom = new ArrayList<>();

                    for (int i = 0; i < playList.size(); i++) {
                        playFrom.add("嗅探列表" + (i + 1));
                    }

                    String vod_play_from = TextUtils.join("$$$", playFrom);
                    String vod_play_url = TextUtils.join("$$$", playList);
                    vodAtom.put("vod_play_from", vod_play_from);
                    vodAtom.put("vod_play_url", vod_play_url);
                }else{
                    vodAtom.put("vod_play_from", "嗅探");
                    vodAtom.put("vod_play_url", "立即播放嗅探$" + url);
                }

                JSONObject result = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", VodName);
                vodAtom.put("type_name", "网页");
                setVideoDesc(vodAtom, content);
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            }
        } catch (Throwable th) {
            SpiderDebug.log(th);
            return "";
        }
        return "";
    }

    public static JSONObject setVideoDesc(JSONObject vodAtom, String content) {
        try {
            if(vodAtom.has("vod_actor"))return vodAtom;
            ArrayList<String> aslist = new ArrayList<>();
            String director = "",actor = "",desc = "";
            String directorRegx = "导演：</span>",actorRegx = "主演：</span>",descRegx = "简介：</span>";
            if (!Misc.matcher(descRegx, content).find()) {
                return vodAtom;
            }
            boolean fb = true;
            if (!Misc.matcher(actorRegx, content).find()) {
                fb=false;
                actorRegx = "主演：";
                directorRegx = "导演：";
            }
            aslist = Misc.subContent(content, actorRegx, "</[p|li]>");
            if(!aslist.isEmpty()) actor = aslist.get(0);
            aslist = Misc.subContent(content, directorRegx, "</[p|li]>");
            if(!aslist.isEmpty()) director = aslist.get(0);
            aslist = Misc.subContent(content, descRegx, "</[p|li]>");
            if(!aslist.isEmpty()) {
                desc = aslist.get(0);
                desc = Misc.trim(desc);
                desc = desc.replaceAll("<a.*>", "");
                desc = Misc.delHTMLTag(desc);
            }
            if (!fb) {
                actor = Misc.delHTMLTag(actor);
                director = Misc.delHTMLTag(director);
            }
            vodAtom.put("vod_actor", actor);
            vodAtom.put("vod_director", director);
            vodAtom.put("vod_content", desc);
        } catch (JSONException e) {
            SpiderDebug.log(e);
        }
        return vodAtom;
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        return player(flag, id, vipFlags);
    }

    public static String player(String flag, String id, List<String> vipFlags) {
        JSONObject result = new JSONObject();
        try {
            switch (flag) {
                case "jx": {
                    result.put("parse", 1);
                    result.put("jx", "1");
                    result.put("url", id);
                    result.put("playUrl", "");
                    if(id.contains("bilibili")){
                        result.put("header", Misc.jHeaders(0,id).toString());
                    } else result.put("header", Misc.jHeaders(Misc.type,id).toString());
                    return result.toString();
                }
                case "magnet":
                case "player": {
                    result.put("parse", 0);
                    result.put("playUrl", "");
                    result.put("url", id);
                    result.put("header", Misc.jHeaders(Misc.type,id).toString());
                    return result.toString();
                }
                case "4K原画": {
                    String[] split = id.split("\\+");
                    String url = getOriginalVideoUrl(split[0], split[1], split[2], split[3]);
                    Map<String, List<String>> headerMap = new HashMap<>();
                    OkHttpUtil.stringNoRedirect(url, getHeaders(), headerMap);
                    String videoUrl = OkHttpUtil.getRedirectLocation(headerMap);
                    result = new JSONObject();
                    result.put("parse", "0");
                    result.put("playUrl", "");
                    result.put("url", videoUrl);
                    result.put("header", new JSONObject(getHeaders()).toString());
                    return result.toString();
                }
            }
            if(flag.startsWith( "AliYun")){
                String[] split = id.split("\\+");
                String str3 = split[0];
                String str5 = split[2];
                String url = Proxy.localProxyUrl() + "?do=ali&type=m3u8&share_id=" + str3 + "&file_id=" + str5;
                result.put("parse", "0");
                result.put("playUrl", "");
                result.put("url", url);
                result.put("header", "");
                return result.toString();
            }
            if(flag.startsWith("嗅探")||flag.startsWith("播放")){
                result.put("header", Misc.jHeaders(Misc.type,id).toString());
            }
            if(id.contains("b23.tv"))result.put("header", Misc.jHeaders(1,id).toString());
            result.put("parse", 1);
            result.put("playUrl", "");
            result.put("url", id);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            SpiderDebug.log(e);
        }
        return "";
    }


    //修复软件不支持的格式无法嗅探的问题
    @Override
    public boolean manualVideoCheck() {
        return true;
    }

    private String[] videoFormatList = new String[]{".m3u8", ".mp4", ".mpeg", ".flv", ".m4a",".mp3",".wma",".wmv"};

    @Override
    public boolean isVideoFormat(String url) {
        url = url.toLowerCase();
        if (url.contains("=http") || url.contains("=https%3a%2f") || url.contains("=http%3a%2f")) {
            return false;
        }
        for (String format : videoFormatList) {
            if (url.contains(format)) {
                return true;
            }
        }
        return false;
    }
}