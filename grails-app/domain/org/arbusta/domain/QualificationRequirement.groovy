package org.arbusta.domain

class QualificationRequirement {
    static belongsTo = [ hitType : HitType ]
    String comparator
    QualificationType qualificationType
    Integer integerValue
    String localeValue

    static constraints = {
        qualificationType(nullable:false, blank:false)
        comparator(nullable:false, blank:false, inList: ["LessThan", "LessThanOrEqualTo",
                                                         "GreaterThan", "GreaterThanOrEqualTo",
                                                         "EqualTo", "NotEqualTo", "Exists"])
        integerValue(nullable:true)
        localeValue(nullable:true)
    }
}
