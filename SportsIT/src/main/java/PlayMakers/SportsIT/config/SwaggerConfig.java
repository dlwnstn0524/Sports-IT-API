package PlayMakers.SportsIT.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Array;
import java.util.Arrays;

@OpenAPIDefinition(
        info = @Info(title = "스포츠잇 API 명세서",
                description = "API 명세 및 데이터 스키마를 확인하고 API 테스트 호출 가능합니다.",
                version = "0.0.1"),
        servers = {
                @Server(url = "https://localhost:8443", description = "Local server"),
                @Server(url = "https://sports-it-test.store", description = "Test server")
        })
@Configuration
public class SwaggerConfig {
    String basePath = "/api";

    public String[] addPrefixToMany(String prefix, String[] stringSet) {
        return Arrays.stream(stringSet).map(s -> prefix + s).toArray(String[]::new);
    }

    @Bean
    public GroupedOpenApi competitionOpenApi() {
        String[] paths = {"api/competitions/**"};

        return GroupedOpenApi.builder()
                .group("Competitions")
                .pathsToMatch(paths)
                .addOpenApiCustomizer(buildSecurityOpenApi())
                .build();
    }

    @Bean
    public GroupedOpenApi memberOpenApi() {
        String[] paths = {"api/member/**"};

        return GroupedOpenApi.builder()
                .group("Member")
                .pathsToMatch(paths)
                .build();
    }

    public OpenApiCustomizer buildSecurityOpenApi() {
        // 설정된 jwt 토큰을 Header에 넣어주는 코드
        return OpenApi -> OpenApi.addSecurityItem(new SecurityRequirement().addList("jwt token"))
                .getComponents().addSecuritySchemes("jwt token", new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .in(SecurityScheme.In.HEADER)
                        .bearerFormat("JWT")
                        .scheme("bearer"));
    }

}
