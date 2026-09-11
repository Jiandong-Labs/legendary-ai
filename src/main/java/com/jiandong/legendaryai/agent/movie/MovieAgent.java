package com.jiandong.legendaryai.agent.movie;

import com.jiandong.legendaryai.agent.movie.model.Movie;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
class MovieAgent {

	private final ChatClient chatClient;

	MovieAgent(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	Movie.BasicInfo getBasicInfo(String movieRequest) {
		return this.chatClient.prompt()
				.user("Get Movie basic info from the given request: %s".formatted(movieRequest))
				.call()
				.entity(Movie.BasicInfo.class);
	}

	Movie.Actors getActors(Movie.BasicInfo movieBasicInfo) {
		return this.chatClient.prompt()
				.user("Get Movie actors from the given movie name:%s".formatted(movieBasicInfo.name()))
				.call()
				.entity(Movie.Actors.class);
	}

	String getIntroduction(Movie.BasicInfo movieBasicInfo) {
		return this.chatClient.prompt()
				.user("Get Movie introduction (less than 100 words) from the given movie name:%s".formatted(movieBasicInfo.name()))
				.call()
				.entity(String.class);
	}

}
