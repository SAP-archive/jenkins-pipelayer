#!/usr/bin/env groovy

import groovy.transform.InheritConstructors
import com.sap.corydoras.Parser

@InheritConstructors
class NoTemplateException extends Exception {}

private infoMessage(localPath, filePath, configPath) {
    def location = localPath ? "${localPath}/${filePath}" : filePath
"""//////////////////////////////////////////
// This file was automatically generated from template: ${location} with file properties: ${configPath}
// Please update this file from github and not directly in jenkins
//////////////////////////////////////////
"""
}

private insureNoShebang(fileContent) {
    if (fileContent.startsWith('#!')) {
        return "//${fileContent}"
    }
}

private processTemplate(file, localPath) {
    def properties = readProperties file: file.path
    println file.path
    println properties
    if (!properties['jenkins.job.template']) {
        throw new NoTemplateException()
    }
    def filePath = properties['jenkins.job.template']
    def fileContent = sh returnStdout: true, script: "cat ${filePath}"
    properties.each { key, value ->
        fileContent = fileContent.replace(/{{${key}}}/, value)
    }

    // note: we comment the first line in case a shebang is present
    fileContent = infoMessage(localPath, filePath, file.path) + insureNoShebang(fileContent)
    def fileName = properties['jenkins.job.name']
    if (!fileName) {
        fileName = filePath.replaceFirst(~/\.[^\.]+$/, '').split('/')[-1]
    }

    return [filePath, fileName, fileContent]
}

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    arrFiles = []

    def parser = new Parser()
    def path = ''

    def resourcesDestination = ''

    if (!config.path) {
        path = config.useTemplate ? 'config/*.properties' : 'jobs/**/*.groovy'
    } else {
        path = config.path
    }
    
    if (!config.gitRemoteUrl || !config.gitConfigJenkinsBranch) {
        error '''gitRemoteUrl and gitConfigJenkinsBranch are mandatory parameters. you can get them from scm step.
commit = checkout scm
gitRemoteUrl = commit.GIT_URL
gitConfigJenkinsBranch = commit.GIT_BRANCH
'''
        return
    }
    config.gitConfigJenkinsBranch = config.gitConfigJenkinsBranch.replaceAll(/^origin\//, '')
    
    //copy src to jenkins
    if (config.copySrc) {
        resourcesDestination = "$JENKINS_HOME/job_resources/$destination"
        sh "mkdir -p $resourcesDestination"
        sh "cp -r * $resourcesDestination"
    }

    findFiles(glob: path).each { file ->
        def name = null
        def filePath = null
        def fileContent = ''
        def fileDescription = []

        if (config.useTemplate) {
            try {
                (filePath, name, fileContent) = processTemplate(file, config.localPath)
                if (resourcesDestination) {
                    fileContent = fileContent.replace(/{{sources.directory}}/, resourcesDestination)
                }
            } catch (NoTemplateException exception) {
                println "You did not specify a template in $file.path, pass"
            }
        } else {
            filePath = file.path
            fileContent = sh returnStdout: true, script: "cat ${filePath}"
        }

        arrFiles << [
            name: name || parser.getBaseName(file.name),
            content: config.withContent || config.useTemplate ? fileContent : '',
            path: filePath,
            displayName: parser.getDisplayName(fileContent),
            description: parser.getDescription(fileContent, filePath),
            triggers: parser.getTriggers(fileContent, filePath),
            parameters: parser.getParameters(fileContent, filePath),
            authorizations: parser.getAuthorizations(fileContent, filePath),
            environmentVariables: parser.getEnvironmentVariables(fileContent, filePath),
            author: sh(returnStdout: true, script: "git log --format=%an ${filePath} | tail -1").trim()
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
                    basePath: config.destination ?: '',
                    gitRemoteUrl: config.gitRemoteUrl,
                    gitConfigJenkinsBranch: config.gitConfigJenkinsBranch,
                    localPath: config.localPath ?: ''
                ]
            ]
}
