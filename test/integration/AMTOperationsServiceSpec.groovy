import spock.lang.Specification
import spock.lang.*
import org.arbusta.services.AMTOperationsService

class AMTOperationsServiceSpec extends Specification {

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

}
