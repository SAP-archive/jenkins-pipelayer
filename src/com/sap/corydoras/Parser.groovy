/**
 * @author Frederic Rousseau
 *
 * Get description, parameters, triggers, authorization from a pipeline declarative job
 *
 * Note: parameter value depending on variables wont work, there are not interpreted
 */
package com.sap.corydoras

/**
 * Class to look for properties from within pipeline files
 */
class Parser {

    Extractor extractor

    Parser() {
        extractor = new Extractor()
    }

    /**
     * extract content of a block between brackets
     * @param  name    name of the block. ie: triggers { }
     * @param  content the whole file
     * @return         the inner content of the block
     */
    def extractBlock(name, content) {
        def extract = content.find(/(?msi)\s*?${name}\s*\{.*/)
        if (!extract) return
        extractor.extract(extract, '{', '}')
    }

    def extractFirstComment(extract) {
        extractor.extract(extract, '/*', '*/', false)
    }
    /**
     * extract within a text, a variable of the form //@variable
     * @param  variableName  the string to extract
     * @param  content  content with/without displayName
     * @return          the display Name or empty string
     */
    def getCommentedVariable(variableName, content) {
        def pattern = /(?im)^\/\/@${variableName} *= */ //case insensitive
        try {
            return content.find(pattern + /(.*)$/).replaceAll(pattern, '')
        } catch (Exception ex) {
            return ''
        }
    }

    /**
     * extract displayName
     * @param  content  content with/without displayName
     * @return          the display Name or empty string
     */
    def getDisplayName(String content) {
        getCommentedVariable('displayName', content)
    }

    /**
     * extract jobName
     * @param  content  content with/without jobName
     * @return          the job Name, the one which would be given to access the job or empty string
     */
    def getJobName(String content) {
        getCommentedVariable('jobName', content)
    }

    /**
     * extract description from first comment block slash star
     * add br tags every newlines except if line ends with ">"
     * @param  content  content with multiline comment
     * @param  filePath path of the file to extract the comment from
     * @return          the first comment content
     */
    def getDescription(content, filePath) {
        try {
            extractFirstComment(content).trim().replaceAll(/>\s*\n\s*/, '>\n').replaceAll(/(?<!>)\s*\n\s*/, '<br>\n')
        } catch (Exception ex) {
            println 'could not extract description for file ' + filePath
            println ex
            println 'you must provide a comment at the beginning of the file in form /* my wonderful description */'
            ''
        }
    }

    /**
     * getTriggers submethod
     * @param  content  pipelineDSL file content
     * @param  filePath path to the pipelineDSL file
     * @return          code to be evaluated
     */
    def getTriggers(content, filePath) {
        try {
           def extract = extractBlock('triggers', content)
            if (extract) {
                println 'triggers found for file ' + filePath
                extract = extract.replaceAll(/pollSCM\s*\(/, 'scm(')
                if (extract.indexOf('upstreamProjects') != -1) {
                    def match = extract.substring(extract.indexOf('upstreamProjects'), extract.size() - 1)
                    def jobs = extractor.extract(match, '\'', '\'', false).split(',')
                    def statusArray = extract.findAll(/threshold\s*?:\s*?hudson\.model\.Result\.(\w+)/)
                    if (statusArray.size() > 0) {
                        def newExtract = ''
                        def status = statusArray[0].replaceAll(/threshold\s*?:\s*?hudson\.model\.Result\./, '')
                        jobs.each {
                            newExtract += "upstream('${it}', '${status}')\n"
                        }
                        extract = extract
                            .replaceAll(/upstream\(.*\n/, '')
                        println extract
                        extract += newExtract.replaceAll(/\n$/, '')
                    }
                }
            }
            "return {\n${extract.trim()}\n}"
        } catch (Exception ex) {
            println 'could not extract triggers for file ' + filePath
            println ex
            null
        }
    }

    /**
     * getEnvironmentVariables submethod
     * @param  content  pipelineDSL file content
     * @param  filePath path to the pipelineDSL file
     * @return          code to be evaluated
     */
    def getEnvironmentVariables(content, filePath) {
        try {
            def extract = extractBlock('environment', content)
            def textScript = 'return {\n'
            if (extract) {
                println 'environment variables found for file ' + filePath
                extract.split(/\s*?\n/).each { line ->
                    if (line.indexOf('=') != -1) {
                        def attrKeyValue = line.trim().split(/=/)
                        textScript += "env('${attrKeyValue[0]}', ${attrKeyValue[1]})\n"
                    }
                }
            }
            textScript += '}'
            textScript
        } catch (Exception ex) {
            println 'could not extract environment variables for file ' + filePath
            println ex
            null
        }
    }

    /**
     * extract matrix based authorization
     * @param  content  pipelineDSL file content
     * @param  filePath path to the pipelineDSL file
     * @return          code to be evaluated
     */
    def getAuthorizations(content, filePath) {
        try {
            def extract = extractBlock('options', content)
            extract = extract ? extract.find(/(?msi)authorizationMatrix\s*?\(.*\)/) : extract
            def textScript = 'return {\n'
            if (extract) {
                println 'authorizationMatrix found for file ' + filePath
                extract.substring(0, extract.indexOf(')')).findAll(/['"](.*?)['"]/).each { line ->
                    textScript += "permission(${line})\n"
                }
            }
            textScript += '}'
            textScript
        } catch (Exception ex) {
            println 'could not extract options for file ' + filePath
            throw ex
        }
    }

    /**
     * remove extension from fileName
     * @param  fileName a filename
     * @return          name of file without extension
     */
    def getBaseName(fileName) {
        def pos = fileName.lastIndexOf('.')
        def baseName
        if (pos > 0) {
            baseName = fileName.substring(0, pos)
        } else {
            baseName = fileName
        }
        baseName
    }

    /**
     * Convert pipelineDSL parameter type to JobDSL type
     * @param  line the full parameter description
     * @return      a JobDSL parameter type
     */
    def extractType(line) {
        //println "extract type from line: ${line}"
        def paramType
        switch (line) {
            case ~/(?msi)^\s*?string\s*?\(.*/:
                paramType = 'stringParam'
                break
            case ~/(?msi)^\s*?booleanParam\s*?\(.*/:
                paramType = 'booleanParam'
                break
            case ~/(?msi)^\s*?choice\s*?\(.*/:
                paramType = 'choiceParam'
                break
            case ~/(?msi)^\s*?password\s*?\(.*/:
                paramType = 'nonStoredPasswordParam'
                break
            default:
                break
        }
        paramType
    }

    /**
     * extract attribute value within pipelineDSL parameter
     * @param  parameter the attribute name
     * @param  line      a line of parameter.
     *                   ie: string(name: "BRANCH", description: 'branch', defaultValue: "x" + "y")
     * @return           parameter value
     */
    def extractAttributeValue(parameter, line) {
        //println "extract parameter ${parameter} from line: ${line}"
        //extract param until quote+comma or parenthesis+newline
        def paramStrWithAttributeName = line.find(/(?msi)${parameter}\s*?:\s*?(?:(?!['\"]\s*?,|\)\n).)*/)
        if (paramStrWithAttributeName) {
            def paramStrWithoutAttributeName = paramStrWithAttributeName.replaceAll(/,\s*[\'"]?.*/, '')
                //remove trailing parenthesis is there is and remove parameter name to only get the value
                .replaceAll(/\)$/, '').replaceAll(/${parameter}\s*?:\s*?/, '').trim()
            def paramStr = paramStrWithoutAttributeName.replaceAll(/[\'"]\s*\+\s*['\"]/, '')
            //add last quote when first one is already here due to regex
            if ((paramStr[0] == '\'' || paramStr[0] == '"')
                && paramStr[paramStr.length() - 1] != '\'' && paramStr[paramStr.length() - 1] != '"') {
                paramStr += paramStr[0]
            }
            //remove quote if only one let by regex
            if (paramStr == "\'" || paramStr == '"') {
                ''
            } else {
                paramStr
            }
        } else {
            ''
        }
    }

    /**
     * getParameters submethod
     * @param  content  pipelineDSL file content
     * @param  filePath path to the pipelineDSL file
     * @return          an array of hashtables [[:type, :name, :defaultValue, :description]]
     */
    def extractParameters(content, filePath) {
        try {
            def parameters = []
            def extract = extractBlock('parameters', content)
            if (extract) {
                println 'parameters found for file ' + filePath
                if (extract.indexOf('}') != -1) {
                    extract = extract.substring(
                        extract.indexOf('{') + 1,
                        extract.lastIndexOf('}') - 1
                    ).trim()
                }
                //split at each `) \n`
                extract.split(/\)\s*?\n/).each { line ->
                    def paramType = extractType(line)
                    def name = extractAttributeValue('name', line)
                    def defaultValue = extractAttributeValue(
                        paramType == 'choiceParam' ? 'choices' : 'defaultValue',
                        line
                    )
                    def description = extractAttributeValue('description', line)
                    if (paramType) {
                        parameters << [
                            type: paramType,
                            name: name,
                            defaultValue: defaultValue,
                            description: description
                        ]
                    }
                }
            }
            parameters
        } catch (Exception ex) {
            println 'could not extract params for file ' + filePath
            println ex
            null
        }
    }

    /**
     * Create JobDSL parameters from ones declared in a declarative dsl file
     * @param  content a pipelineDSL file content
     * @param  filePath the path of the pipelineDSL file
     * @return          a file name
     */
    def getParameters(content, filePath) {
        try {
            def parameters = extractParameters(content, filePath)
            if (parameters.size() == 0) return

            def textScript = 'return {\n'
            parameters.each { p ->
                if (!p.defaultValue?.trim()) {
                    p.defaultValue = "''"
                }
                if (!p.description?.trim()) {
                    p.description = "''"
                }
                if (p.type == 'choiceParam') {
                    def choices = ''
                    def val = "${p.defaultValue}".replaceAll(/^['\"]/, '').replaceAll(/['\"]$/, '')
                    def lineSeparator = null
                    if (val.indexOf('\\n') > -1) {
                        lineSeparator = '\\\\n'
                    } else if (val.indexOf('\n') > -1) {
                        lineSeparator = '\n'
                    }
                    if (lineSeparator) {
                        val.split(lineSeparator).each { choice ->
                            choices += "\'${choice}\',"
                        }
                        choices = choices.substring(0, choices.length() - 1)
                    } else {
                        choices = val
                    }
                    textScript += "${p.type}(${p.name}, [${choices}], ${p.description})\n"
                } else if (p.type == 'nonStoredPasswordParam') {
                    textScript += "${p.type}(${p.name}, ${p.description})\n"
                } else {
                    textScript += "${p.type}(${p.name}, ${p.defaultValue}, ${p.description})\n"
                }
            }
            textScript += '}'
            if (parameters.size() > 0) {
                textScript
            }
        } catch (Exception ex) {
            println 'could not extract parameters for file ' + filePath
            println ex
            null
        }
    }

}
