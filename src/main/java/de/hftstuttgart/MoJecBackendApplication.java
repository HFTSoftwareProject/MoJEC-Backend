package de.hftstuttgart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.ComparisonFailure;
import org.junit.runner.notification.Failure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

@SpringBootApplication
public class MoJecBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoJecBackendApplication.class, args);
    }

    /**
     * Configuration for the Jackson JSON serializer
     */
    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jacksonConfiguration() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.indentOutput(true)
                .serializationInclusion(JsonInclude.Include.NON_NULL) // Don’t include null values in the JSON
                .serializerByType(Diagnostic.class, new DiagnosticSerializer())
                .serializerByType(Failure.class, new FailureSerializer());
        return builder;
    }

    /**
     * Custom serializer for the {@link Diagnostic} class used by Jackson
     */
    private class DiagnosticSerializer extends JsonSerializer<Diagnostic> {
        @Override
        public void serialize(Diagnostic diagnostic, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("code", diagnostic.getCode());
            gen.writeNumberField("columnNumber", diagnostic.getColumnNumber());
            gen.writeStringField("kind", diagnostic.getKind().toString());
            gen.writeNumberField("lineNumber", diagnostic.getLineNumber());
            gen.writeStringField("message", diagnostic.getMessage(Locale.ENGLISH));
            gen.writeNumberField("position", diagnostic.getPosition());

            URI uri = ((JavaFileObject) diagnostic.getSource()).toUri();
            gen.writeStringField("javaFileName", new File(uri).getName());
            gen.writeNumberField("startPosition", diagnostic.getStartPosition());
            gen.writeNumberField("endPosition", diagnostic.getEndPosition());
            gen.writeEndObject();
        }
    }

    /**
     * Custom serializer for the {@link Failure} class used by Jackson
     */
    private class FailureSerializer extends JsonSerializer<Failure> {
        @Override
        public void serialize(Failure failure, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("testHeader", failure.getTestHeader());
            gen.writeStringField("message", failure.getMessage());
            gen.writeStringField("trace", failure.getTrace());

            Throwable exception = failure.getException();
            if (exception instanceof ComparisonFailure) {
                gen.writeStringField("expected", ((ComparisonFailure) exception).getExpected());
                gen.writeStringField("actual", ((ComparisonFailure) exception).getActual());
            }
            gen.writeEndObject();
        }
    }
}
