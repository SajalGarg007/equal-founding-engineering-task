package com.task.founding.engineer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdResponseDTO {

    private UUID id;
    private List<UUID> ids;
    private Long count;

    public static IdResponseDTO of(UUID id) {
        return IdResponseDTO.builder().id(id).build();
    }

    public static IdResponseDTO of(List<UUID> ids) {
        return IdResponseDTO.builder().ids(ids).count((long) ids.size()).build();
    }
}

