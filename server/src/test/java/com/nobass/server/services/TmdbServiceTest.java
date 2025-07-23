package com.nobass.server.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nobass.server.entities.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TmdbServiceTest {
    @InjectMocks
    TmdbService tmdbService;
    @Mock
    RestTemplate restTemplate;
    @Mock
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tmdbService, "apiToken", "fake-token");
        ReflectionTestUtils.setField(tmdbService, "apiKey", "fake-key");
        ReflectionTestUtils.setField(tmdbService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(tmdbService, "objectMapper", objectMapper);
    }

    @Test
    void searchMovies_returnsMovies_whenApiSuccess() throws Exception {
        String json = "{\"results\":[{\"id\":1,\"title\":\"Film\",\"overview\":\"desc\",\"poster_path\":null,\"release_date\":\"2020-01-01\",\"vote_average\":7.5,\"vote_count\":100,\"original_language\":\"fr\",\"popularity\":10.0,\"genre_ids\":[28]}]}";
        ResponseEntity<String> response = ResponseEntity.ok(json);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        JsonNode root = new ObjectMapper().readTree(json);
        when(objectMapper.readTree(json)).thenReturn(root);

        List<Movie> movies = tmdbService.searchMovies("test", 1);

        assertEquals(1, movies.size());
        assertEquals("Film", movies.get(0).getTitle());
        assertEquals(LocalDate.of(2020, 1, 1), movies.get(0).getReleaseDate());
    }

    @Test
    void searchMovies_returnsEmpty_whenApiFails() throws Exception {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("fail"));

        List<Movie> movies = tmdbService.searchMovies("test", 1);

        assertTrue(movies.isEmpty());
    }

    @Test
    void getPopularMovies_returnsMovies_whenApiSuccess() throws Exception {
        String json = "{\"results\":[{\"id\":2,\"title\":\"Populaire\",\"overview\":\"desc\",\"poster_path\":null,\"release_date\":\"2021-01-01\",\"vote_average\":8.0,\"vote_count\":200,\"original_language\":\"fr\",\"popularity\":20.0,\"genre_ids\":[12]}]}";
        ResponseEntity<String> response = ResponseEntity.ok(json);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        JsonNode root = new ObjectMapper().readTree(json);
        when(objectMapper.readTree(json)).thenReturn(root);

        List<Movie> movies = tmdbService.getPopularMovies(1);

        assertEquals(1, movies.size());
        assertEquals("Populaire", movies.get(0).getTitle());
    }

    @Test
    void getMovieDetails_returnsMovie_whenApiSuccess() throws Exception {
        String json = "{\"id\":3,\"title\":\"Détail\",\"overview\":\"desc\",\"poster_path\":null,\"release_date\":\"2022-01-01\",\"vote_average\":9.0,\"vote_count\":300,\"original_language\":\"fr\",\"popularity\":30.0,\"genres\":[{\"name\":\"Action\"}]}";
        ResponseEntity<String> response = ResponseEntity.ok(json);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        JsonNode node = new ObjectMapper().readTree(json);
        when(objectMapper.readTree(json)).thenReturn(node);

        Movie movie = tmdbService.getMovieDetails(3L);

        assertNotNull(movie);
        assertEquals("Détail", movie.getTitle());
    }

    @Test
    void getMovieDetails_returnsNull_whenApiFails() throws Exception {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("fail"));

        Movie movie = tmdbService.getMovieDetails(3L);

        assertNull(movie);
    }
}
