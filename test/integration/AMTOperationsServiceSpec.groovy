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
            println "*******"
            println qt
            println "*******"
            def request = [:]
            request.AutoApprovalDelayInSeconds = "604800"
            request.AssignmentDurationInSeconds = "3600"
            request.Reward = [:]
            request.Reward.Amount = "0.1"
            request.Reward.CurrencyCode = "USD"
            request.Reward.FormattedPrice = "USD 0.1"
            request.Title
            request.Keywords
            request.Description = ""
            request.QualificationRequirement = [:]
            println "About to use: ${qt.QualificationType.QualificationTypeId}"
            request.QualificationRequirement.QualificationTypeId = qt.QualificationType.QualificationTypeId;
            request.QualificationRequirement.Comparator =
            request.QualificationRequirement.Comparator = "GreaterThan"
            request.QualificationRequirement.IntegerValue = "7"
            request.RequiredToPreview = "true"
            request.ResponseGroup = "ALL"
        when:
            def response = AMTOperationsService.RegisterHITType(request)
        then:
            assert response != null
    }


    def "register hit type with multiple qualification requirement" () {

    }

    def "create hit" () {

    }
}
