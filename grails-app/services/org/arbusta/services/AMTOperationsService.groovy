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
            response = RegisterHITType(request)
            request.HITTypeId = response.RegisterHITTypeResult.HITTypeId
        }

        if(request.Question == null){
            throw new Exception("Either a Question parameter or a HITLayoutId parameter must be provided.")
        } else {
            def hitType = HitType.findById(Long.parseLong(request.HITTypeId))

            if(!hitType) throw new IllegalArgumentException("Unable to find HITTypeId: ${HITTypeId} ${request}")

            def hit = new Hit(hitType: hitType,
                    question: request.Question,
                    requesterAnnotation: request.RequesterAnnotation,
                    lifetimeInSeconds: Long.parseLong(request.LifetimeInSeconds),
                    maxAssignments: Integer.parseInt(request.MaxAssignments),
                    creationTime: new Timestamp(System.currentTimeMillis())
            );

            if(!hit.save(flush:true)) throw new ValidationException("Unable to save Hit ${hit}", hit.errors)

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
                list = []
                list.add(qr)
            } else {
                list = qr
            }

            list.each() { qReq ->
                try {
                    def qt = QualificationType.findById(Long.parseLong(qReq.QualificationTypeId))

                    if(!qt)
                        throw new IllegalArgumentException("Undefined qualification type ${qReq.QualificationTypeId}")

                    def q = new QualificationRequirement(
                            qualificationType: qt,
                            comparator: qReq.Comparator,
                            integerValue: Integer.parseInt(qReq.IntegerValue))

                    type.addToQualificationRequirements(q)

                } catch (e) {
                    throw new Exception("Error while trying to create QualificationRequirement [" + qReq + "]", e)
                }
            }
        }

        if(!type.save(flush:true))
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
                keywords: request.Keywords, creationTime: new Timestamp(System.currentTimeMillis()),
                retryDelayInSeconds: request.RetryDelayInSeconds,
                qualificationTypeStatus: request.QualificationTypeStatus,
                test: request.Test, answerKey: request.AnswerKey,
                testDurationInSeconds:request.TestDurationInSeconds)

        q.save(flush:true)

        if(q.hasErrors()) throw new ValidationException(q.errors)

        def ret = [:]
        ret.QualificationType = [:]
        ret.QualificationType.QualificationTypeId = q.id.toString()
        ret.QualificationType.CreationTime = q.creationTime.toString()
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
                                    sendNotification: request.SendNotification,
                                    request: null)

        if(!qa.save(flush:true)) throw new ValidationException("Unable to save QualificationAssignment ${request}", qa.errors)
        return null
    }

    def GrantQualification(request) {
        def qRequest = QualificationRequest.findById(Long.parseLong(request.QualificationRequestId))
        def qa = new QualificationAssignment(worker: qRequest.worker, qualificationType: qRequest.qualificationType, integerValue: Integer.parseInt(request.IntegerValue), sendNotification: "true", request: qRequest)

        if(!qa.save(flush:true)) throw new ValidationException("Unable to create QualificationAssignment as part of the GrantQualificationRequest for ${request}", qa.errors)
        qRequest.assignment = qa
        if(!qRequest.save(flush:true)) throw new ValidationException("Unable to link assignment to request", qRequest.errors)

        return null
    }

    def ChangeHITTypeOfHIT(request) {
        def hit = Hit.findById(Long.parseLong(request.HITId))
        def hitType = HitType.findById(Long.parseLong(request.HITTypeId))

        if(hit==null) throw new IllegalArgumentException("Hit ${request.HITId} does not exist")

        if(hitType==null) throw new IllegalArgumentException("HitType ${request.HITTypeId} does not exist")


        println "###########"

        println "Changing ${hit.hitType.id} -> ${hitType.id} in [${hit.id}]"
        hit.hitType = hitType
        println "New value ${hit.hitType.id} "

        if(!hit.save(flush:true))
            throw new ValidationException("Unable to change hit type ${request}", hit.errors)



        println "New value ${hit.refresh().hitType.id} after save ${hit.errors}"

        return null
    }

    def ExtendHIT(request) {
        def hit = Hit.findById(Long.parseLong(request.HITId))

        hit.maxAssignments += Integer.parseInt(request.MaxAssignmentsIncrement)
        if(hit.lifetimeInSeconds != null)
            hit.lifetimeInSeconds += Long.parseLong(request.ExpirationIncrementInSeconds)
        if(!hit.save(flush:true)) throw new ValidationException("Unable to ExtendHIT ${request}", hit.errors)
        return null
    }

    def ForceExpireHIT(request) {
        def hit = Hit.findById(Long.parseLong(request.HITId))
        hit.lifetimeInSeconds = 0
        if(!hit.save(flush:true)) throw new ValidationException("Unable to expire HIT: ${request}", hit.errors)
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
        if(!hit.save(flush:true)) throw new ValidationException("Unable to change hit status SetHITAsReviewing(${request})", hit.errors)
        return null
    }

    def RejectQualificationRequest(request) {
        def qualifId = Long.parseLong(request.QualificationRequestId)
        def reason = request.Reason

        def qualificationRequest = QualificationRequest.findById(qualifId)

        qualificationRequest.status = "Rejected"
        qualificationRequest.reason = reason

        return null
    }

    def RevokeQualification(request) {
        def worker = Worker.findById(Long.parseLong(request.SubjectId))
        def qualificationType = QualificationType.findById(Long.parseLong(request.QualificationTypeId))
        def qualificationAssignment = QualificationAssignment.findByWorkerAndQualificationType(worker, qualificationType, [lock: true])

        if (!qualificationAssignment)
            throw new IllegalArgumentException("No qualificationAssignment found for Worker:${worker} QualificationType:${qualificationType} Request: ${Rquest}")

        if (qualificationAssignment.request) {
            qualificationAssignment.request.reason = request.Reason
            qualificationAssignment.request.status = "Rejected"
            qualificationAssignment.request.assignment = null
            qualificationAssignment.request.save(flush:true)
        }
        qualificationAssignment.delete()
        return null
    }

    def UpdateQualificationScore(request) {
        def qualificationType = QualificationType.findById(Long.parseLong(request.QualificationTypeId))
        def worker = Worker.findById(Long.parseLong(request.SubjectId))
        def integerValue = Integer.parseInt(request.IntegerValue)

        if(!qualificationType) throw new IllegalArgumentException("QualificationType does not exist Request:${request}")
        if(!worker) throw new IllegalArgumentException("Worker does not exists Request: ${request}")

        def qualificationAssignment = QualificationAssignment.findByWorkerAndQualificationType(worker, qualificationType, [lock: true])

        if(!qualificationAssignment) throw new IllegalArgumentException("There is no granted qualification for $worker and $qualificationType")

        qualificationAssignment.integerValue = integerValue

        if(!qualificationAssignment.save(flush:true)) throw new ValidationException("Unable to update qualification ${request}", qualificationAssignment.errors)

        return null
    }

    def UpdateQualificationType(request) {
        // TODO Implement

    }
}
