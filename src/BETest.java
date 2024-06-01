import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.JSONArray;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class BETest {

    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";
    public static String[] cities = new String[]{"Tel Aviv,IL", "London", "New York"};
    public static Map<String, Double> tempInCities = new HashMap<>();

    public static void main(String[] args) {
        runBackendTest();
    }

    private static void runBackendTest() {
        try {
            for (String city : cities) {
                StringBuilder apiResponse = getAPIResponse(city);
                System.out.println("API response:\n" + apiResponse + "\n");
                if (apiResponse != null) {
                    JSONObject apiResponseJson = new JSONObject(new JSONTokener(apiResponse.toString()));
                    verifyCountry(getCountry(apiResponseJson), city);
                    String returnedCity = getCity(apiResponseJson);
                    getTemp(apiResponseJson, returnedCity);
                }
            }
            System.out.println("\nTemperatures in cities: \n");
            printCities();


        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetches API response for a given city.
     *
     * @param city The city name
     * @return The API response as a StringBuilder
     */
    private static StringBuilder getAPIResponse(String city) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("./config.properties"));

            String API_KEY = config.getProperty("api_key");
            String formattedCity = city.replace(" ", "%20");
            String units = formattedCity.equalsIgnoreCase("Tel%20Aviv,il") ? "metric" : "imperial";

            String apiURL = BASE_URL + "?q=" + formattedCity + "&units=" + units + "&APPID=" + API_KEY;

            URL url = createURL(apiURL);
            if (url == null) return null;

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = connection.getResponseCode();


            if (responseCode == HttpURLConnection.HTTP_OK) { // HTTP_OK = 200
                System.out.println("API response received successfully.");
                System.out.println("Response code: " + responseCode);
                return readResponse(connection);

            } else {
                System.err.println("GET request failed, error occurred: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a URL from the given string.
     *
     * @param apiURL The API URL string
     * @return The created URL
     */
    private static URL createURL(String apiURL) {
        try {
            URI uri = new URI(apiURL);
            return uri.toURL();
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the response from the HTTP connection.
     *
     * @param connection The HTTP connection
     * @return The response as a StringBuilder
     */
    private static StringBuilder readResponse(HttpURLConnection connection) {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response;
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts the country from the API response.
     *
     * @param apiResponse The API response JSON object
     * @return The country code
     */
    public static String getCountry(JSONObject apiResponse) {
        return apiResponse.getJSONObject("sys").getString("country");
    }

    /**
     * Verifies the country code for Tel Aviv.
     *
     * @param country The country code
     * @param city    The city name
     */
    private static void verifyCountry(String country, String city) {
        if (city.equals("Tel Aviv")) {
            if (!country.equals("IL")) {
                System.out.println("Invalid country for Tel Aviv");
            } else {
                System.out.println("Valid country " + country + " for Tel Aviv");
            }
        }
    }

    /**
     * Extracts the city name from the API response JSON.
     *
     * @param apiResponse The API response JSON object
     * @return The city name
     */
    private static String getCity(JSONObject apiResponse) {
        String city = apiResponse.getString("name");
        System.out.println("City: " + city);
        return city;
    }

    /**
     * Extracts the temperature from the API response JSON and stores it in the map.
     *
     * @param apiResponse The API response JSON object
     * @param city        The city name
     */
    public static void getTemp(JSONObject apiResponse, String city) {
        double temp = apiResponse.getJSONObject("main").getDouble("temp");
        tempInCities.put(city, temp);
    }

    /**
     * Prints the stored temperatures for each city.
     */
    private static void printCities() {
        for (Map.Entry<String, Double> entry : tempInCities.entrySet()) {
            String unit = entry.getKey().equalsIgnoreCase("Tel Aviv") ? "C" : "F";
            System.out.println(entry.getKey() + ", Temp: " + entry.getValue() + unit);
        }
        System.out.println("\n");
    }
}

