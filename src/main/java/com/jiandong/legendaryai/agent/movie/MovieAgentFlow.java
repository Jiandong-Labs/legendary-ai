package com.jiandong.legendaryai.agent.movie;

import java.util.function.Function;

import com.jiandong.legendaryai.agent.movie.model.Movie;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;

@Configuration
class MovieAgentFlow {

	private final MovieAgent movieAgent;

	public MovieAgentFlow(MovieAgent movieAgent) {
		this.movieAgent = movieAgent;
	}

	@Bean
	IntegrationFlow movieFlow() {
		return flow -> flow
				.transform(movieAgent::getBasicInfo)

				.transform(Movie.BasicInfo.class, basicInfo -> {
					Movie movie = new Movie();
					movie.basicInfo = basicInfo;
					return movie;
				})

				.transform(Movie.class, context -> {
					context.actors = movieAgent.getActors(context.basicInfo);
					return context;
				})

				.transform(Movie.class, context -> {
					context.introduction = movieAgent.getIntroduction(context.basicInfo);
					return context;
				})

				.log((Function<Message<Movie>, Object>) ctx -> "the movie detail is: " + ctx.getPayload())

				.nullChannel();
	}

}
