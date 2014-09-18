package tests


import grails.validation.ValidationException
import spock.lang.*
import org.arbusta.domain.*

import java.sql.Timestamp

class AMTOperationsServiceSpec extends Specification {
    def AMTOperationsService

    @Shared
    def workers

    @Shared
    def qualificationTypes

    // Populate the database
    def setupSpec() {
        workers = [:]
        qualificationTypes = [:]

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

        println "## Setting up..."
        [workers, qualificationTypes].each { toSave ->
            toSave.keySet().each { key ->
                println "#### Saving \"${key}\"..."
                if (!toSave[key].save())
                    throw new ValidationException("Unable to save $key", toSave[key].errors)
                else
                    println "Ok!"
            }
        }
        println "###################"
    }

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

    @Unroll("register hit type with qualification requirement [#qt] qualification type")
    def "register hit type with qualification requirement" () {
        when:
            // Link the QualificationRequirement
            def request = TestsHelper.loadRequest("RegisterHITType")
            request.QualificationRequirement.QualificationTypeId = qt;
            def l = Long.parseLong(request.QualificationRequirement.QualificationTypeId)

            def response = AMTOperationsService.RegisterHITType(request)
        then:
            response != null
            response.RegisterHITTypeResult.HITTypeId !=null
        where:
            qt                            | _
            qualificationTypes["guitar"].id.toString()  | _
            qualificationTypes["english"].id.toString() | _
            qualificationTypes["math"].id.toString()    | _
            qualificationTypes["trusted_worker"].id.toString()    | _
    }

    @Unroll("register hit type with multiple qualification requirement [#qt1.name] [#qt2.name]")
    def "register hit type with multiple qualification requirement" () {
        setup:
            // Build the RegisterHitType Request
            def request = TestsHelper.loadRequest("RegisterHITType")

            // Link two QualificationRequirements
            request.QualificationRequirement = []
            request.QualificationRequirement.add([:])
            request.QualificationRequirement.add([:])

            request.QualificationRequirement[0].QualificationTypeId = "" + qt1.id.toString();
            request.QualificationRequirement[0].Comparator = "GreaterThan"
            request.QualificationRequirement[0].IntegerValue = "7"
            request.QualificationRequirement[0].RequiredToPreview = "false"

            request.QualificationRequirement[1].QualificationTypeId = "" + qt2.id.toString();
            request.QualificationRequirement[1].Comparator = "LessThanOrEqualTo"
            request.QualificationRequirement[1].IntegerValue = "3"
            request.QualificationRequirement[1].RequiredToPreview = "false"
        when:
            // Take the action
            def response = AMTOperationsService.RegisterHITType(request)

        then:
            // Lookup data for assertion
            def hitType = HitType.findById(Long.parseLong(response.RegisterHITTypeResult.HITTypeId))
            def qualifications = QualificationRequirement.findAllByHitType(hitType)

            // Assert responses & data
            response != null
            response.RegisterHITTypeResult.HITTypeId != null
            hitType != null
            hitType.keywords == request.Keywords
            hitType.title == request.Title
            hitType.description == request.Description
            hitType.assignmentDurationInSeconds == Long.parseLong(request.AssignmentDurationInSeconds)
            hitType.autoApprovalDelayInSeconds == Long.parseLong(request.AutoApprovalDelayInSeconds)
            qualifications != null
            qualifications.size() == 2
            qualifications[0].qualificationType.id == qt1.id || qualifications[1].qualificationType.id == qt1.id
            qualifications[0].qualificationType.id == qt2.id || qualifications[1].qualificationType.id == qt2.id
        where:
            qt1 | qt2
            qualificationTypes.guitar |  qualificationTypes.english
            qualificationTypes.math |  qualificationTypes.trusted_worker
    }



    def "create simple hit without hit_type" () {
        setup:
            def request = TestsHelper.loadRequest("CreateHitWOTypeId")
        when:
            def response = AMTOperationsService.CreateHIT(request)
        then:
            response != null
            response.HITId != null
            response.HITTypeId != null
            response.CreationTime != null
            response.Title == request.Title
            response.Description == request.Description
            response.Question != null
            response.MaxAssignments.toString() == request.MaxAssignments
            response.AutoApprovalDelayInSeconds.toString() == request.AutoApprovalDelayInSeconds
            response.LifetimeInSeconds.toString() == request.LifetimeInSeconds
            response.AssignmentDurationInSeconds.toString() == request.AssignmentDurationInSeconds
            response.Reward.Amount.toString() == request.Reward.Amount
            // TODO Check Database
    }

    def "create complex hit with hit_type" () {
        setup:
            //Register Hit Type
            def request = TestsHelper.loadRequest("RegisterHITType")
            request.QualificationRequirement = null
            def response = AMTOperationsService.RegisterHITType(request)
            def hitTypeId = response.RegisterHITTypeResult.HITTypeId
            //Prepare CreateHitRequest
            request = TestsHelper.loadRequest("CreateHitWTypeId")
            request.HITTypeId = hitTypeId
        when:
            response = AMTOperationsService.CreateHIT(request)
        then:
            // Assert response data
            response != null
            response.HITId != null
            response.HITTypeId != null
            response.CreationTime != null
            response.Question != null
            response.MaxAssignments.toString() == request.MaxAssignments
            response.LifetimeInSeconds.toString() == request.LifetimeInSeconds
            // Assert database data
            def hit = Hit.findById(Long.parseLong(response.HITId))
            hit != null
            hit.id.toString() == response.HITId
            hit.hitType.id.toString() == hitTypeId
            hit.question == request.Question
            hit.lifetimeInSeconds.toString() == request.LifetimeInSeconds
            hit.maxAssignments.toString() == request.MaxAssignments
    }

    @Unroll("Assign qualification [#qt.name] to worker [#worker.firstName #worker.lastName]")
    def "assign qualification to worker" () {
        setup:
            def request = TestsHelper.loadRequest("AssignQualification")
            // Override dynamic fields
            request.QualificationTypeId = qt.id.toString()
            request.WorkerId = worker.id.toString()
        when:
            // Assign qualification to worker.
            def response = AMTOperationsService.AssignQualification(request)
        then:
            // Assert response data
            response == null
            // Assert database data
            def qa = QualificationAssignment.findByWorkerAndQualificationType(worker, qt)
            qa != null
            qa.request == null
            qa.integerValue.toString() == request.IntegerValue
            qa.sendNotification.toString() == request.SendNotification
        where:
            qt | worker
            qualificationTypes.guitar | workers.jagger
            qualificationTypes.english | workers.jagger
            qualificationTypes.guitar | workers.paul
            qualificationTypes.english | workers.paul
            qualificationTypes.math | workers.dario
            qualificationTypes.trusted_worker | workers.dario
    }

    @Unroll("grant qualification #qr.name to #worker.firstName #worker.lastName")
    def "grant qualification to a worker" () {
        setup:
            def qr = TestsHelper.createDummyQualificationRequest(worker, qt)
            def request = TestsHelper.loadRequest("GrantQualification")
            request.QualificationRequestId = qr.id.toString()
        when:
            def response = AMTOperationsService.GrantQualification(request)
        then:
            assert response == null
            qr.assignment != null
            def assignment = QualificationAssignment.findByRequest(qr)
            assignment.qualificationType.id == qt.id
            assignment.worker.id == worker.id
            assignment.integerValue.toString() == request.IntegerValue
        where:
            worker | qt
            workers.jagger | qualificationTypes.english
            workers.jagger | qualificationTypes.guitar
            workers.paul | qualificationTypes.english
            workers.paul | qualificationTypes.guitar
            workers.dario | qualificationTypes.math
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


    @Unroll("Rejecting #qt.name to #worker.firstName #worker.lastName")
    def "reject qualification request" () {
        setup:
            def qr = TestsHelper.createDummyQualificationRequest(worker, qt)
            def request = TestsHelper.loadRequest("RejectQualificationRequest")
            request.QualificationRequestId = qr.id.toString()
        when:
            def response = AMTOperationsService.RejectQualificationRequest(request)
        then:
            response == null
            def qr2 = QualificationRequest.findById(Long.parseLong(request.QualificationRequestId))
            qr2.status == "Rejected"
            qr2.reason == request.Reason
        where:
            worker | qt
            workers.jagger | qualificationTypes.english
            workers.jagger | qualificationTypes.guitar
            workers.paul | qualificationTypes.english
            workers.paul | qualificationTypes.guitar
            workers.dario | qualificationTypes.math
    }

    @Unroll("revoke qualification #qt.name to #worker.firstName #worker.lastName")
    def "revoke qualification" () {
        setup:
            // Create qualification request
            def qr = TestsHelper.createDummyQualificationRequest(worker, qt)
            // Grant qualification
            def request = TestsHelper.loadRequest("GrantQualification")
            request.QualificationRequestId = qr.id.toString()
            AMTOperationsService.GrantQualification(request)
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qt, [lock: true]) != null

            // Prepare revoke
            request = TestsHelper.loadRequest("RevokeQualification")
            request.SubjectId = worker.id.toString()
            request.QualificationTypeId = qr.qualificationType.id.toString()
            request.Reason = "We don't know"
        when:
            // Revoke qualification
            def response = AMTOperationsService.RevokeQualification(request)
        then:
            // Assert null response
            response == null
            // Assert no qualification assigned
            QualificationAssignment.findByWorkerAndQualificationType(worker, qt) == null
            def qr2 = QualificationRequest.findById(qr.id)
            qr2.reason == request.Reason
            qr2.status == "Rejected"
        where:
            worker | qt
            workers.jagger | qualificationTypes.english
            workers.jagger | qualificationTypes.guitar
            workers.paul | qualificationTypes.english
            workers.paul | qualificationTypes.guitar
            workers.dario | qualificationTypes.math
    }

    @Unroll("update #worker.firstName #worker.lastName qualification score for #qt.name to #integerValue")
    def "update worker qualification score for a given qualification type" () {
        setup:
            // Create qualification type
            def qualificationRequest = TestsHelper.createDummyQualificationRequest(worker, qt)

            // Grant qualification
            def request = TestsHelper.loadRequest("GrantQualification")
            request.QualificationRequestId = qualificationRequest.id.toString()
            AMTOperationsService.GrantQualification(request)

            // Assert that the qualification has been granted
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qt, [lock: true]) != null

            // Prepare update qualification request
            request = TestsHelper.loadRequest("UpdateQualificationScore")
            request.QualificationTypeId = qt.id.toString()
            request.SubjectId = worker.id.toString()
            request.IntegerValue = integerValue.toString()
        when:
            def response = AMTOperationsService.UpdateQualificationScore(request)
        then:
            response == null
            QualificationAssignment.findByWorkerAndQualificationType(worker, qt).integerValue == integerValue
        where:
            worker | qt | integerValue
            workers.jagger | qualificationTypes.english | 9
            workers.jagger | qualificationTypes.guitar | 9
            workers.paul | qualificationTypes.english | 9
            workers.paul | qualificationTypes.guitar  | 9
            workers.dario | qualificationTypes.math | 5
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
