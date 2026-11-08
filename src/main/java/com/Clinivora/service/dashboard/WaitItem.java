package com.Clinivora.service.dashboard;

import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.TriagePriority;

public record WaitItem(
        long caseId,
        String caseCode,
        String patientName,
        long waitMinutes,
        CaseStatus status,
        TriagePriority priority
) {
}
