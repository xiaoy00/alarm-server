package com.xin.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
@Component
public class AlilogUtils {

    private static Logger logger = LogManager.getLogger(AlilogUtils.class);
    @Value("${elk.first.account}")
    String elkFirstAccount;
    @Value("${elk.first.pw}")
    String elkFirstPw;

    public String post(String queryCon, String url){
        String html="";
        try {
            HttpClient httpClient = new HttpClient();
            PostMethod post = new PostMethod(url);

            String auth = "Basic " + Base64.getEncoder().encodeToString((elkFirstAccount + ":" + elkFirstPw).getBytes());
            post.setRequestHeader("Authorization",auth);
            post.setRequestHeader("Accept","application/json, text/plain, */*");
            post.setRequestHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
            post.setRequestHeader("Accept-Language","zh-CN,zh;q=0.9");
            post.setRequestHeader("content-Type","application/x-ndjson");
            post.setRequestHeader("Host","alilog.xin.com");
            post.setRequestHeader("Connection","keep-alive");
            post.setRequestHeader("Referer","http://alilog.xin.com/app/kibana");
            post.setRequestHeader("kbn-version","6.2.3");
            post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");

            RequestEntity entity = new StringRequestEntity(queryCon, "text/html", "utf-8");
            post.setRequestEntity(entity);
            httpClient.executeMethod(post);
            html = post.getResponseBodyAsString();
            //System.out.println(html);
        }catch (Exception e){
            //e.printStackTrace();
            logger.error("【日志报警】---调用接口异常:",e);
        }
        return html;
    }

}
