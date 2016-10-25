package com.vonex.dto;

public class HttpResponseDTO {

	private String text;
	private int status;

	public HttpResponseDTO() {
	}

	public HttpResponseDTO(String text, int status) {
		this.text = text;
		this.status = status;
	}

	public String getText() {

		return text;
	}

	public HttpResponseDTO setText(String text) {
		this.text = text;
		return this;
	}

	public int getStatus() {
		return status;
	}

	public HttpResponseDTO setStatus(int status) {
		this.status = status;
		return this;
	}
}
