package it.unina.cinemates.models;

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
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Notification {

    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String notificationType;

    @SerializedName("timestamp")
    private String time;

    @SerializedName("contentId")
    private Integer contentId;

    @SerializedName("read")
    private Boolean read;

    @SerializedName("notifierId")
    private String notifierId;

    @SerializedName("notifiedId")
    private String notifiedId;

    @SerializedName("notifierPhotoId")
    private Integer notifierPhotoId;

    private Boolean followRequestResult;

}
