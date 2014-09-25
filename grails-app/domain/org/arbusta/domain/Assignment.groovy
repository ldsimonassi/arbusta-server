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
    }
	
	static mapping = {
		answer(type:'text')
	}
}
