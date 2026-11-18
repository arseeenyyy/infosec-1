package com.example.secureapi.dto;

public record TokenResponse(String token, String tokenType, long expiresInSeconds) {
}
