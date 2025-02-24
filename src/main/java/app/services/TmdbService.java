package app.services;

import app.utils.ApiReader;
import app.utils.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TmdbService {

    public static void getDanishMoviesSince2020() {

        String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

        String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&language=en-US&page=1&release_date.gte=2020-01-01&sort_by=popularity.desc&api_key=" + ApiKey;

        String json = ApiReader.getDataFromUrl(url);

        System.out.println(json);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {

            ResponseDto responseDto = objectMapper.readValue(json, ResponseDto.class);
            for (Result r : responseDto.results) {
                System.out.println(r);
            }
//            Double temperature = weatherDto.currentData.temperature;
//            String skyText = weatherDto.currentData.skyText;
//            Double humidity = weatherDto.currentData.humidity;
//            String windText = weatherDto.currentData.windText;
//            return new WeatherInfo(temperature, skyText, humidity, windText);

        } catch (Exception e) {
            e.printStackTrace();
//            return null;
        }


    }


    private record ResponseDto(Result[] results) {
    }

    private record Result(String title,
                          @JsonProperty("original_title")
                          String originalTitle,
                          Boolean adult,
                          @JsonProperty("original_language")
                          String originalLanguage,
                          String overview) {
    }




}
