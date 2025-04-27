package com.Dev.Pal.Services;

import org.springframework.stereotype.Service;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.util.*;

@Service
public class YouTubeService {
  /*  @Value("${youtube.api.key}")
    private String apiKey;

    private final YouTube youtube;

    public YouTubeService() {
        this.youtube = new YouTube.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.jackson2.JacksonFactory(),
                request -> {}).setApplicationName("youtube-api").build();
    }

    public List<Map<String, Object>> searchYouTube(String query, int limit) throws IOException {
        List<Map<String, Object>> finalResults = new ArrayList<>();
        Set<String> playlistIds = new HashSet<>();

        int maxPlaylists = Math.min(limit, 50); // Ensures limit does not exceed 50

        // Fetch Arabic and English playlists
        for (String lang : Arrays.asList("en", "ar")) {
            YouTube.Search.List searchRequest = youtube.search().list("snippet");
            searchRequest.setKey(apiKey);
            searchRequest.setQ(query);
            searchRequest.setType("playlist");
            searchRequest.setMaxResults(50L); // Fetch more, filter later
            searchRequest.setRelevanceLanguage(lang);

            SearchListResponse response = searchRequest.execute();
            List<SearchResult> results = response.getItems();

            for (SearchResult result : results) {
                if (result.getId().getKind().equals("youtube#playlist")) {
                    playlistIds.add(result.getId().getPlaylistId());
                }
                if (playlistIds.size() >= maxPlaylists) break; // Stop if we hit the limit
            }
            if (playlistIds.size() >= maxPlaylists) break;
        }

        // Fetch playlist details
        finalResults.addAll(getPlaylists(new ArrayList<>(playlistIds)));

        return finalResults;
    }

    private List<Map<String, Object>> getPlaylists(List<String> playlistIds) throws IOException {
        List<Map<String, Object>> playlists = new ArrayList<>();

        if (playlistIds.isEmpty()) return playlists;

        YouTube.Playlists.List playlistRequest = youtube.playlists().list("snippet,contentDetails");
        playlistRequest.setKey(apiKey);
        playlistRequest.setId(String.join(",", playlistIds));

        PlaylistListResponse playlistResponse = playlistRequest.execute();
        List<Playlist> playlistItems = playlistResponse.getItems();

        for (Playlist playlist : playlistItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("title", playlist.getSnippet().getTitle());
            item.put("url", "https://www.youtube.com/playlist?list=" + playlist.getId());
            item.put("VideosNumber", playlist.getContentDetails().getItemCount());
            item.put("type", playlist.getKind());
            playlists.add(item);
        }

        return playlists;
    }
*/

}
