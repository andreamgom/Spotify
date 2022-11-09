package es.ulpgc.spotify.webservice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.ulpgc.spotify.model.Album;
import es.ulpgc.spotify.model.Artist;
import es.ulpgc.spotify.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Json implements MusicSource {

    static SpotifyAccessor accessor;

    static {
        try {
            accessor = new SpotifyAccessor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonArray getAlbumId(String id) throws Exception {
        String response = accessor.get("/artists/" + id + "/albums?include_groups=album", Map.of());
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
        JsonArray items = jsonObject.get("items").getAsJsonArray();
        JsonArray albumsIds = new JsonArray();
        for (JsonElement item : items) {
            String albumIds = item.getAsJsonObject().get("id").getAsString();
            albumsIds.add(albumIds);
        }
        return albumsIds;
    }

    @Override
    public List<Album> albumsOf (String id) throws Exception {
        List<Album> albums = new ArrayList<>();
        String response = accessor.get("/artists/" + id + "/albums?include_groups=album", Map.of());
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
        JsonArray items = jsonObject.get("items").getAsJsonArray();
        for (JsonElement item : items) {
            String title = item.getAsJsonObject().get("name").getAsString();
            JsonArray artists = item.getAsJsonObject().get("artists").getAsJsonArray();
            JsonArray author = new JsonArray();
            for (JsonElement artist : artists) {
                String nameArtist = artist.getAsJsonObject().get("name").getAsString();
                author.add(nameArtist);
            }
            String releaseDate = item.getAsJsonObject().get("release_date").getAsString();
            int totalTracks = item.getAsJsonObject().get("total_tracks").getAsInt();
            albums.add(new Album(title, author.toString(), releaseDate, totalTracks));
        }
        return albums;

    }

    @Override
    public List<Artist> artistsOf (String id) throws Exception {
        List<Artist> artists = new ArrayList<>();
        String response = accessor.get("/artists/" + id, Map.of());
        System.out.println(response);
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
        String name = jsonObject.get("name").getAsString();
        int popularity = jsonObject.get("popularity").getAsInt();
        JsonArray genres = jsonObject.get("genres").getAsJsonArray();
        int followers = jsonObject.get("followers").getAsJsonObject().get("total").getAsInt();
        artists.add(new Artist(id, name, popularity, genres.toString(), followers));
        return artists;
    }

    @Override
    public List<Track> tracksOf(String id) throws Exception {
        List<Track> tracks = new ArrayList<>();
        JsonArray albumsIds = getAlbumId(id);
        for (JsonElement JsonElementalbumsId : albumsIds) {
            String albumsId = JsonElementalbumsId.getAsString();
            String response = accessor.get("/albums/" + albumsId + "/tracks", Map.of());
            JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
            JsonArray items = jsonObject.get("items").getAsJsonArray();
            for (JsonElement item : items) {
                String title = item.getAsJsonObject().get("name").getAsString();
                JsonArray artists = item.getAsJsonObject().get("artists").getAsJsonArray();
                JsonArray artistsNames = new JsonArray();
                for (JsonElement artist : artists) {
                    String author = artist.getAsJsonObject().get("name").getAsString();
                    artistsNames.add(author);
                }
                int duration = item.getAsJsonObject().get("duration_ms").getAsInt();
                boolean explicit = item.getAsJsonObject().get("explicit").getAsBoolean();
                tracks.add(new Track(title, artistsNames.toString(), duration, explicit));
            }
        }
        return tracks;
    }
}