package org.arbusta.domain

import java.sql.Timestamp

class QualificationRequest {
    Worker worker
    QualificationType qualificationType
    String test
    String answer
    Timestamp submitTime
    QualificationAssignment assignment
    String status = "Pending"
    String reason

    static constraints = {
        worker(nullable: false)
        qualificationType(nullable: false)
        test(nullable: true)
        answer(nullable: true)
        submitTime(nullable: false)
        assignment(nullable: true)
        status(nullable: false, blank: false, inList: ["Pending", "Rejected", "Granted"])
        reason(nullable: true)
    }

    static mapping = {
        reason(type: 'text')
    }
}
