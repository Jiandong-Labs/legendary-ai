package com.jiandong.legendaryai;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class LegendaryAiApplicationTests {

	@Test
	void loadContext() {
		assertThat(LegendaryAiApplication.class).hasAnnotation(SpringBootApplication.class);
	}

}
