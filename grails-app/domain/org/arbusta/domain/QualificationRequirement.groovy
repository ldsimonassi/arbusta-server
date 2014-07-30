package org.arbusta.domain

class QualificationRequirement {
    static belongsTo = [ hitType : HitType ]
    String comparator
    Qualification qualification
    Integer integerValue
    String localeValue

    static constraints = {
        qualification(nullable:false, blank:false)
        comparator(nullable:false, blank:false, inList: ["LessThan", "LessThanOrEqualTo",
                                                         "GreaterThan", "GreaterThanOrEqualTo",
                                                         "EqualTo", "NotEqualTo", "Exists"])
        integerValue(nullable:true)
        localeValue(nullable:true)
    }
}
