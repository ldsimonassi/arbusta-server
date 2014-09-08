package tests

import spock.lang.*
import org.arbusta.domain.*
import org.arbusta.services.AMTOperationsService

class AMTOperationsServiceSpec extends Specification {

    //Fields
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

/*
    def "register hit type with multiple qualification requirement" () {
        setup:

            def request = buildCreateQualificationTypeRequest()
            def qt1 = AMTOperationsService.CreateQualificationType(request)
            def qt2 = AMTOperationsService.CreateQualificationType(request)

            request = buildRegisterHit
            request.AutoApprovalDelayInSeconds = "604800"
            request.AssignmentDurationInSeconds = "3600"
            request.Reward = [:]
            request.Reward.Amount = "0.1"
            request.Reward.CurrencyCode = "USD"
            request.Reward.FormattedPrice = "USD 0.1"
            request.Title = "Solve the following equation"
            request.Keywords = "math, equations, english, basic"
            request.Description = "Solve those simple equations"
            request.RequiredToPreview = "true"
            request.ResponseGroup = "ALL"

            request.QualificationRequirement = []
            request.QualificationRequirement.add([:])
            request.QualificationRequirement.add([:])

            request.QualificationRequirement[0].QualificationTypeId = "" + qt1.QualificationType.QualificationTypeId;
            request.QualificationRequirement[0].Comparator = "GreaterThan"
            request.QualificationRequirement[0].IntegerValue = "7"
            request.QualificationRequirement[0].RequiredToPreview = "false"

            request.QualificationRequirement[1].QualificationTypeId = "" + qt2.QualificationType.QualificationTypeId;
            request.QualificationRequirement[1].Comparator = "LessThanOrEqualTo"
            request.QualificationRequirement[1].IntegerValue = "3"
            request.QualificationRequirement[1].RequiredToPreview = "false"
        when:
            def response = AMTOperationsService.RegisterHITType(request)
            println "Response: ${response}"
            println "TypeId: ${response.RegisterHITTypeResult.HITTypeId}"
        then:
            assert response != null
            assert response.RegisterHITTypeResult.HITTypeId != null
    }

    def "create simple hit without hit_type" () {
        setup:
            def request = [:]
            request.MaxAssignments = "5"
            request.AutoApprovalDelayInSeconds = "1296000"
            request.LifetimeInSeconds = "259200"
            request.AssignmentDurationInSeconds = "3600"
            request.Reward =  [:]
            request.Reward.Amount = "0.05"
            request.Reward.CurrencyCode = "USD"
            request.Title = "Answer a question"
            request.Description = "This is a HIT created by the Mechanical Turk SDK.  Please answer the provided question."
            request.Question = "&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;QuestionForm xmlns=&quot;http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd&quot;&gt;  &lt;Question&gt;    &lt;QuestionIdentifier&gt;1&lt;/QuestionIdentifier&gt;    &lt;QuestionContent&gt;      &lt;Text&gt;What is the weather like right now in Seattle, WA?&lt;/Text&gt;    &lt;/QuestionContent&gt;    &lt;AnswerSpecification&gt;      &lt;FreeTextAnswer/&gt;    &lt;/AnswerSpecification&gt;  &lt;/Question&gt;&lt;/QuestionForm&gt;";
        when:


    }

    */
}
