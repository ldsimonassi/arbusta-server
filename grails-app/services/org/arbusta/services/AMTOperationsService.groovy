package org.arbusta.services

import grails.gorm.DetachedCriteria
import org.arbusta.domain.Assignment;
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

            response.HITId =  hit.id.toString()
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
        def qt = QualificationType.findById(Long.parseLong(request.QualificationTypeId))
        if(qt == null) throw new IllegalArgumentException("QualificationType not found ${request.QualificationTypeId} on request [${request}]")

        if(request.Description)
            qt.description = request.Description

        if(request.QualificationTypeStatus)
            qt.qualificationTypeStatus = request.QualificationTypeStatus

        if(request.Test)
            qt.test = request.Test

        if(request.AnswerKey)
            qt.answerKey = request.AnswerKey

        if(request.TestDurationInSeconds)
            qt.testDurationInSeconds = Long.parseLong(request.TestDurationInSeconds)

        if(request.RetryDelayInSeconds)
            qt.retryDelayInSeconds = Long.parseLong(request.RetryDelayInSeconds)

        if(request.AutoGranted)
            qt.autoGranted = Boolean.parseBoolean(request.AutoGranted)

        if(request.AutoGrantedValue)
            qt.autoGrantedValue = Integer.parseInt(request.AutoGrantedValue)

        if(!qt.save(flush:true)) throw new ValidationException("Error while saving QualificationType ${request}", qt.errors)

        def response = [:]

        response.QualificationType = [:]

        response.QualificationType.QualificationTypeId = qt.id.toString()
        response.QualificationType.CreationTime = qt.creationTime.toString()
        response.QualificationType.Name = qt.name
        response.QualificationType.Description = qt.description
        response.QualificationType.Keywords = qt.keywords
        response.QualificationType.QualificationTypeStatus = qt.qualificationTypeStatus
        response.QualificationType.RetryDelayInSeconds = qt.retryDelayInSeconds.toString()
        response.QualificationType.IsRequestable = qt.isRequestable.toString()

        return response
    }
	
	
	def GetHIT(request) {
		def hitId = Long.parseLong(request.HITId)
		def hit = Hit.findById(hitId)
		
		if(!hit)
			throw new IllegalArgumentException("HITId not found ${hitId} for request: ${request}")

		return buildHitDataStructure(hit)
	}


    def buildHitDataStructure(hit) {
        def response = [:]
        response.HITId = hit.id.toString()
        response.HITTypeId = hit.hitType.id.toString()
        // TODO Implement response.HITGroupId
        // TODO Implement response.HITLayoutId
        response.CreationTime = hit.creationTime.toString()
        response.Title = hit.hitType.title
        response.Description = hit.hitType.description.toString()
        response.Keywords = hit.hitType.keywords.toString()
        response.HITStatus = hit.hitStatus
        response.Reward = [:]
        response.Reward.Amount = hit.hitType.reward.toString()
        response.Reward.CurrencyCode = "USD"
        response.Reward.FormattedPrice = "USD ${response.Reward.Amoun}"
        response.LifetimeInSeconds = hit.lifetimeInSeconds.toString()

        response.AssignmentDurationInSeconds = hit.hitType.assignmentDurationInSeconds.toString()
        response.MaxAssignments = hit.maxAssignments.toString()
        response.AutoApprovalDelayInSeconds = hit.hitType.autoApprovalDelayInSeconds.toString()
        // TODO  Implement response.Expiration
        response.QualificationRequirement = null

        hit.hitType.qualificationRequirements.each { qr ->
            def rsp = [:]
            rsp.QualificationTypeId = qr.qualificationType.id.toString()
            rsp.Comparator = qr.comparator
            rsp.IntegerValue = qr.integerValue.toString()

            if(!response.QualificationRequirement)
                response.QualificationRequirement = rsp
            else if (!response.QualificationRequirement instanceof ArrayList) {
                def tmp = response.QualificationRequirement
                response.QualificationRequirement = []
                response.QualificationRequirement.add tmp
                response.QualificationRequirement.add rsp
            } else
                response.QualificationRequirement.addShutdownHook rsp
        }
        response.Question = hit.question
        response.RequesterAnnotation = hit.requesterAnnotation
        // TODO Implement response.NumberOfSimilarHITs
        response.HITReviewStatus = hit.reviewStatus
        // TODO Implement NumberofAssignmentsPending
        // TODO Implement NumberofAssignmentsAvailable
        // TODO Implement NumberofAssignmentsCompleted
        return response
    }


    def ApproveAssignment(request) {
        def assignmentId = Long.parseLong(request.AssignmentId)
        def assignment = Assignment.findById(assignmentId)
        if(!assignment) throw new IllegalArgumentException("The assignment ${assignmentId} was not found in the db req: $request")
        assignment.requesterFeedback = request.RequesterFeedback
        assignment.status = "Approved"
        if(!assignment.save(flush:true)) throw new ValidationException("Error while saving assignment $assignmentId for request: $request", assignment.errors)
        return null
    }

    def RejectAssignment(request) {
        def assignmentId = Long.parseLong(request.AssignmentId)
        def assignment = Assignment.findById(assignmentId)
        if(!assignment) throw new IllegalArgumentException("The assignment ${assignmentId} was not found in the db req: $request")
        assignment.requesterFeedback = request.RequesterFeedback
        assignment.status = "Rejected"
        if(!assignment.save(flush:true)) throw new ValidationException("Error while saving assignment $assignmentId for request: $request", assignment.errors)
        return null
    }

    def ApproveRejectedAssignment(request) {
        def assignmentId = Long.parseLong(request.AssignmentId)
        def assignment = Assignment.findById(assignmentId)
        if(!assignment) throw new IllegalArgumentException("The assignment ${assignmentId} was not found in the db req: $request")
        if(assignment.status != "Rejected") throw new IllegalStateException("The assignment status is $assignment.status and it should be Rejected. Request data: $request")
        assignment.requesterFeedback = request.RequesterFeedback
        assignment.status = "Approved"
        if(!assignment.save(flush:true)) throw new ValidationException("Error while saving assignment $assignmentId for request: $request", assignment.errors)
        return null
    }

    def GetAssignment(request) {
        def assignment = Assignment.findById(Long.parseLong(request.AssignmentId))
        if(assignment == null) throw new IllegalArgumentException("Assignment not found $request")
        def response = [:]
        response.Assignment = buildAssignmentStructure(assignment)
        response.HIT = buildHitDataStructure(assignment.hit)
        return response
    }

    def buildAssignmentStructure(Assignment assignment) {
        def response =  [:]
        response.AssignmentId = assignment.id.toString()
        response.WorkerId = assignment.worker.id.toString()
        response.HITId = assignment.hit.id.toString()
        response.AssignmentStatus = assignment.status
        response.AutoApprovalTime = assignment.autoApprovalTime.toString()
        response.AcceptTime = assignment.acceptTime.toString()
        response.SubmitTime = assignment.submitTime.toString()
        response.ApprovalTime = assignment.approvalTime.toString()
        response.RejectionTime = assignment.rejectionTime.toString()
        response.Deadline = assignment.deadLine.toString()
        response.Answer = assignment.answer
        response.RequesterFeedback = assignment.requesterFeedback
        return response
    }


    def validateValues(propertyName, value, possibleValues) {
        if(value) {
            if(!possibleValues.contains(value))
                throw new IllegalArgumentException("Invalid value $value for property $propertyName")
        }
    }



    def calculateTotalPages(pageSize, totalCount) {
        pageSize = new BigDecimal(pageSize)
        totalCount = new BigDecimal(totalCount)

        def ret = totalCount.divideAndRemainder(pageSize)
        def totalPages = ret[0]

        if (ret[1] > 0)
            totalPages++

        totalPages
    }



    def GetAssignmentsForHIT(request) {
        def response =  [:]

        long hitId = Long.parseLong(request.HITId)
        def theHit = Hit.findById(hitId)

        if(!theHit)
            throw new IllegalArgumentException("The HITId  $request.HITId was not found in this database")

        // Validate input parameters
        validateValues("AssignmentStatus", request.AssignmentStatus, ["Submitted", "Approved", "Rejected"])

        if(!request.SortProperty) request.SortProperty = "SubmitTime"
        validateValues("SortProperty", request.SortProperty, ["AcceptTime" ,"SubmitTime" , "AssignmentStatus"])

        if(!request.SortDirection) request.SortDirection = "Ascending"
        validateValues("SortDirection", request.SortDirection, ["Ascending", "Descending"])
        def direction = request.SortDirection == "Ascending"? "asc" : "desc"

        int pageSize = request.PageSize?Integer.parseInt(request.PageSize):10
        if(pageSize>100 || pageSize<1) throw new IllegalArgumentException("PageSize values should be between 1 and 100, current value is $pageSize")
        int pageNumber = request.PageNumber? Integer.parseInt(request.PageNumber) : 1
        if(pageNumber<1) throw new IllegalArgumentException("PageSize cannot be negative.")

        DetachedCriteria assignments = new DetachedCriteria(Assignment).build {
            eq 'hit', theHit
            if(request.AssignmentStatus)
                eq 'status', request.AssignmentStatus

        }

        def totalCount = assignments.count()
        def totalPages = calculateTotalPages(pageSize, totalCount)

        if(pageNumber > totalPages)
            throw new IllegalArgumentException("pageNumber: $pageNumber cannot be greater than totalPages[$totalPages] totalCount:[$totalCount] criteria:${assignments.toString()}for given query request: ${beautyfy(request)}")

        response.PageSize = pageSize.toString()
        response.PageNumber = pageNumber.toString()
        response.TotalNumResults = totalCount


        def offset = (pageNumber-1) * pageSize
        def returnedRegs = Math.min(pageSize, totalCount-offset)
        response.NumResults = returnedRegs
        response.Assignment = []

        assignments.list(max: pageSize, offset: offset) {
            if (request.SortProperty == "AcceptTime") {
                order('acceptTime', direction)
            } else if (request.SortProperty == "SubmitTime") {
                order('submitTime', direction)
            } else if (request.SortProperty == "AssignmentStatus") {
                order('status', direction)
            }
        }.each { assignment ->
            response.Assignment.add(buildAssignmentStructure(assignment))
        }
        response
    }


    def beautyfy(obj) {
        _beautify(null, obj, -1)
    }

    def _beautify(key, object, level) {
        def spaces = ""
        level.times {spaces+="\t"}

        def ret = ""
        if(key)
            ret+= "\n${spaces}${key}:"
        if(object instanceof ArrayList) {
            ArrayList list = (ArrayList) object
            list.eachWithIndex { element, index ->
                ret+= _beautify("[$index]", element, level+1)
            }
        } else if(object instanceof LinkedHashMap) {
            LinkedHashMap map = (LinkedHashMap)object
            for(e in map) {
                ret+= _beautify(e.key.toString(), e.value, level+1)
            }

        } else {
            ret+= object.toString()
        }
        ret
    }
}
