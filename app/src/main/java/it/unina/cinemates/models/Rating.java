package it.unina.cinemates.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Rating {

    @SerializedName("id")
    Integer id;

    @SerializedName("authorId")
    String authorId;

    @SerializedName("value")
    Integer value;

    @SerializedName("ratedListId")
    Integer ratedListId;
}
