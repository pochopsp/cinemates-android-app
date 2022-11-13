package it.unina.cinemates.views.backend;

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
public class DiscoverListsElement {

    private BasicUser owner;
    private MovieListPreview list1;
    private MovieListPreview list2;
}
