package com.electroshop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExchangeRatesClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "https://api.exchangerate-api.com/v4/latest/USD";


    public double convertirADivisa(double montoUSD, String divisa) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ExchangeRateResponse rates = objectMapper.readValue(response.body(), ExchangeRateResponse.class);
                return montoUSD * rates.getRateTo(divisa);
            }
        } catch (Exception e) {
            System.err.println("Error API: " + e.getMessage() + " – Usando tasa 1.0");
        }
        return montoUSD;  // Fallback
    }
}