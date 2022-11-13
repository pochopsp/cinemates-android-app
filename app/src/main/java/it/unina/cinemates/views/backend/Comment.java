package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Comment {

    @SerializedName("id")
    private Integer id;

    @SerializedName("body")
    private String body;

    @SerializedName("author")
    private BasicUser author;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("commentedListId")
    private Integer commentedListId;

}
