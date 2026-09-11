package com.jiandong.legendaryai.agent.movie;

import java.time.LocalDate;
import java.util.List;

import com.jiandong.legendaryai.agent.movie.model.Movie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableIntegration
@DirtiesContext
@SpringBootTest(classes = {MovieAgentFlow.class})
class MovieAgentFlowTests {

	@Autowired
	@Qualifier("movieFlow.input")
	MessageChannel inputChannel;

	@MockitoBean
	MovieAgent movieAgent;

	@Test
	void happyFlow() {
		var requestMessage = MessageBuilder.withPayload("Avatar").build();

		when(movieAgent.getBasicInfo(any())).thenReturn(new Movie.BasicInfo("Avatar", "James Cameron", LocalDate.of(2009, 12, 18)));
		when(movieAgent.getActors(any())).thenReturn(new Movie.Actors(List.of("Sam Worthington", "Zoe Saldaña", " Sigourney Weaver")));
		when(movieAgent.getIntroduction(any())).thenReturn("A paraplegic marine takes a Na'vi avatar on Pandora. He initially helps human miners seize the alien land, but later joins the Na'vi to defend their home against human invasion.");

		inputChannel.send(requestMessage);

		verify(movieAgent).getBasicInfo(any());
		verify(movieAgent).getActors(any());
		verify(movieAgent).getIntroduction(any());
	}

}
