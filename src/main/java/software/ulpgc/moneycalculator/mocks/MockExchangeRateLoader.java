package software.ulpgc.moneycalculator.mocks;

import software.ulpgc.moneycalculator.Currency;
import software.ulpgc.moneycalculator.ExchangeRate;
import software.ulpgc.moneycalculator.ExchangeRateLoader;

import java.io.IOException;
import java.time.LocalDate;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.Currency;
import software.ulpgc.moneycalculator.CurrencyLoader;
import software.ulpgc.moneycalculator.fixerws.FixerAPI;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockExchangeRateLoader implements ExchangeRateLoader {


    @Override
    public ExchangeRate load(Currency from, Currency to) {
        String jsonResponse = null;
        try {
            jsonResponse = loadJsonFromApi(from, to);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        double rate = jsonObject.getAsJsonObject("info").get("rate").getAsDouble();
        LocalDate date = LocalDate.parse(jsonObject.get("date").getAsString());
        return new ExchangeRate(from, to, date, rate);
    }
    private String loadJsonFromApi(Currency from, Currency to) throws IOException {
        String fromCode = from.toCode();
        String toCode = to.toCode();

        String urlString = String.format("https://api.apilayer.com/exchangerates_data/convert?to=%s&from=%s&amount=1",
                toCode, fromCode);

        System.out.println("URL de la solicitud: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("apikey", FixerAPI.key);

        int responseCode = connection.getResponseCode();
        System.out.println("Código de respuesta: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error response from API: " + connection.getResponseMessage());
        }

        try (InputStream is = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            String jsonResponse = response.toString();
            System.out.println("Respuesta de la API: " + jsonResponse);
            return jsonResponse;
        } finally {
            connection.disconnect();
        }
    }

}