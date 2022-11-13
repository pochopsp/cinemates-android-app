package it.unina.cinemates.retrofit;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
@Setter
@NoArgsConstructor
public class PaginatedResponse<T> {

    @SerializedName("data")
    private T data;

    @SerializedName("pageNumber")
    private Integer pageNumber;

    @SerializedName("pageSize")
    private Integer pageSize;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("totalRecords")
    private Integer totalRecords;


}
