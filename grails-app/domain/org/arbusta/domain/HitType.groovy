package org.arbusta.domain

class HitType {
  String title
  String description
  Double reward
  Integer assignmentDurationInSeconds
  String keywords
  Integer autoApprovalDelayInSeconds = 2592000 //30 Days

  static hasMany = [ qualificationRequirements : QualificationRequirement]

  static constraints = {
    title(blank:false, nullable:false, size: 3..128)
    description(blank:false, nullable: false, size: 5..2000)
    reward(blank: false, nullable: false)
    assignmentDurationInSeconds(blank: false, nullable: false, range: 30..3153600)
    keywords(blank: true, nullable: true, size: 0..1000)
    autoApprovalDelayInSeconds(blank: false, nullable: false,range: 0..2592000)
  }

    static mapping = {
        description(type: 'text')
    }
}
