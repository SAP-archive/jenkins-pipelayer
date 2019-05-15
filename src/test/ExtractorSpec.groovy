import spock.lang.Specification

import com.sap.corydoras.Extractor

class ExtractorSpec extends Specification {

    String scriptContent2 = this.getClass().getResource('jenkinsfile1').text
    String scriptPath2 = '/dummy/path/jenkinsfile1'
    Extractor extractor = new Extractor()

    def 'correctly extract things'() {
        expect:
            extractor.extract('xxxxxtoto mono poto  }', 'xxxxxtoto', 'poto') == ' mono '
    }
    def 'correctly extract things2'() {
        expect:
            extractor.extract('xxxxxtoto \'mono\' \'mono\' poto  }', '\'', '\'', false) == 'mono'
    }
}
