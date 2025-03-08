package dev.crab.dto;

import java.util.List;

public record BoardReportDTO(
        Long boardId,
        List<CardTimeReportDTO> timeReport,
        List<BlockReportDTO> blockReport
) {}