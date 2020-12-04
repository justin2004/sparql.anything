package com.github.spiceh2020.sparql.anything.model;

public enum IRIArgument {

	LOCATION("location"), MEDIA_TYPE("media-type"), NAMESPACE("namespace"), ROOT("root"), BLANK_NODES("blank-nodes"), TRIPLIFIER("triplifier");

	private String s;

	IRIArgument(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}

}
