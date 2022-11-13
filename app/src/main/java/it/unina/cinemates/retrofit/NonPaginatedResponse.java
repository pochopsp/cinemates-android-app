package it.unina.cinemates.retrofit;

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
@NoArgsConstructor
public class NonPaginatedResponse<T> {

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

}
