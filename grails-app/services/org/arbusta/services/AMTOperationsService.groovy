package org.arbusta.services

import org.arbusta.domain.QualificationType
import org.arbusta.domain.QualificationRequirement
import org.arbusta.domain.HitType
import grails.transaction.Transactional

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
        //TODO Use Actual Parameters in the request
        def q = new QualificationType(title: "Idioma Ingles", description: "Conocer el idioma ingles", keywords: "ingles, idioma", creationTime: new java.sql.Timestamp(System.currentTimeMillis()),
                qualificationTypeStatus: "Active", retryDelayInSeconds: 24L*7*3600, testDurationInSeconds:3600L)

        q.save()

        q.errors.each {
            println it
        }

        return q
    }
}
