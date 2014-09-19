package tests

import org.arbusta.XmlHelper
import org.codehaus.groovy.grails.io.support.IOUtils
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

    def createDummyQualificationRequest(worker, qualificationType) {
        def qr = new QualificationRequest(worker:worker, qualificationType: qualificationType, test: "The test", answer: "The answer", submitTime: new java.sql.Timestamp(System.currentTimeMillis()), assignment: null)
        if(!qr.save()) throw new ValidationException("Unable to create a dummy Qualification Request", qr.errors)
        return qr
    }


    def createDummyHitWOTypeId() {
        def request = loadRequest("CreateHitWOTypeId")
        def response = AMTOperationsService.CreateHIT(request)
        def hit = Hit.findById(Long.parseLong(response.HITId))
        return hit
    }


    def loadRequest(id) {
        def xml = IOUtils.createXmlSlurper().parse(new File("./test/integration/examples/${id}.xml"))
        def ret = XmlHelper.hashBuilder(xml)
        return ret
    }

    def createDummyHitType() {
        def request = loadRequest("RegisterHITType")
        request.QualificationRequirement = null
        def response = AMTOperationsService.RegisterHITType(request)
        return HitType.findById(Long.parseLong(response.RegisterHITTypeResult.HITTypeId))
    }



    def "create simple qualification type" () {
        setup:
            def request = loadRequest("CreateQualificationType")
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
            def request = loadRequest("RegisterHITType")
            request.QualificationRequirement.QualificationTypeId = qt;
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
            def request = loadRequest("RegisterHITType")

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
            def request = loadRequest("CreateHitWOTypeId")
        when:
            def response = AMTOperationsService.CreateHIT(request)
        then:
            // Response data validation
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
            // Database data validation
            def hit = Hit.findById(Long.parseLong(response.HITId))
            hit.id.toString() == response.HITId
            hit.hitType.id.toString() == response.HITTypeId
            hit.hitType.title == response.Title
            hit.question == response.Question
            hit.maxAssignments.toString() == response.MaxAssignments
            hit.lifetimeInSeconds.toString() == response.LifetimeInSeconds
            hit.hitType.assignmentDurationInSeconds.toString() == response.AssignmentDurationInSeconds
    }

    def "create complex hit with hit_type" () {
        setup:
            //Register Hit Type
            def hitType = createDummyHitType()

            //Prepare CreateHitRequest
            def request = loadRequest("CreateHitWTypeId")
            request.HITTypeId = hitType.id.toString()
        when:
            def response = AMTOperationsService.CreateHIT(request)
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
            hit.hitType.id.toString() == hitType.id.toString()
            hit.question == request.Question
            hit.lifetimeInSeconds.toString() == request.LifetimeInSeconds
            hit.maxAssignments.toString() == request.MaxAssignments
    }

    @Unroll("Assign qualification [#qt.name] to worker [#worker.firstName #worker.lastName]")
    def "assign qualification to worker" () {
        setup:
            def request = loadRequest("AssignQualification")
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
            def qr = createDummyQualificationRequest(worker, qt)
            def request = loadRequest("GrantQualification")
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
            def hitType = createDummyHitType()
            // Create Hit
            def hit = createDummyHitWOTypeId()

            // Prepare the request to be tested
            def request = loadRequest("ChangeHITTypeOfHIT")
            request.HITId = hit.id.toString()
            request.HITTypeId = hitType.id.toString()
        when:
            def response = AMTOperationsService.ChangeHITTypeOfHIT(request)
            hit.refresh()
            println "Finding: ${Hit.findById(hit.id).hitType.id}"
            println "Refreshing: ${hit.hitType.id}"
            println "Request Hit Type: ${request.HITTypeId}"
        then:
            response == null
            hit.hitType.id == hitType.id
    }

    
    def "extend hit duration and assignments"() {
        setup:
            // Create Hit
            def hit = createDummyHitWOTypeId()
            def previousLifetimeDuration = hit.lifetimeInSeconds
            def previousMaxAssignments = hit.maxAssignments

            // Prepare the request to be tested
            def request = loadRequest("ExtendHIT")
            request.HITId = hit.id.toString()
        when:
            def response = AMTOperationsService.ExtendHIT(request)
            hit.refresh()
        then:
            response == null
            hit.maxAssignments == Integer.parseInt(request.MaxAssignmentsIncrement) + previousMaxAssignments
            hit.lifetimeInSeconds == Long.parseLong(request.ExpirationIncrementInSeconds) + previousLifetimeDuration
    }

    
    def "force expiration of a HIT using ForceExpireHIT"() {
        setup:
            // Create Hit
            def request = loadRequest("CreateHitWOTypeId")
            def response = AMTOperationsService.CreateHIT(request)
            def hitId = response.HITId
            request = loadRequest("ForceExpireHIT")
            request.HITId = hitId
        when:
            response = AMTOperationsService.ForceExpireHIT(request)
        then:
            response == null
            Hit.findById(Long.parseLong(request.HITId)).lifetimeInSeconds == 0
    }

    def "set hit as reviewing" () {
        setup:
            // Create temporary Hit
            def hit = createDummyHitWOTypeId()

            // Prepare SetHITAsReviewing request
            def request = loadRequest("SetHITAsReviewing")
            request.HITId = hit.id.toString()
        when:
            request.Revert = "false"
            def response = AMTOperationsService.SetHITAsReviewing(request)
            hit.refresh()
        then:
            hit.hitStatus == "Reviewing"
            response == null
    }

    def "set hit as reviewable" () {
        setup:
            // Create temporary Hit
            def hit = createDummyHitWOTypeId()

            // Prepare SetHITAsReviewing request
            def request = loadRequest("SetHITAsReviewing")
            request.HITId = hit.id.toString()
        when:
            request.Revert = "false"
            def response = AMTOperationsService.SetHITAsReviewing(request)
            request.Revert = "true"
            response = AMTOperationsService.SetHITAsReviewing(request)
            hit.refresh()
        then:
            // Assert response data
            response == null

            // Assert database data
            hit.hitStatus == "Reviewable"
    }


    @Unroll("Rejecting #qt.name to #worker.firstName #worker.lastName")
    def "reject qualification request" () {
        setup:
            def qr = createDummyQualificationRequest(worker, qt)
            def request = loadRequest("RejectQualificationRequest")
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
            def qr = createDummyQualificationRequest(worker, qt)
            // Grant qualification
            def request = loadRequest("GrantQualification")
            request.QualificationRequestId = qr.id.toString()
            AMTOperationsService.GrantQualification(request)
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qt, [lock: true]) != null

            // Prepare revoke
            request = loadRequest("RevokeQualification")
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
            def qualificationRequest = createDummyQualificationRequest(worker, qt)

            // Grant qualification
            def request = loadRequest("GrantQualification")
            request.QualificationRequestId = qualificationRequest.id.toString()
            AMTOperationsService.GrantQualification(request)

            // Assert that the qualification has been granted
            assert QualificationAssignment.findByWorkerAndQualificationType(worker, qt, [lock: true]) != null

            // Prepare update qualification request
            request = loadRequest("UpdateQualificationScore")
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
