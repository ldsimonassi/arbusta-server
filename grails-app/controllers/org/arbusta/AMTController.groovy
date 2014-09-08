package org.arbusta


import org.arbusta.services.*
import org.arbusta.XmlHelper

class AMTController {

    AMTOperationsService AMTOperationsService

    def index() {
        def soap = request.XML
        println "Initiating XML Parsing"

        //Check contains Body
        if(!soap.children().collect { it.name() }.contains("Body"))
            throw new Exception("Body not contained");

        println "Body detected"

        def body = soap.Body


        def responses = []
        // Look for operations to execute
        body.children().each { operation ->
            def operationName = operation.name()
            println "Operation: ${operationName}, parsing Request tag"
            def requestData = XmlHelper.hashBuilder(operation.Request)
            // call operationName method
            def individualResponse = AMTOperationsService."$operationName"(requestData)
            responses.add(individualResponse)
            println individualResponse
        }
        render responses.toString()
    }

  def CreateHIT(def req) {
    println "CreateHIT method called"
  }

  def RegisterHITType(def req) {
    println "RegisterHITType method called"
    println "AutoApprovalDelayInSeconds: ${req.AutoApprovalDelayInSeconds}"
    println "QualificationRequirement: ${req.QualificationRequirement}"
  }
}