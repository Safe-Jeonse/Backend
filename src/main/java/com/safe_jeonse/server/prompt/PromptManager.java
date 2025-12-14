package com.safe_jeonse.server.prompt;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.safe_jeonse.server.dto.PromptDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;

@Component
public class PromptManager {
    @Value("${prompts.deep-prompt}")
    private String deepPromptTemplate;

    @Value("${prompts.quick-prompt}")
    private String quickPromptTemplate;

    @Value("${prompts.system}")
    private String systemPromptTemplate;

    @Value("${prompts.apt-prompt}")
    private String aptPromptTemplate;

    @Value("${prompts.apt-system-prompt}")
    private String aptSystemPromptTemplate;

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public String getDeepCheckPrompt(PromptDto promptDto) {
        return renderTemplate(deepPromptTemplate, promptDto);
    }

    public String getQuickCheckPrompt(PromptDto promptDto) {
        return renderTemplate(quickPromptTemplate, promptDto);
    }

    public String getSystemPrompt(PromptDto promptDto) {
        return renderTemplate(systemPromptTemplate, promptDto);
    }

    private String renderTemplate(String template, Object context) {
        Mustache mustache = mustacheFactory.compile(new StringReader(template), "prompt");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }

    public String getAptPricePrompt(String address, String apartmentName, String exclusiveArea) {
        var context = new java.util.HashMap<String, String>();
        context.put("address", address);
        context.put("apartmentName", apartmentName != null ? apartmentName : "정보 없음");
        context.put("exclusiveArea", exclusiveArea != null ? exclusiveArea : "정보 없음");
        return renderTemplate(aptPromptTemplate, context);
    }

    public String getAptPriceSystemPrompt() {
        return aptSystemPromptTemplate;
    }
}

