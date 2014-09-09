package tests

import org.arbusta.XmlHelper
import org.arbusta.domain.QualificationRequest
import org.arbusta.domain.Worker
import org.codehaus.groovy.grails.io.support.IOUtils

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
        if(!worker.save()) {
            worker.errors.each { it ->
                println it
            }
            throw new Exception("Cannot create dummy worker")
        }
        return worker
    }

    static def createDummyQualificationRequest() {

        new QualificationRequest(worker: )
    }
}
