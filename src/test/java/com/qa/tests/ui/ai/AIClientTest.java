package com.qa.tests.ui.ai;

import com.qa.framework.ai.AIClient;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AIClientTest {

    @Test
    public void verifyOllamaConnection() throws Exception {

        try (AIClient aiClient = new AIClient()) {

            String response = aiClient.generateResponse(
                    "Explain in one sentence what software testing is."
            );

            System.out.println("Ollama Response: " + response);

            Assert.assertNotNull(response, "AI response should not be null");
            Assert.assertFalse(response.isBlank(), "AI response should not be empty");
        }
    }
}
