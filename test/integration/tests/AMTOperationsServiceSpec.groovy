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



    /*
    def "create simple hit without hit_type" () {
        setup:
            def request = TestsHelper.loadRequest("")
        when:


    }

    */
}
