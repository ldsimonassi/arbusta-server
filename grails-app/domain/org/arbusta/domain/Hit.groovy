package org.arbusta.domain

import java.sql.Timestamp

class Hit {

    /**
     * TODO: Add constraints to this entity
     */

    HitType hitType
    String question
    Timestamp creationTime
    String hitStatus = "Assignable"
    String requesterAnnotation
    String reviewStatus =  "NotReviewed"
    Long lifetimeInSeconds = null
    Integer maxAssignments = 1

    //String hitLayoutId
    static constraints = {
        hitType(nullable: false)
        requesterAnnotation(nullable: true)
        question(nullable: true, size: 0..65535)
    }

    static mapping = {
        question(type:'text')
    }
}
