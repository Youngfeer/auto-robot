package com.yfrao.autoapi.util;

import com.yfrao.autoapi.BaseRequest.HttpRequestBase;
import com.yfrao.autoapi.constant.GlobalVar;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
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
                HttpGet httpGet = new HttpGet(requestBase.getUrl());
                initHttpGet(httpGet, requestBase.getHeader());
                response = client.execute(httpGet);
                HttpEntity getEntity = response.getEntity();
                String getRes = EntityUtils.toString(getEntity);
                return getRes;
            case "post":
                HttpPost httpPost = new HttpPost();
                initHttpPost(httpPost, requestBase);
                response = client.execute(httpPost);
                HttpEntity postEntity = response.getEntity();
                String postRes = EntityUtils.toString(postEntity);
                return postRes;
            case "post_upload":
                HttpPost httpMutil = new HttpPost();
                initHttpMutil(httpMutil,requestBase);
                response = client.execute(httpMutil);
                HttpEntity upEntity = response.getEntity();
                String upRes = EntityUtils.toString(upEntity);
                return upRes;
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

    private void initHttpMutil(HttpPost httpMutil, HttpRequestBase requestBase) {
        if(!CollectionUtils.isEmpty(requestBase.getHeader())){
            httpMutil.setHeaders(castMapToHead(requestBase.getHeader()));
        }
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        Set<String> set = requestBase.getFormData().keySet();
        for(String key:  set){
            if(key.startsWith("${") && key.endsWith("}")) {
                FileBody fileBody = new FileBody(new File(GlobalVar.dirPath + requestBase.getFormData().get(key)));
                multipartEntityBuilder.addPart(key.substring(2,key.length()-1), fileBody);
            }else{
                multipartEntityBuilder.addTextBody(key, requestBase.getFormData().get(key), ContentType.APPLICATION_JSON);
            }
        }
        HttpEntity httpEntity = multipartEntityBuilder.build();
        httpMutil.setEntity(httpEntity);
    }
}
