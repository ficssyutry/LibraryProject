package com.example.library.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;

public class Id39586BookUpdateRequest {

    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @Size(max = 100, message = "Author name must be less than 100 characters")
    private String author;

    @Size(max = 50, message = "Genre must be less than 50 characters")
    private String genre;

    @Pattern(regexp = "^(?:\\d{13}|\\d{10})$", message = "ISBN must be 10 or 13 digits")
    private String isbn;

    @Min(value = 1800, message = "Publication year must be after 1800")
    @Max(value = 2026, message = "Publication year cannot be in the future")
    private Integer publicationYear;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Boolean available;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}