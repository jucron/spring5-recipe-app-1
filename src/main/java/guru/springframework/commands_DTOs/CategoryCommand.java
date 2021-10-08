package guru.springframework.commands_DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class CategoryCommand {
    private Long id;
    private String description;
}

/*
 NOTE: DTO or Data Transfer Objects are objects sent to the Front-End with only essential data.
 This avoids that sensitive data becomes exposed to the UI (user interface) layer.
 */