package it.unina.cinemates.views.tmdb;

import android.annotation.SuppressLint;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.CastMember;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.CrewMember;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.MovieDetails;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.MovieGenre;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class MovieFull {

    private final Integer id;
    private final String title;
    private final String tmDbRating;
    private final String runtime;
    private final String genres;
    private URL trailerUrl;
    private String releaseDate;
    private final String year;
    private final String movieStatus;
    private final String budget;
    private final String revenue;
    private final String description;
    private final List<CastMember> castMembers;
    private String directorName;


    public MovieFull(MovieDetails movieDetails, String deviceLocale) {
        id = movieDetails.getId();

        title = movieDetails.getTitle();

        tmDbRating = movieDetails.getTmDbRating() == 0.0 ? "n/a" : String.valueOf(movieDetails.getTmDbRating());

        if (movieDetails.getRuntime() != null)
            runtime = minutesToHoursMinutes(movieDetails.getRuntime());
        else
            runtime = "";


        Map<Integer, String> idGenreToTextGenre = deviceLocale.equals("it-IT") ? TmDbConstants.Italian.idGenres : TmDbConstants.English.idGenres;
        List<String> genreNames = new ArrayList<>();
        for (MovieGenre movieGenre : movieDetails.getGenres().stream().limit(3).collect(Collectors.toList()))
            genreNames.add(idGenreToTextGenre.get(movieGenre.getId()));

        String genresResult = genreNames.stream().collect(Collectors.joining(", "));

        genres = genresResult.equals("") ? "N/A" : genresResult;

        try {
            trailerUrl = new URL("https://youtube.com/watch?v=" + movieDetails.getTrailerUrls().getResults().get(0));
        } catch (MalformedURLException | IndexOutOfBoundsException e) {
            trailerUrl = null;
        }

        releaseDate = westernDate(movieDetails.getReleaseDate());

        String unavailable = "Unavailable";
        if (deviceLocale.equals("it-IT")) {
            unavailable = "Non disponibile";
        }

        if (!releaseDate.equals("N/A"))
            year = releaseDate.split("/")[2];
        else {
            releaseDate = unavailable;
            year = unavailable;
        }


        Map<String, String> movieStatusToTextStatus = deviceLocale.equals("it-IT") ? TmDbConstants.Italian.movieStatus : TmDbConstants.English.movieStatus;
        movieStatus = movieStatusToTextStatus.get(movieDetails.getMovieStatus());

        budget = formatMoney(movieDetails.getBudget(), unavailable);

        revenue = formatMoney(movieDetails.getRevenue(), unavailable);

        description = movieDetails.getDescription();

        castMembers = movieDetails.getCredits().getCast().stream().limit(6).collect(Collectors.toList());

        Optional<CrewMember> director = movieDetails.getCredits().getCrew().stream().filter(crewMember -> crewMember.getJob().equals("Director")).findFirst();
        directorName = unavailable;
        director.ifPresent(crewMember -> directorName = crewMember.getName());
    }

    private String minutesToHoursMinutes(int minutesDuration) {

        long hour = TimeUnit.MINUTES.toHours(minutesDuration);
        long minute = TimeUnit.MINUTES.toMinutes(minutesDuration) % 60;

        if (hour > 0 && minute > 0) {
            return hour + "h " + minute + "min";
        } else if (hour > 0) {
            return hour + "h ";
        } else if (minute > 0) {
            return minute + "min";
        } else {
            return "";
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String westernDate(String easternDate) {
        Date westernDate;
        try {
            westernDate = new SimpleDateFormat("yyyy-MM-dd").parse(easternDate);
        } catch (ParseException e) {
            return "N/A";
        }
        assert westernDate != null;
        return new SimpleDateFormat("dd/MM/yyyy").format(westernDate);
    }

    private String formatMoney(Long moneyValue, String unavailable) {
        if (moneyValue >= 1000000000)
            return "$ " + moneyValue.toString().charAt(0) + "," + moneyValue.toString().substring(1, 3) + " mrd";
        else if (moneyValue >= 100000000)
            return "$ " + moneyValue.toString().substring(0, 3) + " mln";
        else if (moneyValue >= 10000000)
            return "$ " + moneyValue.toString().substring(0, 2) + " mln";
        else if (moneyValue >= 1000000)
            return "$ " + moneyValue.toString().charAt(0) + " mln";
        else if (moneyValue >= 10000)
            return "$ " + moneyValue.toString().substring(0, 2) + " k";
        else {
            String money = "$ " + moneyValue.toString().substring(0, moneyValue.toString().length() - 1) + " k";
            if (money.equals("$  k"))
                return unavailable;
            return money;
        }
    }
}