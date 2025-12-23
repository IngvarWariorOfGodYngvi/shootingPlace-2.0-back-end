package com.shootingplace.shootingplace.email;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String htmlContent;

    private List<Attachment> attachments;
    private List<InlineImage> inlineImages;

    @Data
    public static class Attachment {
        private String filename;
        private String contentBase64;
        private String contentType;

    }

    @Data
    public static class InlineImage {
        private String contentId;
        private String contentBase64;
        private String contentType;

    }

}

