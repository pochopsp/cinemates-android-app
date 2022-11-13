package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ReportedComment {

    @SerializedName("body")
    private String body;

    @SerializedName("author")
    private BasicUser commentAuthor;

    @SerializedName("timestamp")
    private String timestamp;

}
