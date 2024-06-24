package org.example;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class HttpClientUtil {

    public static String URL = "http://127.0.0.1:8080/sqrt/";
    public static String doGet(String url) throws IOException {

        HttpClient httpClient = new HttpClient();
        // 设置http连接超时为5秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        GetMethod getMethod = new GetMethod(url);
        // 设置get请求超时为5秒
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
        // 设置请求重试处理，用的是默认的重试处理：请求三次
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

        StringBuffer response = new StringBuffer();
        BufferedReader br = null;
        // 执行HTTP GET请求
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            // 判断访问的状态码
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("请求出错：" + getMethod.getStatusLine());
            }
            // 读取http响应内容
            InputStream inputStream = getMethod.getResponseBodyAsStream();
            br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } catch (HttpException e) {
            // 发生http异常，可能是协议不对或者返回的内容有问题
            throw new HttpException();
        } catch (IOException e) {
            // 发生IO异常
            throw new IOException();
        } finally {
            if(br!=null) {
                br.close();
            }
            // 释放连接
            getMethod.releaseConnection();
        }
        return response.toString();
    }

    public static boolean sendRequest() throws IOException {
        // 随机生成整数
        int number = new Random().nextInt(Integer.MAX_VALUE) + 1;
        System.out.println(number);
        long startTime = System.currentTimeMillis();
        String strSqrt = HttpClientUtil.doGet(URL + number);
        long endTime = System.currentTimeMillis();
        long delayTime = endTime - startTime;
        System.out.println(strSqrt);
        System.out.println(delayTime);
        if (delayTime < 100 || delayTime > 200) {
            System.out.println("错误情况！");
            return false;
        } else {
            return true;
        }
    }
}
