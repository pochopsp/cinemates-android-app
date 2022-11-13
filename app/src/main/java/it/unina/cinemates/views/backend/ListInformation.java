package it.unina.cinemates.views.backend;

import it.unina.cinemates.views.backend.enums.ListType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ListInformation {
    private String name;
    private ListType type;
    private String ownerId;
}
