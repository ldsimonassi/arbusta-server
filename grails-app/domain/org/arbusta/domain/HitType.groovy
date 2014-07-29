package org.arbusta.domain

class HitType {

  String hitTypeId
  String title
  String description
  String keywords
  Double reward
  Long assignmenDurationInSeconds
  Long autoApprovalDelayInSeconds

  static constraints = {
    hitTypeId(blank:false)
    title(blank:false)
    description(blank:false)
    keywords(blank:true)
  }
}
