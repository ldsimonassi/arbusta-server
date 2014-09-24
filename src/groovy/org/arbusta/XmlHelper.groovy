package org.arbusta
/**
 * Created by dsimonassi on 9/7/14.
 */
class XmlHelper {

    def static hashBuilder(def xml) {
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
