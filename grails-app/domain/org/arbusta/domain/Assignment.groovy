package org.arbusta.domain

import java.sql.Timestamp

class Assignment {
	Worker worker
	Hit hit
	String status
	Timestamp autoApprovalTime
	Timestamp acceptTime
	Timestamp submitTime
	Timestamp approvalTime
	Timestamp rejectionTime
	Timestamp deadLine
	String answer
	String requesterFeedback
	
    static constraints = {
		status(inList: ["Submitted", "Approved", "Rejected"])
		autoApprovalTime(nullable: true)
		acceptTime(nullable: true)
		submitTime(nullable: false)
		approvalTime(nullable: true)
		rejectionTime(nullable: true)
		deadLine(nullable: true)
		answer(nullable: true)
		requesterFeedback(nullable: true)
    }
	
	static mapping = {
		answer(type:'text')
	}
}
