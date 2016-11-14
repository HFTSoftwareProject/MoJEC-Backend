package de.hftstuttgart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Locale;

@SpringBootApplication
public class MoJecBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoJecBackendApplication.class, args);
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jacksonConfiguration() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.indentOutput(true)
                .serializationInclusion(JsonInclude.Include.NON_NULL) // Don’t include null values
                .serializerByType(Diagnostic.class, new DiagnosticSerializer());
        return builder;
    }

    private class DiagnosticSerializer extends JsonSerializer<Diagnostic> {
        @Override
        public void serialize(Diagnostic diagnostic, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            gen.writeStartObject();
            gen.writeStringField("code", diagnostic.getCode());
            gen.writeNumberField("columnNumber", diagnostic.getColumnNumber());
            gen.writeStringField("kind", diagnostic.getKind().toString());
            gen.writeNumberField("lineNumber", diagnostic.getLineNumber());
            gen.writeStringField("message", diagnostic.getMessage(Locale.ENGLISH));
            gen.writeNumberField("position", diagnostic.getPosition());
            gen.writeStringField("filePath", ((JavaFileObject) diagnostic.getSource()).toUri().getPath());
            gen.writeNumberField("startPosition", diagnostic.getStartPosition());
            gen.writeNumberField("endPosition", diagnostic.getEndPosition());
            gen.writeEndObject();

        }
    }
}
