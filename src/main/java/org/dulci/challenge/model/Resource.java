package org.dulci.challenge.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Resource implements Serializable {
    private static final long serialVersionUID = 5580555692741196520L;
    private String uuid;
    private String text;
}
