package com.qa.framework.ai;

import com.google.gson.Gson;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.qa.framework.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class AIClient implements AutoCloseable{

    /*AIClient is the communication layer between our Java framework and Ollama.*/
    private static final String OLLAMA_BASE_URL= ConfigManager.getProperty("ai_base_url");
    private static final String MODEL = ConfigManager.getProperty("ai_model");
    private final Playwright playwright;
    private final APIRequestContext requestContext;
    private final Gson gson;

    public AIClient(){
        playwright = Playwright.create();
        APIRequest request =playwright.request();
        requestContext = request.newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(OLLAMA_BASE_URL).setExtraHTTPHeaders(Map.of("Content-type","application/json"))
        );
        gson = new Gson();
    }

    public String generateResponse(String prompt){
        Map<String,Object> requestData =
                new HashMap<>();
        requestData.put("model",MODEL);
        requestData.put("prompt",prompt);
        requestData.put("stream",false);
        String requestBody =gson.toJson(requestData);
        APIResponse response= requestContext.post(
                "/api/generate",RequestOptions.create().setData(requestBody));
        try{
            if(!response.ok()){
                throw new RuntimeException("Ollama request failed, API status:"+response.status());
            }
        String responseBody = response.text();
        return extractResponse(responseBody);
        }finally{
            response.dispose();
        }
    }

    private String extractResponse(String json){
        OllamaResponse ollamaResponse = gson.fromJson(json,OllamaResponse.class);
        if(ollamaResponse==null || ollamaResponse.getResponse()==null){
            throw new IllegalStateException("" +
                    "Could not extract response from Ollama.");
        }
        return ollamaResponse.getResponse();
    }

    @Override
    public void close() throws Exception {
        try{
            requestContext.dispose();
        }
        finally {
            {
                playwright.close();
            }
        }
    }
}
