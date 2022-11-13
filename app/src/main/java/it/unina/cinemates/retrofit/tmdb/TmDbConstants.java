package it.unina.cinemates.retrofit.tmdb;

import java.util.HashMap;
import java.util.Map;

import it.unina.cinemates.BuildConfig;
import it.unina.cinemates.views.tmdb.enums.MovieStatus;

public class TmDbConstants {

    // api configuration data
    public static final String BASE_URL = "https://api.themoviedb.org/";
    public static final String API_KEY = BuildConfig.TMDB_API_KEY;

    public static class Images{

        private static final String baseImageUrl = "https://image.tmdb.org/t/p/";

        private static final String SMALL_BACKDROP_SIZE = "w300";
        private static final String MEDIUM_BACKDROP_SIZE = "w780";
        private static final String LARGE_BACKDROP_SIZE = "w1280";

        private static final String SMALL_POSTER_SIZE = "w185";
        private static final String MEDIUM_POSTER_SIZE = "w342";
        private static final String LARGE_POSTER_SIZE = "w780";

        private static final String SMALL_PROFILE_SIZE = "w45";
        private static final String MEDIUM_PROFILE_SIZE = "w185";
        private static final String BIG_PROFILE_SIZE = "h632";


        public static String mediumProfilePath(String relativePath){
            return baseImageUrl + MEDIUM_PROFILE_SIZE + relativePath;
        }

        public static String smallPosterPath(String relativePath){
            return baseImageUrl + SMALL_POSTER_SIZE + relativePath;
        }

        public static String mediumPosterPath(String relativePath){
            return baseImageUrl + MEDIUM_POSTER_SIZE + relativePath;
        }

        public static String largePosterPath(String relativePath){
            return baseImageUrl + LARGE_POSTER_SIZE + relativePath;
        }

        public static String smallBackdropPath(String relativePath){
            return baseImageUrl + SMALL_BACKDROP_SIZE + relativePath;
        }

        public static String mediumBackdropPath(String relativePath){
            return baseImageUrl + MEDIUM_BACKDROP_SIZE + relativePath;
        }

        public static String largeBackdropPath(String relativePath){
            return baseImageUrl + LARGE_BACKDROP_SIZE + relativePath;
        }
    }

    public static final String defaultAdultContentFilter = "false";

    // TMDb keywords
    public static final String RESULTS = "results";
    public static final String TOTAL_PAGES = "total_pages";
    public static final String PAGE = "page";
    public static final String TOTAL_RESULTS = "total_results";
    // time windows
    public static final String DAY = "day";
    public static final String WEEK = "week";

    // media types
    public static final String ALL = "all";
    public static final String MOVIE = "movie";
    public static final String TV = "tv";
    public static final String PERSON = "person";

    //--------------------------------- retrofit versions
    public static final String API_VERSION = "3";
    public static final String INCLUDE_ADULT = "false";

    public static class English{

        public static final String SEARCH_ACTORS_URL = API_VERSION +  "/search/person?" +
                "api_key=" + API_KEY +
                "&include_adult=" + INCLUDE_ADULT +
                "&language=en";

        public static final String SEARCH_MOVIES_BY_ACTOR = API_VERSION +  "/discover/movie?" +
                "api_key=" + API_KEY +
                "&language=en" +
                "&include_adult=" + INCLUDE_ADULT;

        public static final String UPCOMING_URL = API_VERSION +  "/movie/upcoming?api_key=" + API_KEY +
                "&language=" + "en";
        public static final String NOW_PLAYING_URL = API_VERSION +  "/movie/now_playing?api_key=" + API_KEY +
                "&language=" + "en";
        public static final String POPULAR_URL = API_VERSION +  "/movie/popular?api_key=" + API_KEY +
                "&language=" + "en";
        public static final String TOP_RATED_URL = API_VERSION +  "/movie/top_rated?api_key=" + API_KEY +
                "&language=" + "en";

        public static final String SEARCH_MOVIES_URL = API_VERSION +  "/search/movie?" +
                "api_key=" + API_KEY +
                "&language=en" +
                "&include_adult=" + INCLUDE_ADULT;

        public static final String MOVIE_POSTERS_URL = API_VERSION +
                "/movie/{movieid}/images?" +
                "api_key=" + API_KEY +
                "&language=en";

        public static final String MOVIE_DETAILS_URL = API_VERSION +
                "/movie/{movieid}?" +
                "api_key=" + API_KEY +
                "&append_to_response=videos,credits" +
                "&language=en";

        public static final Map<Integer, String> idGenres = new HashMap<>();
        static {
            English.idGenres.put(28,"Action");
            English.idGenres.put(12,"Adventure");
            English.idGenres.put(16,"Animation");
            English.idGenres.put(35,"Comedy");
            English.idGenres.put(80,"Crime");
            English.idGenres.put(99,"Documentary");
            English.idGenres.put(18,"Drama");
            English.idGenres.put(10751,"Family");
            English.idGenres.put(14,"Fantasy");
            English.idGenres.put(36,"History");
            English.idGenres.put(27,"Horror");
            English.idGenres.put(10402,"Music");
            English.idGenres.put(9648,"Mystery");
            English.idGenres.put(10749,"Romance");
            English.idGenres.put(878,"Sci-Fi");
            English.idGenres.put(10770,"TV Movie");
            English.idGenres.put(53,"Thriller");
            English.idGenres.put(10752,"War");
            English.idGenres.put(37,"Western");
        }

        public static final Map<String, String> movieStatus = new HashMap<>();
        static {
            English.movieStatus.put("Planned","Planned");
            English.movieStatus.put("Released","Released");
            English.movieStatus.put("Post Production","Post production");
            English.movieStatus.put("Canceled","Canceled");
            English.movieStatus.put("Rumored","Rumored");
            English.movieStatus.put("In Production","In production");
        }
    }

    public static class Italian {

        public static final String SEARCH_ACTORS_URL = API_VERSION +  "/search/person?" +
                "api_key=" + API_KEY +
                "&include_adult=" + INCLUDE_ADULT +
                "&language=it";

        public static final String SEARCH_MOVIES_URL = API_VERSION +  "/search/movie?" +
                "api_key=" + API_KEY +
                "&language=it" +
                "&include_adult=" + INCLUDE_ADULT;
        public static final String SEARCH_MOVIES_BY_ACTOR = API_VERSION +  "/discover/movie?" +
                "api_key=" + API_KEY +
                "&language=it" +
                "&include_adult=" + INCLUDE_ADULT;

        public static final String MOVIE_POSTERS_URL = API_VERSION +
                "/movie/{movieid}/images?" +
                "api_key=" + API_KEY +
                "&language=it";

        public static final String MOVIE_DETAILS_URL = API_VERSION +
                "/movie/{movieid}?" +
                "api_key=" + API_KEY +
                "&append_to_response=videos,credits" +
                "&language=it";

        public static final String UPCOMING_URL = API_VERSION +  "/movie/upcoming?api_key=" + API_KEY +
                "&language=" + "it";
        public static final String NOW_PLAYING_URL = API_VERSION +  "/movie/now_playing?api_key=" + API_KEY +
                "&language=" + "it";
        public static final String POPULAR_URL = API_VERSION +  "/movie/popular?api_key=" + API_KEY +
                "&language=" + "it";
        public static final String TOP_RATED_URL = API_VERSION +  "/movie/top_rated?api_key=" + API_KEY +
                "&language=" + "it";

        public static final Map<Integer, String> idGenres = new HashMap<>();
        static {
            Italian.idGenres.put(28,"Azione");
            Italian.idGenres.put(12,"Avventura");
            Italian.idGenres.put(16,"Animazione");
            Italian.idGenres.put(35,"Comedy");
            Italian.idGenres.put(80,"Crime");
            Italian.idGenres.put(99,"Documentario");
            Italian.idGenres.put(18,"Dramma");
            Italian.idGenres.put(10751,"Family");
            Italian.idGenres.put(14,"Fantasy");
            Italian.idGenres.put(36,"Storico");
            Italian.idGenres.put(27,"Horror");
            Italian.idGenres.put(10402,"Musica");
            Italian.idGenres.put(9648,"Mistero");
            Italian.idGenres.put(10749,"Romantico");
            Italian.idGenres.put(878,"Sci-Fi");
            Italian.idGenres.put(10770,"TV Movie");
            Italian.idGenres.put(53,"Thriller");
            Italian.idGenres.put(10752,"Guerra");
            Italian.idGenres.put(37,"Western");
        }

        public static final Map<String, String> movieStatus = new HashMap<>();
        static {
            Italian.movieStatus.put("Planned","Pianificato");
            Italian.movieStatus.put("Released","Rilasciato");
            Italian.movieStatus.put("Post Production","Post produzione");
            Italian.movieStatus.put("Canceled","Cancellato");
            Italian.movieStatus.put("Rumored","Rumoreggiato");
            Italian.movieStatus.put("In Production","In produzione");
        }
    }

}
