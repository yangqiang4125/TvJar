package com.github.catvod.utils;
import android.net.Uri;
import android.os.Build;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.parser.Base64Utils;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Misc {
    public static final String UaWinChrome = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36";
    public static final String DeAgent = "Dalvik/2.1.0 (Linux; U; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.ID + ")";
    public static final String MoAgent = "Mozilla/5.0 (Linux; Android 11; Ghxi Build/RKQ1.200826.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/12.16 SearchCraft/3.9.1 (Baidu; P1 11)";
    public static String jsonUrl = "http://test.xinjun58.com/sp/d.json";
    private static String a = "(https:\\/\\/www.aliyundrive.com\\/s\\/[^\\\"]+)";
    public static final Pattern regexAli = Pattern.compile("(https://www.aliyundrive.com/s/[^\"]+)");
    public static String refreshToken="";
    public static JSONObject siteRule = null;
    public static Integer type=1;
    public static String btype="N";
    public static boolean rflag = false;
    public static String apikey = "0ac44ae016490db2204ce0a042db2916";//豆瓣key
    public static boolean isVip(String url) {
        // 适配2.0.6的调用应用内解析列表的支持, 需要配合直连分析一起使用，参考cjt影视和极品直连
        try {
            boolean isVip = false;
            String host = Uri.parse(url).getHost();
            String[] vipWebsites = new String[]{"iqiyi.com", "v.qq.com", "youku.com", "le.com", "tudou.com", "mgtv.com", "sohu.com", "acfun.cn", "bilibili.com", "baofeng.com", "pptv.com"};
            for (int b = 0; b < vipWebsites.length; b++) {
                if (host.contains(vipWebsites[b])) {
                    if ("iqiyi.com".equals(vipWebsites[b])) {
                        //爱奇艺需要特殊处理
                        if (url.contains("iqiyi.com/a_") || url.contains("iqiyi.com/w_") || url.contains("iqiyi.com/v_")) {
                            isVip = true;
                            break;
                        }
                    } else {
                        isVip = true;
                        break;
                    }
                }
            }
            return isVip;
        } catch (Exception e) {
        }
        return false;
    }
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public static JSONObject jHeaders(int type,String url) {
        JSONObject headers = new JSONObject();
        try {
            if(type==0){
                headers.put("User-Agent", UaWinChrome);
            }else  headers.put("User-Agent", MoAgent);
            headers.put("Accept", " */*");
            headers.put("Connection", " Keep-Alive");
            if (url != null) {
                headers.put("Referer", " " +url);
            }
        } catch (JSONException e) {
        }
        return headers;
    }

    public static HashMap<String, String> Headers(int type,String url) {
        HashMap<String, String> headers = new HashMap<>();
        if(type==0){
            headers.put("User-Agent", UaWinChrome);
        }else  headers.put("User-Agent", MoAgent);

        headers.put("Connection", " Keep-Alive");
        if (url != null) {
            String baseUrl = url.replaceAll("(^https?://(\\w+\\.){2}\\w+).*$", "$1");
            headers.put("Referer", " " +baseUrl);
        }
        return headers;
    }
    public static HashMap<String, String> Headers(int type) {
        return Headers(type,null);
    }

    public static String getWebName(String url,int type){
        if (url.contains("mgtv.com")) {
            if(type==0) return "芒果TV";
            if(type==1) return "http://image.xinjun58.com/sp/pic/bg/mgtv.jpg";
        }
        if (url.contains("qq.com")) {
            if(type==1) return "http://image.xinjun58.com/sp/pic/bg/qq.jpg";
            return "腾讯视频";
        }
        if (url.contains("iqiyi.com")) {
            if(type==1) return "http://image.xinjun58.com/sp/pic/bg/iqiyi.jpg";
            return "爱奇艺";
        }
        if (url.contains("youku.com")) {
            if(type==1) return "http://image.xinjun58.com/sp/pic/bg/youku.jpg";
            return "优酷";
        }
        if (url.contains("bilibili.com")) {
            if(type==1) return "http://image.xinjun58.com/sp/pic/bg/bili.jpg";
            return "哗哩哔哩";
        }
        if (url.startsWith("magnet")) {
            return "磁力";
        }
        if (url.contains("aliyundrive")) {
            if(type==1) return "http://image.xinjun58.com/sp/pic/bg/ali.jpg";
            return "阿里云";
        }
        if(type==1)return "http://image.xinjun58.com/sp/pic/bg/zl.jpg";
        String host = Uri.parse(url).getHost();
        return host;
    }

    private static final Pattern snifferMatch = Pattern.compile("http((?!http).){26,}?\\.(m3u8|mp4)\\?.*|http((?!http).){26,}\\.(m3u8|mp4)|http((?!http).){26,}?/m3u8\\?pt=m3u8.*|http((?!http).)*?default\\.ixigua\\.com/.*|http((?!http).)*?cdn-tos[^\\?]*|http((?!http).)*?/obj/tos[^\\?]*|http.*?/player/m3u8play\\.php\\?url=.*|http.*?/player/.*?[pP]lay\\.php\\?url=.*|http.*?/playlist/m3u8/\\?vid=.*|http.*?\\.php\\?type=m3u8&.*|http.*?/download.aspx\\?.*|http.*?/api/up_api.php\\?.*|https.*?\\.66yk\\.cn.*|http((?!http).)*?netease\\.com/file/.*");

    public static boolean isVideoFormat(String url) {
        if (snifferMatch.matcher(url).find()) {
            if (url.contains("cdn-tos") && url.contains(".js")) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static String fixUrl(String base, String src) {
        try {
            if (src.startsWith("//")) {
                Uri parse = Uri.parse(base);
                src = parse.getScheme() + ":" + src;
            } else if (!src.contains("://")) {
                Uri parse = Uri.parse(base);
                src = parse.getScheme() + "://" + parse.getHost() + src;
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return src;
    }

    public static boolean isBlackVodUrl(String input, String url) {
        if (url.contains("973973.xyz") || url.contains(".fit:"))
            return true;
        return false;
    }

    public static JSONObject fixJsonVodHeader(JSONObject headers, String input, String url) throws JSONException {
        if (headers == null)
            headers = new JSONObject();
        if (input.contains("www.mgtv.com")) {
            headers.put("Referer", " ");
            headers.put("User-Agent", " Mozilla/5.0");
        } else if (url.contains("titan.mgtv")) {
            headers.put("Referer", " ");
            headers.put("User-Agent", " Mozilla/5.0");
        } else if (input.contains("bilibili")) {
            headers.put("Referer", " https://www.bilibili.com/");
            headers.put("User-Agent", " " + Misc.UaWinChrome);
        }else {
            headers.put("User-Agent", DeAgent);
        }
        headers.put("Connection", " Keep-Alive");
        return headers;
    }

    public static JSONObject jsonParse(String input, String json) throws JSONException {
        JSONObject jsonPlayData = new JSONObject(json);
        String url;
        if (jsonPlayData.has("data")) {
            url = jsonPlayData.getJSONObject("data").getString("url");
        } else {
            url = jsonPlayData.getString("url");
        }
        if (url.startsWith("//")) {
            url = "https:" + url;
        }
        if (!url.startsWith("http")) {
            return null;
        }
        if (url.equals(input)) {
            if (isVip(url) || !isVideoFormat(url)) {
                return null;
            }
        }
        if (Misc.isBlackVodUrl(input, url)) {
            return null;
        }
        JSONObject headers = new JSONObject();
        String ua = jsonPlayData.optString("user-agent", MoAgent);
        if (ua.trim().length() > 0) {
            headers.put("User-Agent", " " + ua);
        }
        String referer = jsonPlayData.optString("referer", "");
        if (referer.trim().length() > 0) {
            headers.put("Referer", " " + referer);
        }

        headers = Misc.fixJsonVodHeader(headers, input, url);
        JSONObject taskResult = new JSONObject();
        taskResult.put("header", headers);
        taskResult.put("url", url);
        return taskResult;
    }

    public static Charset CharsetUTF8 = Charset.forName("UTF-8");
    public static Charset CharsetIOS8859 = Charset.forName("iso-8859-1");

    public static String MD5(String src, Charset charset) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(src.getBytes(charset));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                int v = bytes[i] & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    sb.append(0);
                }
                sb.append(hv);
            }
            return sb.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Matcher matcher(String regx, String content) {
        Pattern pattern = Pattern.compile(regx);
        return pattern.matcher(content);
    }
    public static String trim(String str) {
        return str == null ? str : str.replaceAll("^[\\s　|\\s ]*|[\\s　|\\s ]*$", "");
    }

    public static String delHTMLTag(String htmlStr){
        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式
        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
        Matcher m_html=p_html.matcher(htmlStr);
        htmlStr=m_html.replaceAll(""); //过滤html标签
        htmlStr = htmlStr.replaceAll("&amp;|&nbsp;", " ");
        return htmlStr.trim(); //返回文本字符串
    }

    public static ArrayList<String> subContent(String content, String startFlag, String endFlag) {
        ArrayList<String> result = new ArrayList<>();
        if (startFlag.isEmpty() && endFlag.isEmpty()) {
            result.add(content);
            return result;
        }
        try {
            Pattern pattern = Pattern.compile(escapeExprSpecialWord(startFlag) + "(.*?)" + escapeExprSpecialWord(endFlag));
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                result.add(matcher.group(1));
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return result;
    }
    public static String substring(String text) {
        return substring(text, 1);
    }

    public static String substring(String text, int num) {
        if (text != null && text.length() > num) {
            return text.substring(0, text.length() - num);
        } else {
            return text;
        }
    }
    public static String escapeExprSpecialWord(String regexStr) {
        if (!regexStr.isEmpty()) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (regexStr.contains(key)) {
                    regexStr = regexStr.replace(key, "\\" + key);
                }
            }
        }
        return regexStr;
    }

    public static String getRealUrl(String url){
        if (url.contains("upyunso.com/download")) {
            url = Base64Utils.sendGet(url);
        } else if (url.contains("alipansou") || url.contains("zhaoziyuan")) {
            Matcher matcher = matcher(a, OkHttpUtil.string(url, null));
            if (!matcher.find()) {
                return "";
            }
            url = matcher.group(1).replaceAll("\\\\", "");
        }
        return url;
    }
    public static String tip(){
        if (rflag) {
            String text = btype + type + refreshToken;
            return text;
        }
        return "";
    }
}