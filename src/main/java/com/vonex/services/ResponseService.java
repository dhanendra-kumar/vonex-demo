package com.vonex.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonex.dto.HttpResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.vonex.constants.APIConstants.*;

@Component
public class ResponseService {

    private final static Logger log = LoggerFactory.getLogger(HttpBuilderService.class);

    @Autowired
    HttpBuilderService httpBuilderService;
    @Autowired
    ObjectMapper objectMapper;

    public String getRequestToken(LocalDateTime requestStartTime, Map<String, String> headers) throws SSLException {
        String requestToken = "";
        do {
            try {
                HttpResponseDTO response = httpBuilderService.httpGet(API_HOST_NAME + REQUEST_TOKRN_END_POINT, headers);
                if (response.getStatus() == 200) {
                    Map jsonRespone = objectMapper.readValue(response.getText(), Map.class);
                    requestToken = (String) jsonRespone.get("request");
                    if (!ObjectUtils.isEmpty(requestToken)) {
                        break;
                    }
                }
            } catch (SSLException ex) {
                throw ex;
            } catch (IOException ex) {
                log.error("Exception occured while parsing response.");
            }
        } while (LocalDateTime.now().minusSeconds(50).isBefore(requestStartTime));
        return requestToken;
    }

    public String askQuestion(String requestToken, LocalDateTime requestStartTime, Map<String, String> headers) throws SSLException {
        String queryParams = "?request=" + requestToken;
        String answerToken = "";
        while (LocalDateTime.now().minusSeconds(50).isBefore(requestStartTime)) {
            try {
                HttpResponseDTO response = httpBuilderService.httpGet(API_HOST_NAME + ASK_END_POINT + queryParams, headers);
                if (response.getStatus() == 200) {
                    Map jsonRespone = objectMapper.readValue(response.getText(), Map.class);
                    if (!jsonRespone.keySet().contains("error")) {
                        answerToken = (String) jsonRespone.get("answer");
                        if (!ObjectUtils.isEmpty(answerToken)) {
                            break;
                        }
                    }
                }
            } catch (SSLException ex) {
                throw ex;
            } catch (IOException ex) {
                log.error("Exception occured while parsing response.");
            }
        }
        return answerToken;
    }

    public String getAnswerResponse(String answerToken, LocalDateTime requestStartTime, Map<String, String> headers) throws SSLException {
        String queryParams = "?request=" + answerToken;
        String answer = "";
        while (LocalDateTime.now().minusSeconds(50).isBefore(requestStartTime)) {
            try {
                HttpResponseDTO response = httpBuilderService.httpGet(API_HOST_NAME + ANSWER_END_POINT + queryParams, headers);
                if (response.getStatus() == 200) {
                    Map jsonRespone = objectMapper.readValue(response.getText(), Map.class);
                    answer = (String) jsonRespone.get("answer");
                    if (!ObjectUtils.isEmpty(answer)) {
                        break;
                    }
                }
            } catch (SSLException ex) {
                throw ex;
            } catch (IOException ex) {
                log.error("Exception occured while parsing response.");
            }
        }
        return answer;
    }

    public String getAnswer(String userAgent) {
        LocalDateTime requestStartTime = LocalDateTime.now();
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", userAgent + " Vonex Request");
        String answer = "";
        try {
            String requestToken = getRequestToken(requestStartTime, headers);
            if (!ObjectUtils.isEmpty(requestToken)) {
                String answerToken = askQuestion(requestToken, requestStartTime, headers);
                if (!ObjectUtils.isEmpty(answerToken)) {
                    answer = getAnswerResponse(answerToken, requestStartTime, headers);
                }
            }
            if (ObjectUtils.isEmpty(answer)) {
                answer = "API Took more than 50 seconds to respond (Timeout), Please try again.";
            }
        } catch (SSLException ex) {
            answer = "SSL Handshake Failed.";
        }
        return answer;
    }
}
