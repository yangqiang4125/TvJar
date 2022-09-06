package com.github.catvod.parser;

import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA 2017.2.3.
 * author: YangQiang
 * DateTime: 2022/9/5 11:18
 * updateTime: 2022/9/5 11:18
 * Description:
 */
public class Base64Utils {

    public static JSONArray getJSONByUrl(String url){
        JSONArray a2 = new JSONArray();
        try {
            String json = OkHttpUtil.string(url, Misc.Headers(1,url));
            String s = getBase64(json),title="",uri="",atime="",zt="";
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.optString("status", "").equals("success")) {
                JSONObject o = null, o2 = null;
                JSONObject jsonObject2 = jsonObject.getJSONObject("result"),oc = null;
                JSONArray array = jsonObject2.getJSONArray("items"), ac2 = new JSONArray();
                int z = 15, f=0;
                int len = array.length() > z ? z : array.length();
                for (int i = 1; i < len; i++) {
                    o2 = new JSONObject();
                    o = (JSONObject) array.get(i);
                    atime = o.optString("available_time", "");
                    if (atime.startsWith("2099")) {
                        title = o.optString("title", "");
                        ac2 = o.getJSONArray("content");
                        f = ac2.length();
                        if (f > 0) {
                            oc = (JSONObject) ac2.get(0);
                            zt = oc.optString("title", "");
                            if (zt.contains("4K")||zt.contains("4k")) {
                                title = title + " 4K";
                            }else if(zt.contains("1080")) title = title + " 1080P";
                            title = title  + "/" + f;
                        }

                        uri = o.optString("page_url", "");
                        o2.put("title", title);
                        o2.put("url", "upyunso.com/"+uri);
                        a2.put(o2);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return a2;
    }

    public static String getBase64(String json){
        String s = JsonBasic.getBase64(json);
        return s;
    }

    //获取真实地址
    //String url = sendGet("https://www.upyunso.com/download.html?url=3DEAD2A1C2069EF6050451BC6D38A3146583A5455F0229464F2B4BDD8934AE28FD535DA70D084ADC8B47CF78DBA2CC76181586F5F3DECAEB37C0B650A3CDDC081397A8787EC99ED49CB9133839352CBAA1730CF7734083878A");
    public static String sendGet(String url) {
        String result = "";
        try {
            String r = url.replaceAll(".*url=(.*)", "$1");
            String url1 = "https://api.upyunso.com/download?url=" + r;
            String s = OkHttpUtil.string(url1, Misc.Headers(1,url));
            s = getBase64(s);
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.optString("status", "").equals("success")) {
                JSONObject jsonObject2 = jsonObject.getJSONObject("result");
                return jsonObject2.getString("res_url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }



}
