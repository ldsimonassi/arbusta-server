package org.arbusta.domain

import java.sql.Timestamp

class QualificationRequest {
    Worker worker
    QualificationType qualificationType
    String test
    String answer
    Timestamp submitTime
    QualificationAssignment assignment

    static constraints = {
        worker(nullable: false)
        qualificationType(nullable: false)
        test(nullable: true)
        answer(nullable: true)
        submitTime(nullable: false)
        assignment(nullable: true)
    }
}
