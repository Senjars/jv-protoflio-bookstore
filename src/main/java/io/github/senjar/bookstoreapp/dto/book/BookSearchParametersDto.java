package io.github.senjar.bookstoreapp.dto.book;

public record BookSearchParametersDto(String[] title, String[] author, String[] isbn) {
}
