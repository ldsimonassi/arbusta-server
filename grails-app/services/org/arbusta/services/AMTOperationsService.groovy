package org.arbusta.services

import org.arbusta.domain.QualificationType
import org.arbusta.domain.QualificationRequirement
import org.arbusta.domain.HitType
import grails.transaction.Transactional
import grails.validation.ValidationException

@Transactional
class AMTOperationsService {

    static scope = "singleton"

    def CreateHIT(request) {
        //TODO Implement
    }


    def RegisterHITType(request) {
        def type = new HitType(
                title: request.Title,
                description: request.Description,
                reward: Double.parseDouble(request.Reward.Amount),
                assignmentDurationInSeconds: Long.parseLong(request.AssignmentDurationInSeconds),
                keywords: request.Keywords)

        def qr = request.QualificationRequirement
        def list;

        if (!(qr instanceof List)) {
            println "Is NOT a list"
            list = []
            list.add(qr)
        } else {
            println "Is a list"
            list = qr
        }

        println "List: [${list}]"

        list.each() { qReq ->
            println "Element: [${qReq}]"
            try {
                def qt = QualificationType.findById(Long.parseLong(qReq."QualificationTypeId"))
                def q = new QualificationRequirement(
                        qualificationType: qReq.QualificationTypeId,
                        comparator: qReq.Comparator,
                        integerValue: Integer.parseInt(qReq.IntegerValue))
                type.addToQualificationRequirements(q)
            } catch (e) {
                throw new Exception("Error while trying to create QualificationRequirement ["+qReq+"]", e)
            }
        }


        type.save()

        println "Saved ${type.id}"

        type.errors.each {
            println it
        }

        // TODO Build good response.
    }

    def CreateQualificationType(request) {
        def q = new QualificationType(
                name: request.Name, description: request.Description,
                keywords: request.Keywords, creationTime: new java.sql.Timestamp(System.currentTimeMillis()),
                retryDelayInSeconds: request.RetryDelayInSeconds,
                qualificationTypeStatus: request.QualificationTypeStatus,
                test: request.Test, answerKey: request.AnswerKey,
                testDurationInSeconds:request.TestDurationInSeconds)

        q.save()

        if(q.hasErrors()) throw new ValidationException(q.errors)

        def ret = [:]
        ret.QualificationType = [:]
        ret.QualificationType.QualificationTypeId = q.id
        ret.QualificationType.CreationTime = q.creationTime
        ret.QualificationType.Name = q.name
        ret.QualificationType.Description = q.description
        ret.QualificationType.QualificationTypeStatus = q.qualificationTypeStatus
        ret.QualificationType.AutoGranted = q.autoGranted
        return ret
    }
}
