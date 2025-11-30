package com.chat.realtimechat;

import com.chat.realtimechat.controller.testController;
import jakarta.servlet.MultipartConfigElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RealtimeChatApplicationTests {

    @Test
    void contextLoads() {
        assertThat(2).isEqualTo(2);
    }
}
