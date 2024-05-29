package org.app.dtos;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Integer id;
    private String text;
    private Date date;
    private String userEmail;
}
