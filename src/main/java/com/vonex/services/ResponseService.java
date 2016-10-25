package com.vonex.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonex.dto.HttpResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ResponseService {

    private String API_HOST_NAME = "https://test.vonex.com.au";
    private String REQUEST_TOKRN_END_POINT = "/api/initial-request";
    private String ASK_END_POINT = "/api/ask";
    private String ANSWER_END_POINT = "/api/answer";
    private final static Logger log = LoggerFactory.getLogger(HttpBuilderService.class);

    @Autowired
    HttpBuilderService httpBuilderService;
    @Autowired
    ObjectMapper objectMapper;

    public String getRequestToken(LocalDateTime requestStartTime) {
        String requestToken = "";
        do {
            HttpResponseDTO response = httpBuilderService.httpGet(API_HOST_NAME + REQUEST_TOKRN_END_POINT, null);
            if (response.getStatus() == 200) {
                try {
                    Map jsonRespone = objectMapper.readValue(response.getText(), Map.class);
                    requestToken = (String) jsonRespone.get("request");
                    if (!ObjectUtils.isEmpty(requestToken)) {
                        break;
                    }
                } catch (IOException ex) {
                    log.error("Exception occured while parsing response.");
                }
            }
        } while (LocalDateTime.now().minusSeconds(50).isBefore(requestStartTime));
        return requestToken;
    }

    public String askQuestion(String requestToken, LocalDateTime requestStartTime) {
        String queryParams = "?request=" + requestToken;
        String answerToken = "";
        while (LocalDateTime.now().minusSeconds(50).isBefore(requestStartTime)) {
            HttpResponseDTO response = httpBuilderService.httpGet(API_HOST_NAME + ASK_END_POINT + queryParams, null);
            if (response.getStatus() == 200) {
                try {
                    Map jsonRespone = objectMapper.readValue(response.getText(), Map.class);
                    if (!jsonRespone.keySet().contains("error")) {
                        answerToken = (String) jsonRespone.get("answer");
                        if (!ObjectUtils.isEmpty(answerToken)) {
                            break;
                        }
                    }
                } catch (IOException ex) {
                    log.error("Exception occured while parsing response.");
                }
            }
        }
        return answerToken;
    }

    public String getAnswerResponse(String answerToken, LocalDateTime requestStartTime) {
        String queryParams = "?request=" + answerToken;
        String answer = "";
        while (LocalDateTime.now().minusSeconds(50).isBefore(requestStartTime)) {
            HttpResponseDTO response = httpBuilderService.httpGet(API_HOST_NAME + ANSWER_END_POINT + queryParams, null);
            if (response.getStatus() == 200) {
                try {
                    Map jsonRespone = objectMapper.readValue(response.getText(), Map.class);
                    answer = (String) jsonRespone.get("answer");
                    if (!ObjectUtils.isEmpty(answer)) {
                        break;
                    }
                } catch (IOException ex) {
                    log.error("Exception occured while parsing response.");
                }
            }
        }
        return answer;
    }

    public String getAnswer() {
        LocalDateTime requestStartTime = LocalDateTime.now();
        String requestToken = getRequestToken(requestStartTime);
        String answer = "";
        if (!ObjectUtils.isEmpty(requestToken)) {
            String answerToken = askQuestion(requestToken, requestStartTime);
            if(!ObjectUtils.isEmpty(answerToken)) {
                answer = getAnswerResponse(answerToken, requestStartTime);
            }
        }
        if (ObjectUtils.isEmpty(answer)) {
            answer = "API Took more than 50 seconds to respond (Timeout), Please try again.";
        }
        return answer;
    }
}
