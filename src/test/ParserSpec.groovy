import spock.lang.Specification

import com.sap.corydoras.Parser

class ParserSpec extends Specification {

    String scriptContent = this.getClass().getResource('dummyScript').text
    String scriptPath = '/dummy/path/dummyScript'
    Parser parser = new Parser()

    def 'correctly extract block with given name'() {
        expect:
            parser.extractBlock('dummyBlock', scriptContent) == "\n        dummy = 'dummy'\n    "
    }
    def 'correctly parse jobName'() {
        expect:
            parser.getJobName(scriptContent) == 'dummy script'
    }
    def 'correctly parse displayName'() {
        expect:
            parser.getDisplayName(scriptContent) == 'dummy script'
    }
    def 'correctly parse description'() {
        expect:
            parser.getDescription(scriptContent, scriptPath) == 'This is a multiline<br>\ndescription that<br>\ncan contain anything,<br>\nnumbers 123456789,<br>\nsymbols $%:;*\\/,<br>\nEVERYTHING'
    }
    def 'correctly parse triggers'() {
        expect:
            parser.getTriggers(scriptContent, scriptPath) == 'return {\ncron(* * * * *)\n}'
    }
    def 'correctly parse environment variables'() {
        expect:
            parser.getEnvironmentVariables(scriptContent, scriptPath) == "return {\nenv('envVar ',  'my_env_variable')\n}"
    }
    def 'correctly parse name'() {
        expect:
            parser.getBaseName('dummyScript.groovy') == 'dummyScript'
    }
    def 'correctly parse parameters'() {
        expect:
            parser.getParameters(scriptContent, scriptPath) == "return {\nstringParam('DEPLOY_ENV', 'staging', '')\nbooleanParam('DEBUG_BUILD', true, '')\nchoiceParam('CHOICES', ['one','two','three'], '')\nnonStoredPasswordParam('PASSWORD', 'A secret password')\n}"
    }
}
