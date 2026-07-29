package com.ssafy.ozz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:ozz;MODE=MySQL;NON_KEYWORDS=OFFSET;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.data.elasticsearch.repositories.enabled=false",
                "demo.account.email=test@ozz.local"
        }
)
class OzzApplicationTests {

    @Test
    void contextLoads() {
    }
}
