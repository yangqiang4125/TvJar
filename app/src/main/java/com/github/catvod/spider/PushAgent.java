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
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PushAgent extends Spider {

    private static String j = "";
    private static long StopRefresh;
    private static final Map<String, String> l = new HashMap();
    private static final Map<String, Long> q = new HashMap();
    private static final Map<String, Map<String, String>> dizz = new HashMap();
    private static final ReentrantLock ReentrantLock = new ReentrantLock();
    private static final long n = 0;
    public static Integer type=1;
    private static final String SiteUrl = "https://api.aliyundrive.com";
    private static final Pattern AliPLink = Pattern.compile("(https://www.aliyundrive.com/s/[^\"]+)");
    public static Pattern Folder = Pattern.compile("www.aliyundrive.com/s/([^/]+)(/folder/([^/]+))?");
    public static String Token="3a49cf29cf20410997247c6eb4509be9";
    public static JSONObject siteRule = null;
    public static String jsonUrl = "http://test.xinjun58.com/sp/d.json";

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        if (extend != null) {
            String[] arr = extend.split(";");
            if (arr.length > 0) {
                this.Token = arr[0];
                if(arr.length>2){
                    String aid = arr[2];
                    if(Misc.isNumeric(aid)) type = Integer.valueOf(aid);
                }
            }else Token = extend;
           /* if (extend.startsWith("http")) {
                Token = OkHttpUtil.string(extend, null);
            } */
        }
    }

    public JSONObject fetchRule(boolean flag,int t) {
        try {
            if (flag || siteRule == null) {
                String json = OkHttpUtil.string(jsonUrl+"?t="+Time(), null);
                JSONObject jo = new JSONObject(json);
                if(t==0) {
                    String[] fenleis = getRuleVal(jo,"fenlei", "").split("#");
                    for (String fenlei : fenleis) {
                        String[] info = fenlei.split("\\$");
                        jo.remove(info[1]);
                    }
                    siteRule = jo;
                }
                return jo;
            }
        } catch (JSONException e) {
        }
        return siteRule;
    }

    public String getRuleVal(JSONObject o,String key, String defaultVal) {
        String v = o.optString(key);
        if (v.isEmpty() || v.equals("空"))
            return defaultVal;
        return v;
    }

    public String getRuleVal(JSONObject o,String key) {
        return getRuleVal(o,key, "");
    }

    public static long Time() {
        return (System.currentTimeMillis() / 1000) + n;
    }

    public static Object[] ProxyMedia(Map<String, String> map) {
        try {
            String ShareId = map.get("share_id");
            String FileId = map.get("file_id");
            String MediaId = map.get("media_id");
            String Token = getShareTk(ShareId, "");
            ReentrantLock.lock();
            String str4 = dizz.get(FileId).get(MediaId);
            UrlQuerySanitizer urlQuerySanitizer = new UrlQuerySanitizer(str4);
            Long l2 = new Long(urlQuerySanitizer.getValue("x-oss-expires"));
            long longValue = l2.longValue();
            if (longValue - Time() <= 60) {
                VideoDetail(ShareId, Token, FileId);
                Map<String, Map<String, String>> map3 = dizz;
                str4 = map3.get(FileId).get(MediaId);
            }
            ReentrantLock.unlock();
            new HashMap();
            OKCallBack.OKCallBackDefault abVar = new OKCallBack.OKCallBackDefault() {
                public void onResponse(Response response) {
                }

                @Override
                protected void onFailure(Call call, Exception exc) {
                }
            };
            OkHttpClient YM = OkHttpUtil.defaultClient();
            HashMap<String, String> t = Headers();
            OkHttpUtil.get(YM, str4, null, t, abVar);
            ResponseBody body = abVar.getResult().body();
            return new Object[]{200, "video/MP2T", body.byteStream()};
        } catch (Exception e) {
            SpiderDebug.log(e);
            return null;
        }
    }

    public static Object[] vod(Map<String, String> map) {
        String str = map.get("type");
        if (str.equals("m3u8")) {
            return getFile(map);
        }
        if (str.equals("media")) {
            return ProxyMedia(map);
        }
        return null;
    }


    public static Object[] getFile(Map<String, String> map) {
        try {
            String ShareId = map.get("share_id");
            String ShareTK = getShareTk(ShareId, "");
            return new Object[]{200, "application/octet-stream", new ByteArrayInputStream(VideoDetail(ShareId, ShareTK, map.get("file_id")).getBytes())};
        } catch (Exception e) {
            SpiderDebug.log(e);
            return null;
        }
    }

    private static String Post(String str, String str2, Map<String, String> map) {
        OKCallBack.OKCallBackString acVar = new OKCallBack.OKCallBackString() {

            public void onResponse(String r) {
            }

            @Override
            protected void onFailure(Call call, Exception exc) {
            }
        };
        OkHttpClient YM = OkHttpUtil.defaultClient();
        OkHttpUtil.postJson(YM, str, str2, map, acVar);
        return acVar.getResult();
    }

    private static synchronized String getShareTk(String str, String str2) {

        try {
            long b = Time();
            Map<String, String> map = l;
            String str3 = map.get(str);
            Long l2 = q.get(str);
            if (!TextUtils.isEmpty(str3)) {
                if (l2 - b > 600) {
                    return str3;
                }
            }
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("share_id", str);
            jSONObject.put("share_pwd", str2);
            String jSONObject2 = jSONObject.toString();
            JSONObject jSONObject3 = new JSONObject(Post("https://api.aliyundrive.com/v2/share_link/get_share_token", jSONObject2, Headers()));
            String string = jSONObject3.getString("share_token");
            Long valueOf = Long.valueOf(b + jSONObject3.getLong("expires_in"));
            Map<String, Long> map2 = q;
            map2.put(str, valueOf);
            Map<String, String> map3 = l;
            map3.put(str, string);
            return string;
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }

    }


    private static HashMap<String, String> Headers() {
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


    private static void refreshTk() {
        long b = Time();
        String str = j;
        if (str.isEmpty() || StopRefresh - b <= 600) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("refresh_token", Token);
                String jSONObject2 = jSONObject.toString();
                HashMap<String, String> t = Headers();
                JSONObject jSONObject3 = new JSONObject(Post("https://api.aliyundrive.com/token/refresh", jSONObject2, t));
                StringBuilder sb = new StringBuilder();
                String string = jSONObject3.getString("token_type");
                sb.append(string);
                sb.append(" ");
                String string2 = jSONObject3.getString("access_token");
                sb.append(string2);
                j = sb.toString();
                long j2 = jSONObject3.getLong("expires_in");
                StopRefresh = b + j2;
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
        }
    }


    private static String VideoDetail(String shareid, String token, String fileid) {
        int i;
        try {
            String url = SiteUrl + "/v2/file/get_share_link_video_preview_play_info";
            JSONObject Data = new JSONObject();
            Data.put("share_id", shareid);
            Data.put("category", "live_transcoding");
            Data.put("file_id", fileid);
            Data.put("template_id", "");
            HashMap<String, String> headers = Headers();
            headers.put("x-share-token", token);
            String str5 = j;
            headers.put("authorization", str5);
            String jSONObject2 = Data.toString();
            JSONObject jSONObject3 = new JSONObject(Post(url, jSONObject2, headers));
            ArrayList arrayList = new ArrayList();
            arrayList.add("FHD");
            arrayList.add("HD");
            arrayList.add("SD");
            JSONObject jSONObject4 = jSONObject3.getJSONObject("video_preview_play_info");
            JSONArray jSONArray = jSONObject4.getJSONArray("live_transcoding_task_list");
            Iterator it = arrayList.iterator();
            String objectUrl = "";
            while (true) {
                boolean hasNext = it.hasNext();
                i = 0;
                if (!hasNext) {
                    break;
                }
                String str7 = (String) it.next();
                if (!objectUrl.isEmpty()) {
                    break;
                }
                while (true) {
                    if (i < jSONArray.length()) {
                        JSONObject jSONObject5 = jSONArray.getJSONObject(i);
                        String string = jSONObject5.getString("template_id");
                        if (string.equals(str7)) {
                            objectUrl = jSONObject5.getString("url");
                            break;
                        }
                        i++;
                    }
                }
            }
            if (TextUtils.isEmpty(objectUrl)) {
                JSONObject jSONObject6 = jSONArray.getJSONObject(0);
                objectUrl = jSONObject6.getString("url");
            }
            HashMap hashMap = new HashMap();
            HashMap<String, String> header = Headers();
            OkHttpUtil.stringNoRedirect(objectUrl, header, hashMap);
            String d = OkHttpUtil.getRedirectLocation(hashMap);
            String i2 = OkHttpUtil.string(d, Headers());
            String substring = d.substring(0, d.lastIndexOf("/")) + "/";
            ArrayList arrayList2 = new ArrayList();
            HashMap hashMap2 = new HashMap();
            String[] split = i2.split("\n");
            int length = split.length;
            int i3 = 0;
            while (i < length) {
                String str8 = split[i];
                if (str8.contains("x-oss-expires")) {
                    i3++;
                    String sb6 = substring + str8;
                    String sb8 = "" + i3;
                    hashMap2.put(sb8, sb6);
                    str8 = Proxy.localProxyUrl() + "?do=push&type=media&share_id=" + shareid + "&file_id=" + fileid + "&media_id=" + i3;
                }
                arrayList2.add(str8);
                i++;
            }
            dizz.put(fileid, hashMap2);
            return TextUtils.join("\n", arrayList2);
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    public void listFiles(Map<String, String> map, String str, String str2, String str3) {
        String str4;
        try {
            String str5 = "https://api.aliyundrive.com/adrive/v3/file/list";
            HashMap<String, String> Y2 = Headers();
            Y2.put("x-share-token", str2);
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("image_thumbnail_process", "image/resize,w_160/format,jpeg");
            jSONObject.put("image_url_process", "image/resize,w_1920/format,jpeg");
            jSONObject.put("limit", 200);
            jSONObject.put("order_by", "updated_at");
            jSONObject.put("order_direction", "DESC");
            jSONObject.put("parent_file_id", str3);
            jSONObject.put("share_id", str);
            jSONObject.put("video_thumbnail_process", "video/snapshot,t_1000,f_jpg,ar_auto,w_300");
            String str6 = "";
            ArrayList<String> arrayList = new ArrayList();
            for (int i = 1; i <= 50 && (i < 2 || !TextUtils.isEmpty(str6)); i++) {
                jSONObject.put("marker", str6);
                JSONObject jSONObject2 = new JSONObject(Post(str5, jSONObject.toString(), Y2));
                JSONArray jSONArray = jSONObject2.getJSONArray("items");
                int i2 = 0;
                while (i2 < jSONArray.length()) {
                    JSONObject jSONObject3 = jSONArray.getJSONObject(i2);
                    if (jSONObject3.getString("type").equals("folder")) {
                        arrayList.add(jSONObject3.getString("file_id"));
                        str4 = str5;
                    } else {
                        str4 = str5;
                        if (jSONObject3.getString("mime_type").contains("video")) {
                            String replace = jSONObject3.getString("name").replace("#", "_").replace("$", "_");
                            if (replace.length() > 20) {
                                replace = replace.substring(0, 10) + "..." + replace.substring(replace.length() - 10);
                            }
                            String fileIds = jSONObject3.getString("file_id");
                            map.put(replace, str + "+" + str2 + "+" + fileIds);
                        }
                    }
                    i2++;
                    str5 = str4;
                }
                str6 = jSONObject2.getString("next_marker");
            }
            for (String str7 : arrayList) {
                try {
                    listFiles(map, str, str2, str7);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                    return;
                }
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
    }

    public String getAliContent(List<String> list,String pic) {
        String str;
        try {
            String url = list.get(0).trim();
            String[] idInfo = url.split("\\$\\$\\$");
            if (idInfo.length > 0)  url = idInfo[0].trim();
            Pattern pattern = Folder;
            Matcher matcher = pattern.matcher(url);
            if (!matcher.find()) {
                return "";
            }
            String group = matcher.group(1);
            if (matcher.groupCount() == 3) {
                str = matcher.group(3);
            } else {
                str = "";
            }
            String AnonymousUrl = SiteUrl + "/adrive/v3/share_link/get_share_by_anonymous";
            JSONObject Data = new JSONObject();
            Data.put("share_id", group);
            HashMap<String, String> headers = Headers();
            JSONObject jSONObject3 = new JSONObject(Post(AnonymousUrl, Data.toString(), headers));
            JSONArray jSONArray = jSONObject3.getJSONArray("file_infos");
            if (jSONArray.length() == 0) {
                return "";
            }
            JSONObject jSONObject4 = null;
            boolean isEmpty = TextUtils.isEmpty(str);
            if (!isEmpty) {
                int i = 0;
                while (true) {
                    if (i >= jSONArray.length()) {
                        break;
                    }
                    JSONObject jSONObject5 = jSONArray.getJSONObject(i);
                    String string = jSONObject5.getString("file_id");
                    String string2 = jSONObject5.getString("file_id");
                    if (string.equals(string2)) {
                        jSONObject4 = jSONObject5;
                        break;
                    }
                    i++;
                }
                if (jSONObject4 == null) {
                    return "";
                }
            } else {
                jSONObject4 = jSONArray.getJSONObject(0);
                str = jSONObject4.getString("file_id");
            }
            JSONObject jSONObject6 = new JSONObject();
            jSONObject6.put("vod_id", url);
            String string3 = jSONObject3.getString("share_name");
            jSONObject6.put("vod_name", string3);
            jSONObject6.put("vod_pic", pic);
            jSONObject6.put("vod_content", url);
            jSONObject6.put("vod_play_from", "AliYun");
            ArrayList arrayList = new ArrayList();
            String string4 = jSONObject4.getString("type");
            if (!string4.equals("folder")) {
                String string5 = jSONObject4.getString("type");
                if (string5.equals("file")) {
                    String string6 = jSONObject4.getString("category");
                    if (string6.equals("video")) {
                        str = "root";
                    }
                }
                return "";
            }

            String s = getShareTk(group, "");
            Map<String, String> hashMap = new HashMap<>();
            listFiles(hashMap, group, s, str);
            ArrayList arrayList2 = new ArrayList(hashMap.keySet());
            Collections.sort(arrayList2);
            Iterator it = arrayList2.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String str3 = (String) it.next();
                String sb4 = str3 + "$" + hashMap.get(str3);
                arrayList.add(sb4);
            }
            ArrayList arrayList3 = new ArrayList();
            for (int i2 = 0; i2 < 4; i2++) {
                String join = TextUtils.join("#", arrayList);
                arrayList3.add(join);
            }
            String join2 = TextUtils.join("$$$", arrayList3);
            jSONObject6.put("vod_play_url", join2);
            JSONObject result = new JSONObject();
            JSONArray jSONArray2 = new JSONArray();
            jSONArray2.put(jSONObject6);
            result.put("list", jSONArray2);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    @Override
    public String detailContent(List<String> list) {
        try {
            String url = list.get(0).trim();
            String[] idInfo = url.split("\\$\\$\\$");
            if (idInfo.length > 0)  url = idInfo[0].trim();
            String pic = null;
            if (idInfo.length>1&&!idInfo[1].equals("")) {
                pic = idInfo[1].trim();
            }
            if(pic==null) pic = Misc.getWebName(url, 1);
            Pattern pattern = Folder;
            Pattern pattern2 = AliPLink;
            Matcher matcher2 = pattern2.matcher(url);
            Matcher matcher = pattern.matcher(url);
            List<String> vodItems = new ArrayList<>();
            JSONArray lists = new JSONArray();
            String typeName = Misc.getWebName(url, 0);
            JSONObject vodAtom = new JSONObject();
            String VodName = "";
            vodAtom.put("vod_id", url);
            vodAtom.put("vod_pic", pic);
            vodAtom.put("type_name", typeName);
            vodAtom.put("vod_content", url);
            vodAtom.put("vod_area", type);
            if (Misc.isVip(url) && !url.contains("qq.com") && !url.contains("mgtv.com")) {
                Document doc = Jsoup.parse(OkHttpUtil.string(url, Misc.Headers(0,url)));
                VodName = doc.select("head > title").text();
                JSONObject result = new JSONObject();
                vodAtom.put("vod_name", VodName);
                vodAtom.put("vod_play_from", "jx");
                vodAtom.put("vod_play_url", "立即播放$" + url);
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
                    JSONObject Data = new JSONObject(OkHttpUtil.string(Ep, Headers()));
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
                VodName = url;
                if (url.length() > 100) {
                    VodName = url.substring(0, 30) + "..." + url.substring(url.length() - 10);
                }
                JSONObject result = new JSONObject();
                vodAtom.put("vod_name", VodName);
                vodAtom.put("type_name", "磁力");
                vodAtom.put("vod_play_from", "磁力测试");
                vodAtom.put("vod_play_url", "立即播放$" + url);
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            } else if (url.startsWith("http") && (matcher2.find())) {
                return getAliContent(list,pic);
            } else if (url.startsWith("http") && (!matcher.find()) && (!matcher2.find())) {
                Document doc = null;
                String baseUrl = url.replaceAll("(^https?://.*?)(:\\d+)?/.*$", "$1");//https://www.dyk9.com
                Pattern urlder = Pattern.compile(".*\\d+.html");
                Pattern urlder2 = Pattern.compile(".*-\\d+-\\d+");
                String content=null,uri=null,a=null,b=null,hz=null,text=null,prefxs=null,detailRex;
                boolean fb = true;
                Matcher mh = null;
                if(!url.contains("-")){
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
                    }
                }

                doc = Jsoup.parse(OkHttpUtil.string(url, Misc.Headers(0,url)));
                VodName = doc.select("head > title").text();
                doc.select("div.playon").remove();
                content = doc.body().html();//[\u4e00-\u9fa5]+
                if(urlder.matcher(url).find()){//集合多个视频
                    //String prefxUrl = url.replace(".html", "");
                    //prefxUrl = url.replaceAll("(.*)-\\d+", "$1");
                    //prefxUrl = prefxUrl.replace(baseUrl, "");//  /vod/play/70631-1
                    if(!url.contains("-")){
                        detailRex = url.replaceAll(".*/(\\d+)\\..*", "$1");
                        mh = Pattern.compile("href=\"(.*/"+detailRex+"-.*.html)\"").matcher(content);
                        while (mh.find()&&fb){
                            fb=false;
                            url = baseUrl+mh.group(1);
                        }
                    }

                    prefxs= url.replaceAll("(.*)-\\d+-\\d+.html", "$1");
                    prefxs = prefxs.replace(baseUrl, "");//  /vod/play/70631
                    prefxs = prefxs.replace(".html", "");
                    ArrayList<String> playList = new ArrayList<>();
                    for (int i = 0; i < 9; i++) {
                        fb = false;
                        if(!content.contains(prefxs+"-"+i+"-"))continue;
                        Map<String, String> m = new LinkedHashMap<>();
                        Matcher mat = Pattern.compile("href=\"("+prefxs+"-"+i+"-\\d+.html).*?/a>").matcher(content);
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
                }else if(urlder2.matcher(url).find()){//https://dyxs13.com/paly-215645-9-1/
                    Map<String, String> m = new LinkedHashMap<>();
                    String s = "";
                    if(url.endsWith("/")) s = "/";
                    prefxs= url.replaceAll(baseUrl+"(.*)-\\d+"+s, "$1");
                    Matcher mat = Pattern.compile("href=\"("+prefxs+"-\\d+"+s+").*?/a>").matcher(content);
                    while (mat.find()){
                        uri = mat.group(1);
                        a = "<"+mat.group(0);
                        text=a.replaceAll("<[^>]+>",""); //过滤html标签
                        text = text.replaceAll("&amp;|&nbsp;", "");
                        if(text.equals(""))text="其他";
                        uri=baseUrl + uri;
                        if(m.containsKey(uri)) m.remove(uri);
                        m.put(uri, text);
                    }
                    if(m.containsKey(url)) {
                        if(VodName.equals("")){
                            b = a.replaceAll(".*title=\"(.*)\".*","$1");
                            if (!b.startsWith("<")) {
                                b = b.replace("播放", "");
                                VodName = b;
                            } else VodName = m.get(url);
                        }
                    }
                    vodItems = new ArrayList<>();
                    for (String key : m.keySet()) {
                        vodItems.add(m.get(key) + "$" + key);
                    }

                    String playList = TextUtils.join("#", vodItems);
                    vodAtom.put("vod_play_from", "嗅探列表");
                    vodAtom.put("vod_play_url", playList);
                } else{
                    vodAtom.put("vod_play_from", "嗅探");
                    vodAtom.put("vod_play_url", "立即播放嗅探$" + url);
                }

                JSONObject result = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", VodName);
                vodAtom.put("type_name", "嗅探");
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            }
        } catch (Throwable throwable) {

        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        JSONObject result = new JSONObject();
        try {
            switch (flag) {
                case "jx": {
                    result.put("parse", 1);
                    result.put("jx", "1");
                    result.put("url", id);
                    if(id.contains("bilibili"))result.put("header", Misc.jHeaders(0,id).toString());
                    else result.put("header", Misc.jHeaders(type,id).toString());
                    return result.toString();
                }
                case "player": {
                    result.put("parse", 0);
                    result.put("playUrl", "");
                    result.put("url", id);
                    result.put("header", Misc.jHeaders(type,id).toString());
                    return result.toString();
                }
                case "AliYun":
                    refreshTk();
                    String[] split = id.split("\\+");
                    String str3 = split[0];
                    String str5 = split[2];
                    String url = Proxy.localProxyUrl() + "?do=push&type=m3u8&share_id=" + str3 + "&file_id=" + str5;
                    result.put("parse", "0");
                    result.put("playUrl", "");
                    result.put("url", url);
                    result.put("header", "");
                    return result.toString();
            }
            if(flag.startsWith("嗅探")){
                result.put("parse", 1);
                result.put("playUrl", "");
                result.put("url", id);
                result.put("header", Misc.jHeaders(type,id).toString());
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SpiderDebug.log(e);
        }
        return "";
    }

}