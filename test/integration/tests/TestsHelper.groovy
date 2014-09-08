package tests

import org.arbusta.XmlHelper
import org.codehaus.groovy.grails.io.support.IOUtils

/**
 * Created by dsimonassi on 8/26/14.
 */
class TestsHelper {
    static def loadRequest(id) {
        def xml = IOUtils.createXmlSlurper().parse(new File("./test/integration/examples/${id}.xml"))
        def ret = XmlHelper.hashBuilder(xml)
        println "loadRequest:[${id}] [${ret}]"
        return ret
    }
}
