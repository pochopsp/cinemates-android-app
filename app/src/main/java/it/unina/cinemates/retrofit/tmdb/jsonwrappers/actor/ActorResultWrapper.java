package it.unina.cinemates.retrofit.tmdb.jsonwrappers.actor;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ActorResultWrapper {

    @SerializedName("results")
    private List<Actor> actorResult;

    @Getter
    @Setter
    public static class Actor{
        @SerializedName("id")
        private int id;
    }

}
