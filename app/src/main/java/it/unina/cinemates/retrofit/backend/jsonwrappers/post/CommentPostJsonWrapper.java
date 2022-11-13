package it.unina.cinemates.retrofit.backend.jsonwrappers.post;


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
public class CommentPostJsonWrapper {

    String body;
    String authorId;
    Integer commentedListId;
}
