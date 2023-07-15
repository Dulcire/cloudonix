package org.dulci.challenge.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response implements Serializable {
    private static final long serialVersionUID = 8902938902362756778L;
    private String value;
    private String lexical;
}
