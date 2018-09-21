import spock.lang.Specification

import com.sap.corydoras.Parser

class OtherParserSpec extends Specification {

    String scriptContent2 = this.getClass().getResource('jenkinsfile1').text
    String scriptPath2 = '/dummy/path/jenkinsfile1'
    Parser parser = new Parser()

    def 'correctly parse no description'() {
        expect:
            parser.getDescription(scriptContent2, scriptPath2) == ''
    }
    def 'correctly parse triggers'() {
        expect:
            parser.getTriggers(scriptContent2, scriptPath2) == 'return {\nupstream(\'job1\', \'SUCCESS\')\nupstream(\'job2\', \'SUCCESS\')\n}'
    }
}
