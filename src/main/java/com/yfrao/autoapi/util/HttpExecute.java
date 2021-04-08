package com.yfrao.autoapi.util;

import com.yfrao.autoapi.BaseRequest.HttpRequestBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class HttpExecute {

    public String execute(HttpRequestBase requestBase) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response;
        String requetMethod = requestBase.getRequestType().toLowerCase();
        switch (requetMethod){
            case "get":
                HttpGet httpGet = new HttpGet();
                initHttpGet(httpGet, requestBase.getHeader());
                response = client.execute(httpGet);
                return response.getEntity().toString();
            case "post":
                HttpPost httpPost = new HttpPost();
                initHttpPost(httpPost, requestBase);
                response = client.execute(httpPost);
                return response.getEntity().toString();
            default:
                System.out.println("请求方式不支持，请确认！");

        }
        return null;
    }


    public void initHttpGet(HttpGet httpGet, HashMap<String,String> header){
        if(!CollectionUtils.isEmpty(header)){
            httpGet.setHeaders(castMapToHead(header));
        }
    }

    private Header[] castMapToHead(HashMap<String, String> headParam) {
        ArrayList<Header> headList = new ArrayList<Header>();
        Set<String> keySet = headParam.keySet();
        if (keySet.size() > 0) {
            for (String key : keySet) {
                headList.add(new BasicHeader(key, headParam.get(key)));
            }
        }
        return headList.toArray(new Header[headList.size()]);
    }

    public void initHttpPost(HttpPost httpPost, HttpRequestBase httpRequest){
        if(!CollectionUtils.isEmpty(httpRequest.getHeader())){
            httpPost.setHeaders(castMapToHead(httpRequest.getHeader()));
        }
        if(!CollectionUtils.isEmpty(httpRequest.getFormData())){
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Set<String> keySet = httpRequest.getFormData().keySet();
            for(String key : keySet){
                list.add(new BasicNameValuePair(key,httpRequest.getFormData().get(key)));
            }
            if(list.size()>0){
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(list, Charset.forName("utf8"));
                httpPost.setEntity(formEntity);
            }
        }
        if(StringUtils.isNotBlank(httpRequest.getPostBody())){
            StringEntity entity = new StringEntity(httpRequest.getPostBody(), Charset.forName("utf8"));
            httpPost.setEntity(entity);
        }
    }
}
