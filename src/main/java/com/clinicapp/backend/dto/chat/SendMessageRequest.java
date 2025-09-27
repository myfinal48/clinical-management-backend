package com.clinicapp.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to send a chat message")
public class SendMessageRequest {

    @NotNull(message = "Recipient ID is required")
    @Schema(description = "ID of the message recipient", required = true)
    private Long recipientId;

    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 5000, message = "Message content must be between 1 and 5000 characters")
    @Schema(description = "Message content", required = true, maxLength = 5000)
    private String content;

    @Schema(description = "Message type", defaultValue = "TEXT")
    private String type;

    @Schema(description = "ID of the message being replied to")
    private Long replyToId;

    @Schema(description = "Additional metadata in JSON format")
    private String metadata;
}
