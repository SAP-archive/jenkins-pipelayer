#!/usr/bin/env groovy

import groovy.transform.InheritConstructors
import com.sap.corydoras.Parser

@InheritConstructors
class NoTemplateException extends Exception {}

private infoMessage(localPath) {
    def location = localPath ? "${localPath}/${filePath}" : filePath
    """
//////////////////////////////////////////
// This file was automatically generated from template: ${location} with file properties: ${file}
// Please update this file from github and not directly in jenkins
//////////////////////////////////////////
"""
}

private insureNoShebang(fileContent) {
    if (!fileContent.startsWith('!#')) {
        return fileContent
    } else {
        fileContentArray = fileContent.split('\n')
        return fileContentArray[1..fileContentArray.size-1].join('')
    }
}

private processTemplate(file, localPath) {
    def properties = readProperties file: file.path
    if (!properties['jenkins.job.template']) {
        throw new NoTemplateException()
    }
    def filePath = properties['jenkins.job.template']
    def fileContent = sh returnStdout: true, script: "cat ${filePath}"
    properties.each { key, value ->
        fileContent = fileContent.replace(/{{${key}}}/, value)
    }

    // note: we comment the first line in case a shebang is present
    fileContent = infoMessage(localPath) + insureNoShebang(fileContent)
    def fileName = properties['jenkins.job.name']
    if (!fileName) {
        fileName = filePath.replaceFirst(~/\.[^\.]+$/, '').split('/')[-1]
    }

    return [filePath, fileName, fileContent]
}

def call(String path, String destination, commit, additionalParameters) {
    arrFiles = []
    def parser = new Parser()

    def resourcesDestination = ''

    if (!path) {
        if (additionalParameters.useTemplate) {
            path = 'config/*.properties'
        } else {
            path = 'jobs/**/*.groovy'
        }
    }

    if (!commit || !commit['GIT_URL'] || !commit['GIT_BRANCH']) {
        error 'Cannot generate Jobs. Job must be triggered by a commit.\nIf you are running a multibranch job. Run Scan Multibranch Pipeline Now'
        return
    }

    //copy src to jenkins
    if (additionalParameters.copySrc) {
        sourcesDestination = "$JENKINS_HOME/job_resources/$destination"
        sh "mkdir -p $sourcesDestination"
        sh "cp -r * $sourcesDestination"
    }

    findFiles(glob: path).each { file ->

        def name = ''
        def filePath = ''
        def fileContent = ''

        if (additionalParameters.useTemplate) {
            try {
                (filePath, name, fileContent) = processTemplate(file, additionalParameters.localPath)
                if (sourcesDestination) {
                    fileContent = fileContent.replace(/{{sources.directory}}/, sourcesDestination)
                }
            } catch (NoTemplateException exception) {
                println "You did not specify a template in $file.path, pass"
            }
        } else {
            fileContent = sh returnStdout: true, script: "cat ${file.path}"
            filePath = file.path
        }

        arrFiles << [
            path: filePath,
            name: name ?: parser.getBaseName(file.name),
            displayName: parser.getDisplayName(fileContent),
            description: parser.getDescription(fileContent, filePath),
            triggers: parser.getTriggers(fileContent, filePath),
            parameters: parser.getParameters(fileContent, filePath),
            authorizations: parser.getAuthorizations(fileContent, filePath),
            environmentVariables: parser.getEnvironmentVariables(fileContent, filePath),
            author: sh(returnStdout: true, script: "git log --format=%an ${filePath} | tail -1").trim(),
            content: additionalParameters.withContent || additionalParameters.useTemplate ? fileContent : ''
        ]
    }

    def targetFile = 'seed/jobs.groovy'
    def jobDefinition = libraryResource "com/sap/corydoras/${targetFile}"
    writeFile file: targetFile, text: jobDefinition

    jobDsl removedJobAction: 'DELETE',
            removedViewAction: 'DELETE',
            targets: targetFile,
            unstableOnDeprecation: true,
            additionalParameters: [
                pipelineJobs: arrFiles,
                props: [
                    basePath: destination,
                    gitRemoteUrl: "${commit.GIT_URL}",
                    gitConfigJenkinsBranch: "${commit.GIT_BRANCH.replaceAll(/^origin\//, '')}",
                    localPath: additionalParameters.localPath
                ]
            ]
}
