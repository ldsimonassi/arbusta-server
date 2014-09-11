package tests

import grails.validation.ValidationException
import org.arbusta.XmlHelper
import org.arbusta.domain.QualificationAssignment
import org.arbusta.domain.QualificationRequest
import org.arbusta.domain.QualificationType
import org.arbusta.domain.Worker
import org.codehaus.groovy.grails.io.support.IOUtils

import java.sql.Timestamp

/**
 * Created by dsimonassi on 8/26/14.
 */
class TestsHelper {

    static int i;

    static def loadRequest(id) {
        def xml = IOUtils.createXmlSlurper().parse(new File("./test/integration/examples/${id}.xml"))
        def ret = XmlHelper.hashBuilder(xml)
        println "loadRequest:[${id}] [${ret}]"
        return ret
    }

    static def createDummyWorker() {
        def worker = new Worker(firstName: "Luis${i++}", lastName: "Simonassi", email: "ldsimonassi@gmail.com")
        if(!worker.save())
            throw new ValidationException("Cannot create dummy worker", worker.errors)
        return worker
    }

    static def createDummyQualificationRequest(qualificationType) {
        def worker = createDummyWorker()
        def qr = new QualificationRequest(worker:worker, qualificationType: qualificationType, test: "The test", answer: "The answer", submitTime: new java.sql.Timestamp(System.currentTimeMillis()), assignment: null)
        if(!qr.save()) throw new ValidationException("Unable to create a dummy Qualification Request", qr.errors)
        return qr
    }
}
