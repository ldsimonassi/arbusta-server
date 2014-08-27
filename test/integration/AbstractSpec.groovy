import spock.lang.Specification

/**
 * Created by dsimonassi on 8/26/14.
 */
abstract class AbstractSpec extends Specification{

    def createValidQualificationType(amt) {
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
        def response = amt.CreateQualificationType(request)
        return response
    }

}
