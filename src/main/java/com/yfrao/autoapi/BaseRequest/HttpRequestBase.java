package com.yfrao.autoapi.BaseRequest;

import lombok.Data;

import java.util.HashMap;


@Data
public class HttpRequestBase {

    private String url;
    private String requestType;
    private HashMap<String,String> header;
    private HashMap<String,String> formData;
    private String postBody;
    private String expectResult;
}
