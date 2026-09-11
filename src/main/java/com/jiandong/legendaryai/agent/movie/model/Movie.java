package com.jiandong.legendaryai.agent.movie.model;

import java.time.LocalDate;
import java.util.List;

public class Movie {

	public BasicInfo basicInfo;

	public Actors actors;

	public String introduction;

	public record BasicInfo(String name, String director, LocalDate releaseDate) {

	}

	public record Actors(List<String> actors) {

	}

	@Override
	public String toString() {
		return "Movie{" +
				"basicInfo=" + basicInfo +
				", actors=" + actors +
				", introduction='" + introduction + '\'' +
				'}';
	}

}