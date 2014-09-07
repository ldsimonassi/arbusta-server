import spock.lang.*
import org.arbusta.services.AMTOperationsService
import org.arbusta.domain.*

class AMTOperationsServiceSpec extends AbstractSpec {

    //Fields
    def AMTOperationsService

    def "create simple qualification type" () {
        setup:
            def request = [:]
            request.Name = "Conocimientos de ingles"
            request.Keywords = "ingles,idioma,lenguaje"
            request.Description = "Saber escribir y escuchar en idioma ingles"
            request.QualificationTypeStatus = "Active"
            request.RetryDelayInSeconds = "604800"
            request.Test = "The cat is under the table"
            request.AnswerKey = "No"
            request.TestDurationInSeconds = "3600"
            request.AutoGranted = "false"
        when:
            def response = AMTOperationsService.CreateQualificationType(request)
            println "**** Response!! :"
            println response
            println "**** Response!! :"
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
            def qt = createValidQualificationType(AMTOperationsService)
            def request = [:]
            request.AutoApprovalDelayInSeconds = "604800"
            request.AssignmentDurationInSeconds = "3600"
            request.Reward = [:]
            request.Reward.Amount = "0.1"
            request.Reward.CurrencyCode = "USD"
            request.Reward.FormattedPrice = "USD 0.1"
            request.Title = "Solve the following equation"
            request.Keywords = "math, equations, english, basic"
            request.Description = "Solve those simple equations"
            request.QualificationRequirement = [:]
            request.QualificationRequirement.QualificationTypeId = ""+qt.QualificationType.QualificationTypeId;
            request.QualificationRequirement.Comparator = "GreaterThan"
            request.QualificationRequirement.IntegerValue = "7"
            request.RequiredToPreview = "true"
            request.ResponseGroup = "ALL"
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
        def qt1 = createValidQualificationType(AMTOperationsService)
        def qt2 = createValidQualificationType(AMTOperationsService)

        def request = [:]
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
        response.RegisterHITTypeResult.HITTypeId != null
    }

    def "create hit" () {

    }
}
