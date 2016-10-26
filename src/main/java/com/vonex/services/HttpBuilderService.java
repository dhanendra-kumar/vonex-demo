package com.vonex.services;


import com.vonex.dto.HttpResponseDTO;
import com.vonex.util.JsonConverter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class HttpBuilderService {

    @Autowired
    CloseableHttpAsyncClient httpAsyncClient;
    @Autowired
    JsonConverter jsonConverter;

    private final static Logger log = LoggerFactory.getLogger(HttpBuilderService.class);

    /**
     * This method will make HTTP GET request for given uri, with request headers
     *
     * @param uri
     * @param headers
     * @return
     */
    public HttpResponseDTO httpGet(String uri, Map<String, String> headers) {
        HttpGet get = new HttpGet(uri);
        if (Objects.nonNull(headers)) {
            get = (HttpGet) addHeaders(get, headers).orElse(get);
        }
        return consumeEntityAsString(execute(get));
    }

    /**
     * This method will call HTTP post method with JSON request body
     *
     * @param uri
     * @param parameters
     * @param headers
     * @return
     */
    public HttpResponseDTO httpPostWithJsonBody(String uri, Map<String, Object> parameters, Map<String, String> headers) {
        HttpPost post = new HttpPost(uri);
        System.out.println("---- request received for url [ " + uri + " ]");
        if (!ObjectUtils.isEmpty(headers)) {
            post = (HttpPost) addHeaders(post, headers).orElse(post);
        }
        if (!ObjectUtils.isEmpty(parameters)) {
            try {
                HttpEntity entity = new StringEntity(jsonConverter.serialize(parameters).orElse(""), "UTF-8");
                post.setEntity(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return consumeEntityAsString(execute(post));
    }

    /**
     * This method will invoke HTTP post method
     *
     * @param uri
     * @param parameters
     * @param headers
     * @return
     */
    public HttpResponseDTO httpPost(String uri, Map<String, Object> parameters, Map<String, String> headers) {
        HttpPost post = new HttpPost(uri);
        if (!ObjectUtils.isEmpty(headers)) {
            post = (HttpPost) addHeaders(post, headers).orElse(post);
        }

        if (!ObjectUtils.isEmpty(parameters)) {
            Optional<List<NameValuePair>> nameValuePairs = buildParameters(parameters);
            try {
                if (nameValuePairs.isPresent())
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs.get()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return consumeEntityAsString(execute(post));
    }

    /**
     * This is a common method to add headers in current request
     *
     * @param message
     * @param headers
     */
    private Optional<AbstractHttpMessage> addHeaders(final AbstractHttpMessage message, final Map<String, String> headers) {
        Optional<AbstractHttpMessage> optional = Optional.empty();
        if (!ObjectUtils.isEmpty(headers)) {
            headers.entrySet().stream()
                    .map(entry -> new BasicHeader(entry.getKey(), String.valueOf(entry.getValue())))
                    .forEach(message::addHeader);
            optional = Optional.of(message);
        }
        return optional;
    }

    /**
     * This method will take parameters map as input and it'll return List of NameValuePair.
     *
     * @param parameters
     * @return
     */
    private Optional<List<NameValuePair>> buildParameters(Map<String, Object> parameters) {
        Optional<List<NameValuePair>> optional = Optional.empty();
        if (!ObjectUtils.isEmpty(parameters)) {
            return Optional.of(
                    parameters.entrySet().stream()
                            .map(e -> new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue())))
                            .collect(Collectors.toList())
            );
        }
        return optional;
    }

    /**
     * This method will consume response and string will be returned as output
     *
     * @param response
     * @return Object of {@link HttpResponseDTO}
     */
    private HttpResponseDTO consumeEntityAsString(HttpResponse response) {
        HttpResponseDTO responseDTO = new HttpResponseDTO();
        if (Objects.nonNull(response)) {
            try {
                responseDTO.setStatus(response.getStatusLine().getStatusCode());
                responseDTO.setText(EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseDTO;
    }

    /**
     * This is the core execute method which will make api calls for given method
     *
     * @param method
     * @return
     */
    private HttpResponse execute(HttpUriRequest method) {
        httpAsyncClient.start();
        Future<HttpResponse> responseFuture = httpAsyncClient.execute(method, null);
        try {
            return responseFuture.get();
        } catch (InterruptedException e) {
			log.error(e.getMessage());
        } catch (ExecutionException e) {
			log.error(e.getMessage());
        }
        return null;
    }
}
