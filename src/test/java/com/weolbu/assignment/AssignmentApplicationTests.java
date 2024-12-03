package com.weolbu.assignment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"JWT_SECRET_KEY=test_secret_key_test_secret_key_test_secret_key"})
class AssignmentApplicationTests {

    @Test
    void contextLoads() {
    }

}
