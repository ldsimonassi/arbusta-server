import org.arbusta.domain.*
import grails.validation.ValidationException
import java.sql.Timestamp

def typeB = new HitType(
          title: "La guitarra esta afinada?",
          description: "blabla",
          reward: 0.1,
          assignmentDurationInSeconds: 3600,
          autoApprovalDelayInSeconds : 3600,
          keywords: "language, math")

def typeA = new HitType(
          title: "El queso esta rico?",
          description: "blabla",
          reward: 0.1,
          assignmentDurationInSeconds: 3600,
          autoApprovalDelayInSeconds : 3600,
          keywords: "language, math")

[typeA, typeB].each {
    if(!it.save()) throw new ValidationException("", it.errors)
}

def hit = new Hit(hitType: typeA,
        question: "",
        requesterAnnotation: "annotation",
        lifetimeInSeconds: 192831,
        maxAssignments: 3,
        creationTime: new Timestamp(System.currentTimeMillis())
);

[hit].each {
    if(!it.save()) throw new ValidationException("", it.errors)
}

println "Before change...: ${hit.id} has ${hit.hitType.id}"

hit.setHitType(typeB)



println "After change....: ${hit.id} has ${hit.hitType.id}"

[hit].each {
    if(!it.save(flush:true)) throw new ValidationException("", it.errors)
}

println "After save......: ${hit.id} has ${hit.hitType.id}"

hit.refresh()

println "After refresh...: ${hit.id} has ${hit.hitType.id}"




