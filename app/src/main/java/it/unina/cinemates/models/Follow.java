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
public class Follow {

    @SerializedName("followerId")
    private String followerId;

    @SerializedName("followingId")
    private String followingId;

    @SerializedName("accepted")
    private Boolean accepted;


    public static String getCompositeKeyFromNotification(Notification notification) {
        return notification.getNotifierId() + "-" + notification.getNotifiedId();
    }

    public String getCompositeKey() {
        return followerId + "-" + followingId;
    }
}
