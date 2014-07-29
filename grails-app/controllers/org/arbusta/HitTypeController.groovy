package org.arbusta

class HitTypeController {

  def amtService

  //def scaffold = HitType
  def index() {
    def soap = request.XML

    println "Initiating XML Parsing"

    //Check contains Body
    if(!soap.children().collect { it.name() }.contains("Body"))
      throw new Exception("Body not contained");

    println "Body detected"

    def body = soap.Body

    // Look for operations to execute
    body.children().each { operation ->
      def operationName = operation.name()
      println "Operation: ${operationName}, parsing Request tag"
      def requestData = hashBuilder(operation.Request)

      // call operationName action
      def responseData = this."$operationName"(requestData)

    }
    render "OK"
  }

  def CreateHIT(def req) {
    println "CreateHIT method called"
  }

  def RegisterHITType(def req) {
    println "RegisterHITType method called"
    println "AutoApprovalDelayInSeconds: ${req.AutoApprovalDelayInSeconds}"
    println "QualificationRequirement: ${req.QualificationRequirement}"
  }


  def hashBuilder(def xml) {
	  def ret = [:]
	  xml.children().each { child ->
		  def name = child.name()
		  def value 

		  if(child.children().size() == 0)
			  value = child.text();
		  else
			  value = hashBuilder(child);
		
		  if(ret[name] != null) {
			  if(ret[name] instanceof java.util.ArrayList) {
				  ret[name].add(value)
			  } else {
				  def array = []
				  array.add(ret[name])
				  array.add(value)
				  ret[name] = array
			  }
		  } else {
			  ret[name] = value
		  }

	  }
	  return ret;
  }
}
