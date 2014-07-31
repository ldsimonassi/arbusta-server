package org.arbusta.domain

import java.sql.Timestamp

class QualificationType {
    /**
     * TODO Add:- QualificationType Masters - Reserved qualification IDs
     * TODO Add:- Locale QualificationType
     * TODO Add:- Adult Content
     */

    Timestamp creationTime
    String name
    String description
    String keywords
    String qualificationTypeStatus
    Integer retryDelayInSeconds
    String test
    Integer testDurationInSeconds
    String answerKey
    boolean autoGranted
    boolean isRequestable = true
    Integer autoGrantedValue = 1

    static hasMany = [ requirements : QualificationRequirement]

    static constraints = {
        creationTime(nullable: false)
        name(nullable: true, blank:true, size: 0..200)
        description(nullable: true, blank:true, size: 0..2000)
        keywords(nullable: true, blank:true, size: 0..1000)
        qualificationTypeStatus(nullable: false, blank: false, inList: ["Active", "Inactive"], size: 6..8)
        retryDelayInSeconds(nullable: true, range: 1..Integer.MAX_VALUE)
        test(nullable: true, size: 0..65535)
        testDurationInSeconds(nullable: true, range: 0..Integer.MAX_VALUE)
        answerKey(nullable: true, size: 0..65535)
        autoGranted(nullable: true)
        autoGrantedValue(nullable: true)
        isRequestable(nullable: false)
    }

    static mapping = {
        description(type: 'text')
        keywords(type: 'text')
        test(type:'text')
        answerKey(type:'text')
    }
}
