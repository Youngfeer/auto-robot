package com.yfrao.autorobot;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class AutoHttpRequestTest {

    @Test
    public void sendHttpGetRequest(){
        String url  = "http://10.213.8.21/crm-zuul-api/api-b2b-sf-accounting-front/sales/types";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet;
        HttpResponse res;
//        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//        nameValuePairs.add(new BasicNameValuePair("param", "value"));
        try {
//            URIBuilder builder = new URIBuilder(url);
//            builder.setParameters(nameValuePairs);
            httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json;charset=UTF-8");
            res = httpClient.execute(httpGet);
            System.out.println("########################################" + res);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
