package com.recruitment.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

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
				.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
				.components(new Components().addSecuritySchemes("Bearer Authentication",
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.bearerFormat("JWT")
								.scheme("bearer")));
	}
}
