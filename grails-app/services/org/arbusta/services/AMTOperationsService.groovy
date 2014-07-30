package org.arbusta.services

import org.arbusta.domain.Qualification
import org.arbusta.domain.HitType
import grails.transaction.Transactional

@Transactional
class AMTOperationsService {

    def RegisterHITType(request) {
        def type = new HitType(title: "Categorizacion de imagenes", description: "Identificar la categoría que mejor representa la imagen", reward: 0.1, assignmentDurationInSeconds: 3600L, keywords: "categorias, imagenes, trabajo" )

        def qualif = Qualification.get(1)

        type.addToQualificationRequirements(new QualificationRequirement(qualification: qualif, comparator: "GreaterThan", integerValue: 7))

        type.save()

        println "Saved ${type.id}"

        type.errors.each {
            println it
        }
    }

    def CreateQualification(request) {
        def qualif = new Qualification(title: "Idioma Ingles", description: "Conocer el idioma ingles", keywords: "ingles, idioma",
                                qualificationTypeStatus: "Active", retryDelayInSeconds: 24L*7*3600, testDurationInSeconds:3600L)
        qualif.save()
    }
}
