import spock.lang.Specification

import com.sap.corydoras.Parser

class ParserNoCommentSpec extends Specification {

    String scriptContent2 = this.getClass().getResource('jenkinsfile1').text
    String scriptPath2 = '/dummy/path/jenkinsfile1'
    Parser parser = new Parser()

    def 'correctly parse no description'() {
        expect:
            parser.getDescription(scriptContent2, scriptPath2) == ''
    }
}
