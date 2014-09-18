package org.arbusta.domain

class QualificationAssignment {
    Worker worker
    QualificationType qualificationType
    Integer integerValue
    String sendNotification
    QualificationRequest request

    static constraints = {
        worker(nullable: false)
        qualificationType(nullable: false, unique: 'worker')
        integerValue(nullable: false)
        sendNotification(nullable: false)
        request(nullable: true)
    }

    static mapping = {
        worker index: 'worker_qualification_type_index'
        qualificationType index: 'worker_qualification_type_index'
        request index: 'request_qualification_type_index'
    }
}
