package io.github.senjar.bookstoreapp.repository.book;

public record BookSearchParameters(String[] title, String[] author, String[] isbn) {
}
