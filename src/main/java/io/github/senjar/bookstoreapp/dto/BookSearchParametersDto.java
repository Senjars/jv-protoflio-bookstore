package io.github.senjar.bookstoreapp.dto;

public record BookSearchParametersDto(String[] title, String[] author, String[] isbn) {
}
