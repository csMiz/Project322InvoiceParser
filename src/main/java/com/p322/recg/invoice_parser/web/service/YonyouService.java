package com.p322.recg.invoice_parser.web.service;

import com.p322.recg.invoice_parser.web.domain.Constant;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class YonyouService {

    public static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };


    public static String getYonYouRequest(String p_date, String p_number, String p_code, String p_verifyCode, String p_priceWithoutTax, String p_type){
        String result =null;
        String url ="https://api.yonyoucloud.com/apis/dst/verifyInvoiceWithType/verifyInvoiceWithType";//请求接口地址
        String method = "POST";
        String paramFormat = "form";
        Map<String, Object> params = new HashMap<String, Object>();//请求参数
        params.put("date", p_date);
        params.put("number", p_number);
        params.put("code", p_code);
        params.put("verifyCode", p_verifyCode);
        params.put("priceWithoutTax", p_priceWithoutTax);
        params.put("type", p_type);

        Map<String, Object> headerParams = new HashMap<String, Object>();//请求头参数
        headerParams.put("apicode", Constant.yonyou);//APICODE
        headerParams.put("Content-Type", "application/json");

        try {
            result = net(url, params, headerParams, method, paramFormat);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return  网络请求字符串
     * @throws Exception
     */
    public static String net(String strUrl, Map<String,Object> params, Map<String,Object> headerParams,String method, String paramFormat) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            String contentType = null;
            if(headerParams.containsKey("Content-Type"))
                contentType = headerParams.get("Content-Type").toString();

            StringBuffer sb = new StringBuffer();
            if(method==null || method.equals("GET")){
                strUrl = strUrl+"?"+urlencode(params);
            }

            trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);

            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if(method==null || method.equals("GET")){
                conn.setRequestMethod("GET");
            }else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
            for (String i : headerParams.keySet()) {
                conn.setRequestProperty(i, headerParams.get(i).toString());
            }
            if("form".equals(paramFormat) && !"application/x-www-form-urlencoded".equals(contentType) && !"application/xml".equals(contentType)) {
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            }
            conn.setUseCaches(false);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params!= null && method.equals("POST")) {
                try {
                    OutputStream out = conn.getOutputStream();
                    if("form".equals(paramFormat)) {
                        if("application/x-www-form-urlencoded".equals(contentType))
                            out.write(urlencode(params).getBytes("utf-8"));
                        else if("application/xml".equals(contentType))
                            out.write(xmlencode(params).getBytes("utf-8"));
                        else
                            out.write(jsonencode(params).getBytes("utf-8"));
                    } else
                        out.write(params.toString().getBytes("utf-8"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String,Object>data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                if(("").equals(i.getKey())) {
                    sb.append(URLEncoder.encode(i.getValue()+"","UTF-8"));
                } else {
                    sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    //将map型转为请求参数型
    public static String jsonencode(Map<String,Object>data) {
        JSONObject jparam = new JSONObject();
        for (Map.Entry i : data.entrySet())
            jparam.put((String)i.getKey(), i.getValue());

        return jparam.toString();
    }

    //将map型转为请求参数型
    public static String xmlencode(Map<String,Object>data) {
        StringBuffer xmlData = new StringBuffer();
        xmlData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        for (Map.Entry i : data.entrySet())
            xmlData.append("<" + i.getKey() + ">" + i.getValue() + "</" + i.getKey() + ">");

        return xmlData.toString();
    }

    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    public static String getInvoiceTypeCode(String input){
        if ("专用发票".equals(input)){
            return "01";
        }
        if ("普通发票".equals(input)){
            return "04";
        }
        return input;

    }

    public static String getInvoiceDateCode(String input){
        //2018年01月15日
        String tmp = input.replace("年","")
                .replace("月","")
                .replace("日","")
                .replace(" ","");
        return tmp;

    }
}
