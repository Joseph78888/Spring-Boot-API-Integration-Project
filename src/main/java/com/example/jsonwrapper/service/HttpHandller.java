package com.example.jsonwrapper.service;

import java.util.Map;

public interface HttpHandller {

    <T> T get(String url, Class<T> responseType, Map<String, String> headers,
              Map<String, String> queryParams, Map<String, String> bodyParams);

    <T> T post(String url, Object body,Class<T> responseType, Map<String, String> headers,
              Map<String, String> queryParams, Map<String, String> bodyParams);

    <T> T delete(String url, Class<T> responseType, Map<String, String> headers,
                 Map<String, String> queryParams, Map<String, String> bodyParams);

    <T> T put(String url, Class<T> responseType, Map<String, String> headers);

}
