package it.unina.cinemates.views.backend;

import it.unina.cinemates.views.backend.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class NotifiedReport {

    private ReportedComment comment;
    private ListInformation listInformation;
    private ReportType type;
    private Boolean outcome;

}
