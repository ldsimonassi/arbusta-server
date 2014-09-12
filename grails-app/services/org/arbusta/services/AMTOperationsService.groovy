package org.arbusta.services

import org.arbusta.domain.Hit
import org.arbusta.domain.QualificationAssignment
import org.arbusta.domain.QualificationRequest
import org.arbusta.domain.QualificationType
import org.arbusta.domain.QualificationRequirement
import org.arbusta.domain.HitType
import grails.transaction.Transactional
import grails.validation.ValidationException
import org.arbusta.domain.Worker

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

            if(!hit.save()) throw new ValidationException("Unable to save Hit ${hit}", hit.errors)

            response = [:]

            response.HITId = hit.id.toString()
            response.HITTypeId = hit.hitType.id.toString()
            response.CreationTime = hit.creationTime.toString()
            response.Title = hit.hitType.title
            response.Description = hit.hitType.description
            response.Keywords = hit.hitType.keywords
            response.HITStatus = hit.hitStatus
            response.Question = hit.question
            response.RequesterAnnotation = hit.requesterAnnotation
            response.HITReviewStatus = hit.reviewStatus

            // HitType related Information
            response.Reward = [:]
            response.Reward.Amount = hit.hitType.reward.toString()
            response.Reward.CurrencyCode = "USD"
            response.Reward.FormattedPrice = "USD${response.Reward.Amount}"
            response.LifetimeInSeconds = hit.lifetimeInSeconds.toString()
            response.AssignmentDurationInSeconds = hit.hitType.assignmentDurationInSeconds.toString()
            response.MaxAssignments = hit.maxAssignments.toString()
            response.AutoApprovalDelayInSeconds = hit.hitType.autoApprovalDelayInSeconds.toString()
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

        if(!type.save())
            throw new ValidationException("Impossible to RegisterHITType ${request}", type.errors)

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

    def AssignQualification(request) {
        def worker = Worker.findById(Long.parseLong(request.WorkerId))
        def qt = QualificationType.findById(Long.parseLong(request.QualificationTypeId))

        def qa = new QualificationAssignment(
                                    worker: worker,
                                    qualificationType: qt,
                                    integerValue: Integer.parseInt(request.IntegerValue),
                                    sendNotification: request.SendNotification)

        if(!qa.save()) throw new ValidationException("Unable to save QualificationAssignment ${request}", qa.errors)
        return null
    }

    def GrantQualification(request) {
        def qRequest = QualificationRequest.findById(Long.parseLong(request.QualificationRequestId))
        def qa = new QualificationAssignment(worker: qRequest.worker, qualificationType: qRequest.qualificationType, integerValue: Integer.parseInt(request.IntegerValue), sendNotification: "true")

        if(!qa.save()) throw new ValidationException("Unable to create QualificationAssignment as part of the GrantQualificationRequest for ${request}", qa.errors)
        qRequest.assignment = qa
        if(!qRequest.save()) throw new javax.xml.bind.ValidationException("Unable to link assignment to request", qRequest.errors)

        return null
    }

    def ChangeHITTypeOfHIT(request) {
        def hit = Hit.findById(Long.parseLong(request.HITId))
        def hitType = HitType.findById(Long.parseLong(request.HITTypeId))

        if(hit==null) throw new IllegalArgumentException("Hit ${request.HITId} does not exist")

        if(hitType==null) throw new IllegalArgumentException("HitType ${request.HITTypeId} does not exist")


        hit.setHitType(hitType)

        if(!hit.save())
            throw new ValidationException("Unable to change hit type ${request}")
    }

    def ExtendHIT(request) {
        def hit = Hit.findById(Long.parseLong(request.HITId))

        hit.maxAssignments += Integer.parseInt(request.MaxAssignmentsIncrement)
        if(hit.lifetimeInSeconds != null)
            hit.lifetimeInSeconds += Long.parseLong(request.ExpirationIncrementInSeconds)
        if(!hit.save()) throw new ValidationException("Unable to ExtendHIT ${request}", hit.errors)
        return null
    }

    def ForceExpireHIT(request) {
        def hit = Hit.findById(Long.parseLong(request.HITId))
        hit.lifetimeInSeconds = 0
        if(!hit.save()) throw new ValidationException("Unable to expire HIT: ${request}", hit.errors)
        return null
    }

    def SetHITAsReviewing(request) {
        def hit = Hit.findById(Long.parseLong(request.HITId))
        def revert = Boolean.parseBoolean(request.Revert)
        if(revert) {
            if(hit.hitStatus == "Reviewing")
                hit.hitStatus = "Reviewable"
            else
                throw new IllegalStateException("Unable to revert status of ${request.HITId} because current status is not Reviewing. Status: ${hit.hitStatus} Request: ${request}")
        } else {
            if(hit.hitStatus == "Reviewing")
                throw new IllegalStateException("Hit is already in the Reviewing status")
            else
                hit.hitStatus = "Reviewing"
        }
        if(!hit.save()) throw new ValidationException("Unable to change hit status SetHITAsReviewing(${request})", hit.errors)
        return null
    }

    /**
     * TODO: Implent
     * RejectQualificationRequest
     * RevokeQualification
     * UpdateQualificationScore
     * UpdateQualificationType
     */
}
