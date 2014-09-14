package tests

import grails.validation.ValidationException
import spock.lang.*
import org.arbusta.domain.*
import org.arbusta.services.AMTOperationsService

import java.sql.Timestamp

class AMTOperationsServiceSpec extends Specification {
    def AMTOperationsService

    def "create simple qualification type" () {
        setup:
            def request = TestsHelper.loadRequest("CreateQualificationType")
        when:
            def response = AMTOperationsService.CreateQualificationType(request)
        then:
            assert response != null
            assert response.QualificationType != null
            assert response.QualificationType.QualificationTypeId != null
            assert response.QualificationType.CreationTime != null
            assert response.QualificationType.Name == request.Name
            assert response.QualificationType.Description == request.Description
            assert response.QualificationType.QualificationTypeStatus == request.QualificationTypeStatus
            assert response.QualificationType.AutoGranted.toString() == request.AutoGranted
    }

    def "domain object should not be persisted between tests" () {
        when:
            def qt = QualificationType.findById(1)
        then:
            assert qt == null
    }

    def "register hit type with simple qualification requirement" () {
        setup:
            // Create a QualificationType
            def request = TestsHelper.loadRequest("CreateQualificationType")
            def qualificationTypeId = AMTOperationsService.CreateQualificationType(request).QualificationType.QualificationTypeId

            // Build the RegisterHitType Request
            request = TestsHelper.loadRequest("RegisterHITType")

            // Link the QualificationRequirement
            request.QualificationRequirement.QualificationTypeId = ""+qualificationTypeId;
        when:
            def response = AMTOperationsService.RegisterHITType(request)
            println "Response: ${response}"
            println "TypeId: ${response.RegisterHITTypeResult.HITTypeId}"
        then:
            assert response != null
            assert response.RegisterHITTypeResult.HITTypeId !=null
    }

    def "register hit type with multiple qualification requirement" () {
        setup:
            // Create a QualificationType
            def request = TestsHelper.loadRequest("CreateQualificationType")
            def qualificationTypeId1 = AMTOperationsService.CreateQualificationType(request).QualificationType.QualificationTypeId
            def qualificationTypeId2 = AMTOperationsService.CreateQualificationType(request).QualificationType.QualificationTypeId

            // Build the RegisterHitType Request
            request = TestsHelper.loadRequest("RegisterHITType")

            // Link two QualificationRequirements
            request.QualificationRequirement = []
            request.QualificationRequirement.add([:])
            request.QualificationRequirement.add([:])

            request.QualificationRequirement[0].QualificationTypeId = "" + qualificationTypeId1;
            request.QualificationRequirement[0].Comparator = "GreaterThan"
            request.QualificationRequirement[0].IntegerValue = "7"
            request.QualificationRequirement[0].RequiredToPreview = "false"

            request.QualificationRequirement[1].QualificationTypeId = "" + qualificationTypeId2;
            request.QualificationRequirement[1].Comparator = "LessThanOrEqualTo"
            request.QualificationRequirement[1].IntegerValue = "3"
            request.QualificationRequirement[1].RequiredToPreview = "false"
        when:
            def response = AMTOperationsService.RegisterHITType(request)
            println "Response: ${response}"
            println "TypeId: ${response.RegisterHITTypeResult.HITTypeId}"
        then:
            assert response != null
            assert response.RegisterHITTypeResult.HITTypeId !=null
    }

    def "create simple hit without hit_type" () {
        setup:
            def request = TestsHelper.loadRequest("CreateHitWOTypeId")
        when:
            def response = AMTOperationsService.CreateHIT(request)
        then:
            assert response != null
            assert response.HITId != null
            assert response.HITTypeId != null
            assert response.CreationTime != null
            assert response.Title == request.Title
            assert response.Description == request.Description
            assert response.Question != null
            assert response.MaxAssignments.toString() == request.MaxAssignments
            assert response.AutoApprovalDelayInSeconds.toString() == request.AutoApprovalDelayInSeconds
            assert response.LifetimeInSeconds.toString() == request.LifetimeInSeconds
            assert response.AssignmentDurationInSeconds.toString() == request.AssignmentDurationInSeconds
            assert response.Reward.Amount.toString() == request.Reward.Amount
    }

    def "create complex hit with hit_type" () {
        setup:
            def request
            def response

            //Register Hit Type
            request = TestsHelper.loadRequest("RegisterHITType")
            request.QualificationRequirement = null
            response = AMTOperationsService.RegisterHITType(request)

            def hitTypeId = response.RegisterHITTypeResult.HITTypeId
            //Prepare CreateHitRequest
            request = TestsHelper.loadRequest("CreateHitWTypeId")
            request.HITTypeId = hitTypeId

        when:
            response = AMTOperationsService.CreateHIT(request)
            println "Hit Created response :[${response}]"
        then:
            assert response != null
            assert response.HITId != null
            assert response.HITTypeId != null
            assert response.CreationTime != null
            assert response.Question != null
            assert response.MaxAssignments.toString() == request.MaxAssignments
            assert response.LifetimeInSeconds.toString() == request.LifetimeInSeconds
    }


    def "assign qualification to worker" () {
        setup:
            def qTypeId = AMTOperationsService.CreateQualificationType(TestsHelper.loadRequest("CreateQualificationType")).QualificationType.QualificationTypeId
            def worker = TestsHelper.createDummyWorker()
            def request = TestsHelper.loadRequest("AssignQualification")
            // Override dynamic fields
            request.QualificationTypeId = qTypeId.toString()
            request.WorkerId = worker.id.toString()
        when:
            // Assign qualification to worker.
            def response = AMTOperationsService.AssignQualification(request)
        then:
            //assert workerId != null
            assert qTypeId != null
            assert request != null
            assert response == null

            // TODO Add domain related assertions.
    }


    def "grant qualification to a worker" () {
        setup:
            def request = TestsHelper.loadRequest("CreateQualificationType")
            def qt = AMTOperationsService.CreateQualificationType(request).QualificationType.QualificationTypeId
            def qr = TestsHelper.createDummyQualificationRequest(qt)
            request = TestsHelper.loadRequest("GrantQualification")
            request.QualificationRequestId = "${qr.id}"
        when:
            def response = AMTOperationsService.GrantQualification(request)
        then:
            assert response == null
            qr.assignment != null
    }

    def "change hit type" () {
        setup:
            // Register Hit Type
            def request = TestsHelper.loadRequest("RegisterHITType")
            request.QualificationRequirement = null
            def response = AMTOperationsService.RegisterHITType(request)
            def hitTypeId = response.RegisterHITTypeResult.HITTypeId

            // Create Hit
            request = TestsHelper.loadRequest("CreateHitWOTypeId")
            response = AMTOperationsService.CreateHIT(request)
            def hitId = response.HITId

            // Prepare the request to be tested
            request = TestsHelper.loadRequest("ChangeHITTypeOfHIT")
            request.HITId = hitId.toString()
            request.HITTypeId = hitTypeId.toString()
        when:
            response = AMTOperationsService.ChangeHITTypeOfHIT(request)
        then:
            assert response == null
            assert Hit.findById(hitId).hitType.id.toString() == hitTypeId
    }

    def "extend hit duration and assignments"() {
        setup:
            // Create Hit
            def request = TestsHelper.loadRequest("CreateHitWOTypeId")
            def response = AMTOperationsService.CreateHIT(request)
            def hitId = response.HITId
            def hit = Hit.findById(Long.parseLong(hitId))
            def previousLifetimeDuration = hit.lifetimeInSeconds
            def previousMaxAssignments = hit.maxAssignments
            // Prepare the request to be tested
            request = TestsHelper.loadRequest("ExtendHIT")
            request.HITId = hitId
        when:
            response = AMTOperationsService.ExtendHIT(request)
        then:
            assert response == null
            assert Hit.findById(hitId).maxAssignments == Integer.parseInt(request.MaxAssignmentsIncrement) + previousMaxAssignments
            assert Hit.findById(hitId).lifetimeInSeconds == Long.parseLong(request.ExpirationIncrementInSeconds) + previousLifetimeDuration
            // Verify increments
            // Hit.findById(Long.parseLong(hitId)).

    }

    def "force expiration of a HIT using ForceExpireHIT"() {
       setup:
           // Create Hit
           def request = TestsHelper.loadRequest("CreateHitWOTypeId")
           def response = AMTOperationsService.CreateHIT(request)
           def hitId = response.HITId
           request = TestsHelper.loadRequest("ForceExpireHIT")
           request.HITId = hitId
       when:
            response = AMTOperationsService.ForceExpireHIT(request)
      then:
            assert response == null
            assert Hit.findById(Long.parseLong(request.HITId)).lifetimeInSeconds == 0
    }

    def "set hit as reviewing" () {
        setup:
            // Create temporary Hit
            def request = TestsHelper.loadRequest("CreateHitWOTypeId")
            def response = AMTOperationsService.CreateHIT(request)
            def hitId = response.HITId

            // Prepare SetHITAsReviewing request
            request = TestsHelper.loadRequest("SetHITAsReviewing")
            request.HITId = hitId
            request.Revert = "false"
        when:
            response = AMTOperationsService.SetHITAsReviewing(request)

        then:
            assert Hit.findById(Long.parseLong(hitId)).hitStatus == "Reviewing"
            assert response == null
    }

    def "set hit as reviewable" () {
        setup:
            // Create temporary Hit
            def request = TestsHelper.loadRequest("CreateHitWOTypeId")
            def response = AMTOperationsService.CreateHIT(request)
            def hitId = response.HITId

            // Prepare SetHITAsReviewing request
            request = TestsHelper.loadRequest("SetHITAsReviewing")
            request.HITId = hitId
            request.Revert = "false"
            response = AMTOperationsService.SetHITAsReviewing(request)
            request.Revert = "true"
        when:
            response = AMTOperationsService.SetHITAsReviewing(request)
        then:
            assert Hit.findById(Long.parseLong(hitId)).hitStatus == "Reviewable"
            assert response == null
    }

    def "reject qualification request" () {
        setup:
            def request = TestsHelper.loadRequest("CreateQualificationType")
            def qt = AMTOperationsService.CreateQualificationType(request).QualificationType.QualificationTypeId
            def qr = TestsHelper.createDummyQualificationRequest(qt)
            request = TestsHelper.loadRequest("RejectQualificationRequest")
            request.QualificationRequestId = qr.id.toString()
        when:
            def response = AMTOperationsService.RejectQualificationRequest(request)
        then:
            assert response == null
            assert QualificationRequest.findById(Long.parseLong(request.QualificationRequestId)).status == "Rejected"
            assert QualificationRequest.findById(Long.parseLong(request.QualificationRequestId)).reason == request.Reason

    }

    def "revoke qualification" () {
        setup:
            // Create qualification type
            def request = TestsHelper.loadRequest("CreateQualificationType")
            def qualificationTypeId = AMTOperationsService.CreateQualificationType(request).QualificationType.QualificationTypeId
            def qualificationType = QualificationType.findById(Long.parseLong(qualificationTypeId))
            def qualificationRequest = TestsHelper.createDummyQualificationRequest(qualificationTypeId)
            def worker = qualificationRequest.worker
            // Grant qualification
            request = TestsHelper.loadRequest("GrantQualification")
            request.QualificationRequestId = qualificationRequest.id.toString()
            AMTOperationsService.GrantQualification(request)
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qualificationType, [lock: true]) != null
            // Prepare revoke
            request = TestsHelper.loadRequest("RevokeQualification")
            request.SubjectId = worker.id.toString()
            request.QualificationTypeId = qualificationTypeId
            request.Reason = "We don't know"
        when:
            // Revoke qualification
            def response = AMTOperationsService.RevokeQualification(request)
        then:
            // Assert null response
            assert response == null
            // Assert no qualification assigned
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qualificationType) == null
    }


    def "update worker qualification score for a given qualification type" () {
        setup:
            // Create qualification type
            def request = TestsHelper.loadRequest("CreateQualificationType")
            def qualificationTypeId = AMTOperationsService.CreateQualificationType(request).QualificationType.QualificationTypeId
            def qualificationType = QualificationType.findById(Long.parseLong(qualificationTypeId))
            def qualificationRequest = TestsHelper.createDummyQualificationRequest(qualificationTypeId)
            def worker = qualificationRequest.worker
            // Grant qualification
            request = TestsHelper.loadRequest("GrantQualification")
            request.QualificationRequestId = qualificationRequest.id.toString()
            AMTOperationsService.GrantQualification(request)

            // Assert that the qualification has been granted
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qualificationType, [lock: true]) != null

            // Prepare update qualification request
            request = TestsHelper.loadRequest("UpdateQualificationScore")
            request.QualificationTypeId = qualificationTypeId
            request.SubjectId = worker.id.toString()
        when:
            def response = AMTOperationsService.UpdateQualificationScore(request)
        then:
            assert response == null
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qualificationType).integerValue == Integer.parseInt(request.IntegerValue)
    }

    def workers = [:]
    def qualificationTypes = [:]
    def qualificationRequests = [:]


    def cleanup() {
        println "Cleaning up"
    }

    // Populate the database
    def setup () {
        workers.dario = new Worker(
                firstName: "Luis Dario",
                lastName: "Simonassi",
                email: "dario@arbusta.com"
        )

        workers.jagger = new Worker(
                firstName: "Michael",
                lastName: "Jagger",
                email: "michael.jagger@rollingstones.com"
        )

        workers.paul = new Worker(
                firstName: "Paul",
                lastName: "McCartney",
                email: "paul@beatles.com"
        )

        qualificationTypes["english"] =
                new QualificationType(
                        creationTime: new Timestamp(System.currentTimeMillis()),
                        name: "English language",
                        description: "Knowledge to read/write and listen the english language",
                        keywords: "language, english, writing",
                        qualificationTypeStatus: "Active",
                        retryDelayInSeconds: (3600*24*7),
                        test: "Write the result of: How much is two plus two?",
                        testDurationInSeconds: 300,
                        answerKey: "two",
                        autoGranted: false,
                        isRequestable: true,
                        autoGrantedValue: null)

        qualificationTypes["math"] =
                new QualificationType(
                        creationTime: new Timestamp(System.currentTimeMillis()),
                        name: "Basic mathematics",
                        description: "Knowledge on basic arithmetic operations",
                        keywords: "mathematics, algebra, basic",
                        qualificationTypeStatus: "Active",
                        retryDelayInSeconds: (3600*24*7),
                        test: "(2+3+5)/(2*1)",
                        testDurationInSeconds: 20,
                        answerKey: "5",
                        autoGranted: false,
                        isRequestable: true,
                        autoGrantedValue: null)

        qualificationTypes["trusted_worker"] =
                new QualificationType(
                        creationTime: new Timestamp(System.currentTimeMillis()),
                        name: "Trusted worker 1st degree",
                        description: "Based on your work history, we bealive you'll continue to work fine",
                        keywords: "reputation, relationship",
                        qualificationTypeStatus: "Active",
                        retryDelayInSeconds: (3600*24*7),
                        test: null,
                        testDurationInSeconds: 0,
                        answerKey: null,
                        autoGranted: false,
                        isRequestable: false,
                        autoGrantedValue: null)

        qualificationTypes["guitar"] =
                new QualificationType(
                        creationTime: new Timestamp(System.currentTimeMillis()),
                        name: "Guitar abilities required",
                        description: "We need you to be a rockstar playing the guitar",
                        keywords: "music, guitar, rock",
                        qualificationTypeStatus: "Active",
                        retryDelayInSeconds: (3600*24*7),
                        test: "Play these notes with your guitar",
                        testDurationInSeconds: 3600,
                        answerKey: "Upload the audio",
                        autoGranted: false,
                        isRequestable: false,
                        autoGrantedValue: null)


        qualificationRequests["dario_english"] = new QualificationRequest(
                worker: workers["dario"],
                qualificationType: qualificationTypes["english"],
                test: "Write the result of: How much is two plus two?",
                answer: "four",
                submitTime: new Timestamp(System.currentTimeMillis()),
                assignment: null,
                status: "Pending",
                reason: null
        )

        qualificationRequests["paul_guitar"] = new QualificationRequest(
                worker: workers["paul"],
                qualificationType: qualificationTypes["guitar"],
                test: "Play these notes with your guitar",
                answer: "Sound uploaded",
                submitTime: new Timestamp(System.currentTimeMillis()),
                assignment: null,
                status: "Pending",
                reason: null
        )

        [workers, qualificationTypes, qualificationRequests].each { toSave ->
            toSave.keySet().each { key ->
                if (!toSave[key].save())
                    throw new ValidationException("Unable to save $key", toSave[key].errors)
            }
        }
    }

//    def "update the parameters of a given qualification type" () {

  //@Unroll("wee #a #b")
  //      setup:
            // Create dummy qualification type
            // Prepare update request
            // UpdateQualificationType
            //  QualificationTypeId
            //  Description
            //  QualificationTypeStatus
            //  Test
            //  AnswerKey
            //  TestDurationInSeconds
            //  RetryDelayInSeconds
            //  AutoGranted
            //  AutoGrantedValue

    //    when:
            // Excecute update
      //  then:
            // Assert changes have been successful
    //}

    /****************************************************
     * TODO: Implement the following tests:
     * UpdateQualificationType
     ***************************************************/

}
