package com.nobass.server.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nobass.server.entities.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour l'accès à l'API TMDB (The Movie Database).
 * <p>
 * Permet la recherche, la récupération des films populaires et des détails de film via l'API distante.
 * </p>
 */
@Service
public class TmdbService {

    public static final String RELEASE_DATE = "release_date";
    public static final String GENRE_IDS = "genre_ids";
    public static final String POSTER_PATH = "poster_path";
    public static final String BACKDROP_PATH = "backdrop_path";
    public static final String GENRES = "genres";
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.token}")
    private String apiToken;

    private final String BASE_URL = "https://api.themoviedb.org/3";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final java.util.Map<Integer, String> TMDB_GENRE_MAP = java.util.Map.ofEntries(
        java.util.Map.entry(28, "Action"),
        java.util.Map.entry(12, "Aventure"),
        java.util.Map.entry(16, "Animation"),
        java.util.Map.entry(35, "Comédie"),
        java.util.Map.entry(80, "Crime"),
        java.util.Map.entry(99, "Documentaire"),
        java.util.Map.entry(18, "Drame"),
        java.util.Map.entry(10751, "Familial"),
        java.util.Map.entry(14, "Fantastique"),
        java.util.Map.entry(36, "Histoire"),
        java.util.Map.entry(27, "Horreur"),
        java.util.Map.entry(10402, "Musique"),
        java.util.Map.entry(9648, "Mystère"),
        java.util.Map.entry(10749, "Romance"),
        java.util.Map.entry(878, "Science-fiction"),
        java.util.Map.entry(10770, "Téléfilm"),
        java.util.Map.entry(53, "Thriller"),
        java.util.Map.entry(10752, "Guerre"),
        java.util.Map.entry(37, "Western")
    );

    /**
     * Recherche des films par titre (via l'API TMDB).
     * @param query Titre ou mot-clé à rechercher
     * @param page Numéro de page (défaut : 1)
     * @return Liste des films correspondants
     */
    public List<Movie> searchMovies(String query, int page) {
        String url = UriComponentsBuilder
            .fromUriString(BASE_URL + "/search/movie")
            .queryParam("query", query)
            .queryParam("page", page)
            .queryParam("language", "fr-FR")
            .build()
            .toUriString();
        return fetchMoviesList(url);
    }

    /**
     * Récupère les films populaires (via l'API TMDB).
     * @param page Numéro de page (défaut : 1)
     * @return Liste des films populaires
     */
    public List<Movie> getPopularMovies(int page) {
        String url = UriComponentsBuilder
            .fromUriString(BASE_URL + "/movie/popular")
            .queryParam("page", page)
            .queryParam("language", "fr-FR")
            .build()
            .toUriString();
        return fetchMoviesList(url);
    }

    /**
     * Récupère les détails d'un film par son identifiant TMDB.
     * @param tmdbId Identifiant TMDB du film
     * @return Le film trouvé ou null
     */
    public Movie getMovieDetails(Long tmdbId) {
        String url = UriComponentsBuilder
            .fromUriString(BASE_URL + "/movie/" + tmdbId)
            .queryParam("language", "fr-FR")
            .build()
            .toUriString();
        return fetchMovieDetails(url);
    }

    private List<Movie> fetchMoviesList(String url) {
        try {
            JsonNode root = fetchJson(url);
            List<Movie> movies = new ArrayList<>();
            JsonNode results = root.get("results");
            if (results != null && results.isArray()) {
                for (JsonNode movieNode : results) {
                    Movie movie = parseMovieFromJson(movieNode);
                    if (movie != null) {
                        movies.add(movie);
                    }
                }
            }
            return movies;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Movie fetchMovieDetails(String url) {
        try {
            JsonNode movieNode = fetchJson(url);
            return parseMovieFromJson(movieNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JsonNode fetchJson(String url) throws Exception {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return objectMapper.readTree(response.getBody());
    }

    private Movie parseMovieFromJson(JsonNode movieNode) {
        try {
            Long tmdbId = movieNode.get("id").asLong();
            String title = movieNode.get("title").asText();
            String overview = movieNode.has("overview") ? movieNode.get("overview").asText() : "";
            String posterPath = movieNode.has(POSTER_PATH) && !movieNode.get(POSTER_PATH).isNull()
                ? movieNode.get(POSTER_PATH).asText() : null;
            String backdropPath = movieNode.has(BACKDROP_PATH) && !movieNode.get(BACKDROP_PATH).isNull()
                ? movieNode.get(BACKDROP_PATH).asText() : null;
            String originalLanguage = movieNode.has("original_language") ? movieNode.get("original_language").asText() : "";
            Double popularity = movieNode.has("popularity") ? movieNode.get("popularity").asDouble() : 0.0;

            LocalDate releaseDate = null;
            if (movieNode.has(RELEASE_DATE) && !movieNode.get(RELEASE_DATE).isNull()) {
                String releaseDateStr = movieNode.get(RELEASE_DATE).asText();
                if (!releaseDateStr.isEmpty()) {
                    releaseDate = LocalDate.parse(releaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                }
            }

            Double voteAverage = movieNode.has("vote_average") ? movieNode.get("vote_average").asDouble() : 0.0;
            Integer voteCount = movieNode.has("vote_count") ? movieNode.get("vote_count").asInt() : 0;

            java.util.List<String> genres = new java.util.ArrayList<>();
            if (movieNode.has(GENRE_IDS) && movieNode.get(GENRE_IDS).isArray()) {
                for (JsonNode genreIdNode : movieNode.get(GENRE_IDS)) {
                    int genreId = genreIdNode.asInt();
                    String genreName = TMDB_GENRE_MAP.getOrDefault(genreId, "Autre");
                    if (!genres.contains(genreName)) genres.add(genreName);
                }
            } else if (movieNode.has(GENRES) && movieNode.get(GENRES).isArray()) {
                for (JsonNode genreObj : movieNode.get(GENRES)) {
                    String genreName = genreObj.has("name") ? genreObj.get("name").asText() : null;
                    if (genreName != null && !genres.contains(genreName)) genres.add(genreName);
                }
            }

            Movie movie = new Movie(tmdbId, title, overview, posterPath, releaseDate, voteAverage, voteCount,
                           backdropPath, originalLanguage, popularity);
            movie.setGenres(genres);
            return movie;
        } catch (Exception e) {
            return null;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
