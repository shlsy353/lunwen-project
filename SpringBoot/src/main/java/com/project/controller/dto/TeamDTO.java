package com.project.controller.dto;

import com.project.entity.Team;
import lombok.Data;
import java.util.List;

@Data
public class TeamDTO {
    private Team team;
    private List<Long> memberIds;
}
