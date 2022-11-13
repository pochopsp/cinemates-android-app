package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import it.unina.cinemates.views.backend.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LoggedUserReaction {

    @SerializedName("reactionId")
    private int reactionId;

    @SerializedName("type")
    private ReactionType type;
}