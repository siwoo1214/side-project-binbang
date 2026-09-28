package com.binbang.backend;

import com.binbang.backend.global.service.EmailConsumer;
import com.binbang.backend.global.service.NotificationConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @MockBean
    EmailConsumer emailConsumer;

    @MockBean
    NotificationConsumer notificationConsumer;

	@Test
	void contextLoads() {
	}

}
