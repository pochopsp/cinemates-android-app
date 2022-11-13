package it.unina.cinemates.views.backend;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import it.unina.cinemates.views.backend.enums.ListType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class MovieListBasic implements Parcelable {

    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private ListType type;

    @SerializedName("ownerId")
    private String ownerId;

    @SerializedName("averageRating")
    private Float averageRating;

    public MovieListBasic(MovieListFull movieListFull){
        this.id = movieListFull.getId();
        this.name = movieListFull.getName();
        this.type = movieListFull.getType();
        this.ownerId = movieListFull.getOwnerId();
        this.averageRating = movieListFull.getAverageRating();
    }

    // Parcelling part
    public MovieListBasic(Parcel in){
        this.id = in.readInt();
        this.name = in.readString();
        this.type = ListType.values()[in.readInt()];
        this.ownerId = in.readString();
        this.averageRating = in.readFloat();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.type.ordinal());
        dest.writeString(this.ownerId);
        float averageRating = this.averageRating != null ? this.averageRating : 0f;
        dest.writeFloat(averageRating);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MovieListBasic createFromParcel(Parcel in) {
            return new MovieListBasic(in);
        }

        public MovieListBasic[] newArray(int size) {
            return new MovieListBasic[size];
        }
    };
}
