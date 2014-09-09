package org.arbusta.services

import org.arbusta.domain.Hit
import org.arbusta.domain.QualificationType
import org.arbusta.domain.QualificationRequirement
import org.arbusta.domain.HitType
import grails.transaction.Transactional
import grails.validation.ValidationException

import java.sql.Timestamp

@Transactional
class AMTOperationsService {

    static scope = "singleton"

    def CreateHIT(request) {
        def response

        // Detect when HitTypeID is missing and create one
        if(request.HITTypeId == null) {
            println "No hit type registered, registering..."
            response = RegisterHITType(request)
            request.HITTypeId = response.RegisterHITTypeResult.HITTypeId
        }

        if(request.Question == null){
            throw new Exception("Either a Question parameter or a HITLayoutId parameter must be provided.")
        } else {
            def hit = new Hit(hitType: request.HITTypeId,
                    question: request.Question,
                    requesterAnnotation: request.RequesterAnnotation,
                    lifetimeInSeconds: Long.parseLong(request.LifetimeInSeconds),
                    maxAssignments: Integer.parseInt(request.MaxAssignments),
                    creationTime: new java.sql.Timestamp(System.currentTimeMillis())
            );

            if(!hit.save()) {
                hit.errors.each { it ->
                    println "-------------------"
                    println it
                }
                throw new Exception("Unable to save Hit ${hit}")
            }

            response = [:]

            response.HITId = hit.id
            response.HITTypeId = hit.hitType.id
            response.CreationTime = hit.creationTime
            response.Title = hit.hitType.title
            response.Description = hit.hitType.description
            response.Keywords = hit.hitType.keywords
            response.HITStatus = hit.hitStatus
            response.Question = hit.question
            response.RequesterAnnotation = hit.requesterAnnotation
            response.HITReviewStatus = hit.reviewStatus

            // HitType related Information
            response.Reward = [:]
            response.Reward.Amount = hit.hitType.reward
            response.Reward.CurrencyCode
            response.Reward.FormattedPrice
            response.LifetimeInSeconds = hit.lifetimeInSeconds
            response.AssignmentDurationInSeconds = hit.hitType.assignmentDurationInSeconds
            response.MaxAssignments = hit.maxAssignments
            response.AutoApprovalDelayInSeconds = hit.hitType.autoApprovalDelayInSeconds
            //response.QualificationRequirement

            return response
        }
    }

    def RegisterHITType(request) {
        def type = new HitType(
                title: request.Title,
                description: request.Description,
                reward: Double.parseDouble(request.Reward.Amount),
                assignmentDurationInSeconds: Long.parseLong(request.AssignmentDurationInSeconds),
                autoApprovalDelayInSeconds : Long.parseLong(request.AutoApprovalDelayInSeconds),
                keywords: request.Keywords)

        def qr = request.QualificationRequirement
        def list;

        if (qr != null) {
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
                    throw new Exception("Error while trying to create QualificationRequirement [" + qReq + "]", e)
                }
            }
        } else {
            println "QR is null"
        }

        if(!type.save()) {
            type.errors.each { it ->
                println "----------"
                println it
            }
            throw new Exception("Impossible to RegisterHITType ${request}")
        }


        def response = [:]
        response.RegisterHITTypeResult = [:]
        response.RegisterHITTypeResult.Request = [:]
        response.RegisterHITTypeResult.Request.IsValid = "True"
        response.RegisterHITTypeResult.HITTypeId = "" + type.id
        return response
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
