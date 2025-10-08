package com.electroshop.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)  // Ignora campos extras como "provider", "base"
public class ExchangeRateResponse {
    @JsonProperty("rates")
    private Map<String, Double> rates;

    // Otros campos opcionales (para completitud, pero no necesarios)
    @JsonProperty("base")
    private String base;
    @JsonProperty("date")
    private String date;

    public Map<String, Double> getRates() { return rates; }
    public void setRates(Map<String, Double> rates) { this.rates = rates; }

    public double getRateTo(String currency) {
        return rates != null ? rates.getOrDefault(currency, 1.0) : 1.0;
    }

    // Getters extras si quieres logs
    public String getBase() { return base; }
    public String getDate() { return date; }
}