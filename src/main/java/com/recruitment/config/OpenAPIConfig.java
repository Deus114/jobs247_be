package com.recruitment.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

	public static final String BEARER_AUTH_SCHEME = "Bearer Authentication";
	public static final String ACCEPT_LANGUAGE_PARAM = "Accept-Language";

	@Value("${server.port}")
	private String serverPort;

	@Bean
	OpenAPI adminOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Recruitment API")
						.version("1.0")
						.description("API Recruitment"))
				.servers(List.of(new Server()
						.url("http://localhost:" + serverPort)
						.description("Local")))
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH_SCHEME,
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.bearerFormat("JWT")
										.scheme("bearer"))
						.addParameters(ACCEPT_LANGUAGE_PARAM, acceptLanguageParameter()));
	}

	@Bean
	OpenApiCustomizer acceptLanguageComponentCustomizer() {
		return openApi -> openApi.getComponents()
				.addParameters(ACCEPT_LANGUAGE_PARAM, acceptLanguageParameter());
	}

	@Bean
	OperationCustomizer acceptLanguageOperationCustomizer() {
		return (operation, handlerMethod) -> {
			boolean alreadyPresent = operation.getParameters() != null
					&& operation.getParameters().stream()
							.anyMatch(parameter -> ACCEPT_LANGUAGE_PARAM.equals(parameter.getName())
									|| (parameter.get$ref() != null
											&& parameter.get$ref().endsWith(ACCEPT_LANGUAGE_PARAM)));
			if (!alreadyPresent) {
				operation.addParametersItem(new Parameter()
						.$ref("#/components/parameters/" + ACCEPT_LANGUAGE_PARAM));
			}
			return operation;
		};
	}

	private static Parameter acceptLanguageParameter() {
		return new Parameter()
				.in("header")
				.name(ACCEPT_LANGUAGE_PARAM)
				.required(false)
				.schema(new StringSchema()
						._enum(List.of("vi", "en"))
						._default("vi"));
	}
}
