package com.ssafy.ozz;

import com.ssafy.ozz.user.global.config.DemoAccountProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = ElasticsearchRepositoriesAutoConfiguration.class)
@ComponentScan(
        basePackages = "com.ssafy.ozz",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@EnableJpaAuditing
@EnableConfigurationProperties(DemoAccountProperties.class)
public class OzzApplication {

    public static void main(String[] args) {
        SpringApplication.run(OzzApplication.class, args);
    }
}
