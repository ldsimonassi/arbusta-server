package org.arbusta.services

import org.arbusta.domain.QualificationType
import org.arbusta.domain.QualificationRequirement
import org.arbusta.domain.HitType
import grails.transaction.Transactional
import grails.validation.ValidationException

@Transactional
class AMTOperationsService {

    static scope = "singleton"

    def CreateHIT(request) {
        //TODO Implement
    }

    def RegisterHITType(request) {
        //TODO Use Actual Parameters in the request
        def q = QualificationType.get(1)
        def type = new HitType(title: "Categorizacion de imagenes", description: "Identificar la categoría que mejor representa la imagen", reward: 0.1, assignmentDurationInSeconds: 3600L, keywords: "categorias, imagenes, trabajo" )

        type.addToQualificationRequirements(new QualificationRequirement(qualificationType: q, comparator: "GreaterThan", integerValue: 7))

        type.save()

        println "Saved ${type.id}"

        type.errors.each {
            println it
        }
    }

    def CreateQualificationType(request) {
        def q = new QualificationType(
                name: request.Name, description: request.Description,
                keywords: request.Keywords, creationTime: new java.sql.Timestamp(System.currentTimeMillis()),
                retryDelayInSeconds: request.RetryDelayInSeconds,
                qualificationTypeStatus: request.QualificationTypeStatus,
                test: request.Test, answerKey: request.AnswerKey,
                testDurationInSeconds:request.TestDurationInSeconds)

        q.save()

        if(q.hasErrors()) throw new ValidationException(q.errors)

        def ret = [:]
        ret.QualificationType = [:]
        ret.QualificationType.QualificationTypeId = q.id
        ret.QualificationType.CreationTime = q.creationTime
        ret.QualificationType.Name = q.name
        ret.QualificationType.Description = q.description
        ret.QualificationType.QualificationTypeStatus = q.qualificationTypeStatus
        ret.QualificationType.AutoGranted = q.autoGranted
        return ret
    }
}
