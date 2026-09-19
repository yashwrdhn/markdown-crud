package com.mvp;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.util.stream.Stream;

public class Main {

        public static void main(String[] args) {
            // Initialize the Ollama model instance
            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("gemma3:4b")
                    .temperature(0.7)
                    .build();

            // Send prompt and get response
            String response = model.generate("Explain quantum computing in one simple sentence.");

            System.out.println("Response from Ollama:");
            System.out.println(response);
        }
}



