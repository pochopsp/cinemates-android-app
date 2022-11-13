package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Rating {

    @SerializedName("id")
    Integer id;

    @SerializedName("author")
    BasicUser author;

    @SerializedName("value")
    Integer value;

    @SerializedName("ratedListId")
    Integer ratedListId;

}
