package com.safe_jeonse.server.prompt;

import com.safe_jeonse.server.dto.PromptDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.StringReader;
import java.io.StringWriter;

@Component
public class PromptManger {
    @Value("${spring.prompts.deep-prompt}")
    private String deepPromptTemplate;

    @Value("${spring.prompts.quick-prompt}")
    private String quickPromptTemplate;

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public String generatePrompt(PromptDto promptDto) {
        if (promptDto.getParseResultDto() == null) {
            return getQuickCheckPrompt(promptDto);
        } else {
            return getDeepCheckPrompt(promptDto);
        }
    }

    public String getDeepCheckPrompt(PromptDto promptDto) {
        return renderTemplate(deepPromptTemplate, promptDto);
    }

    public String getQuickCheckPrompt(PromptDto promptDto) {
        return renderTemplate(quickPromptTemplate, promptDto);
    }

    private String renderTemplate(String template, Object context) {
        Mustache mustache = mustacheFactory.compile(new StringReader(template), "prompt");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }
}
