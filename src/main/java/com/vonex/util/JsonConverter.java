package com.vonex.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;


@Component
public class JsonConverter {

	@Autowired
	ObjectMapper objectMapper;

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public Optional<byte[]> serializeAsBytes(Object object) {
		Optional<byte[]> optional = Optional.empty();
		if (Objects.nonNull(object))
			try {
				optional = Optional.of(objectMapper.writeValueAsBytes(object));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		return optional;
	}

	public Optional<String> serialize(Object object) {
		Optional<String> optional = Optional.empty();
		if (Objects.nonNull(object)) {
			try {
				optional = Optional.of(objectMapper.writeValueAsString(object));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return optional;
	}

	public <T> Optional<T> deserializeFromBytes(Class<T> type, byte[] bytes) {
		Optional<T> optional = Optional.empty();
		if (Objects.nonNull(bytes)) {
			try {
				optional = Optional.of(objectMapper.readValue(bytes, type));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return optional;
	}

	public <T> Optional<T> deserialize(Class<T> type, String json) {
		Optional<T> optional = Optional.empty();
		if (Objects.nonNull(json)) {
			try {
				optional = Optional.of(objectMapper.readValue(json, type));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return optional;
	}
}
