package com.jiandong.legendaryai.agent.movie;

import java.time.LocalDate;
import java.util.List;

import com.jiandong.legendaryai.agent.movie.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveAgentTests {

	@Mock
	ChatClient.Builder chatClientBuilder;

	@Mock
	ChatClient chatClient;

	@Mock
	ChatClient.ChatClientRequestSpec requestSpec;

	@Mock
	ChatClient.CallResponseSpec responseSpec;

	MovieAgent movieAgent;

	@BeforeEach
	void setUp() {
		when(chatClientBuilder.build()).thenReturn(chatClient);
		movieAgent = new MovieAgent(chatClientBuilder);
	}

	@Test
	void getBasicInfo() {
		String movieRequest = "Find the sci-fi movie Inception released in 2010";
		Movie.BasicInfo expectedInfo = new Movie.BasicInfo("Inception", "Sci-Fi", LocalDate.of(2010, 1, 1));

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.entity(Movie.BasicInfo.class)).thenReturn(expectedInfo);

		Movie.BasicInfo actualInfo = movieAgent.getBasicInfo(movieRequest);

		assertThat(actualInfo).isNotNull();
		assertThat(actualInfo.name()).isEqualTo("Inception");
		assertThat(actualInfo.releaseDate()).isEqualTo(LocalDate.of(2010, 1, 1));
	}

	@Test
	void getActors() {
		Movie.BasicInfo basicInfo = new Movie.BasicInfo("Inception", "Sci-Fi", LocalDate.of(2010, 1, 1));
		Movie.Actors expectedActors = new Movie.Actors(List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt"));

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.entity(Movie.Actors.class)).thenReturn(expectedActors);

		Movie.Actors actualActors = movieAgent.getActors(basicInfo);

		assertThat(actualActors).isNotNull();
		assertThat(actualActors.actors().contains("Leonardo DiCaprio")).isTrue();
	}

	@Test
	void getIntroduction() {
		Movie.BasicInfo basicInfo = new Movie.BasicInfo("Inception", "Sci-Fi", LocalDate.of(2010, 1, 1));
		String expectedIntro = "A thief who steals corporate secrets through dream-sharing technology...";

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.entity(String.class)).thenReturn(expectedIntro);

		String actualIntro = movieAgent.getIntroduction(basicInfo);

		assertThat(actualIntro).isEqualTo(expectedIntro);
	}

}
