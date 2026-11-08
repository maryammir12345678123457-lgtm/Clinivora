package com.Clinivora.service.dashboard;

import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.TriagePriority;

public record PipelineRow(
        long caseId,
        String caseCode,
        String patientName,
        CaseStatus status,
        TriagePriority priority,
        long waitMinutes,
        String chiefComplaint
) {
}
