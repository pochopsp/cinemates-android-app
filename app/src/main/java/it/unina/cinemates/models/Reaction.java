package it.unina.cinemates.models;

import com.google.gson.annotations.SerializedName;

import it.unina.cinemates.views.backend.BasicUser;
import it.unina.cinemates.views.backend.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Reaction {

    @SerializedName("id")
    private Integer id;

    @SerializedName("type")
    private ReactionType type;

    @SerializedName("authorId")
    private String authorId;

    @SerializedName("author")
    private BasicUser author;

    @SerializedName("reactedListId")
    private Integer reactedListId;

    public Reaction(ReactionType type, String authorId, Integer reactedListId){
        this.type = type;
        this.authorId = authorId;
        this.reactedListId = reactedListId;
    }

    public Reaction(Integer id, ReactionType type, String authorId, Integer reactedListId){
        this.id = id;
        this.type = type;
        this.authorId = authorId;
        this.reactedListId = reactedListId;
    }

}
