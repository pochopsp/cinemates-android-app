package it.unina.cinemates.models;

import com.google.gson.annotations.SerializedName;

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
public class Report {

    @SerializedName("id")
    private Integer id;

    @SerializedName("openingTimestamp")
    private String openingTimestamp;

    @SerializedName("closingTimestamp")
    private String closingTimestamp;

    @SerializedName("type")
    private String type;

    @SerializedName("outcome")
    private Boolean outcome;

    @SerializedName("commentId")
    private Integer commentId;

    @SerializedName("adminId")
    private Integer adminId;

    @SerializedName("authorId")
    private String authorId;


    public String jsonReportTypeFromViewReportType(String viewReportType){
        switch (viewReportType){
            case "Hate Speech":
            case "Incitamento all'odio":
                return "hateSpeech";
            case "Spam":
                return "spam";
            case "Offensive":
            case "Offensivo":
                return "offensive";
            case "Racist":
            case "Razzista":
                return "racist";
            case "Sexist":
            case "Sessista":
                return "sexist";
        }

        return "";
    }

}
