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

def fileDescription(parser, config, fileContent, file, propsName, propsFolder, propsFolderDescription) {
    if (propsName) {
        name = propsName
    } else {
        variableGetJobName = parser.getJobName(fileContent)
        if (variableGetJobName) {
            name = variableGetJobName
        } else {
            if (file.name == 'Jenkinsfile') {
                name = (file.path - ~/\/Jenkinsfile$/).replace('/', '-')
            } else {
                name = parser.getBaseName(file.name)
            }
        }
    }
    return [
        name: name,
        folder: propsFolder,
        folderDescription: propsFolderDescription,
        content: config.withContent ?: (config.useTemplate ? fileContent : ''),
        path: file.path,
        displayName: parser.getDisplayName(fileContent),
        description: parser.getDescription(fileContent, file.path),
        triggers: parser.getTriggers(fileContent, file.path),
        parameters: parser.getParameters(fileContent, file.path),
        authorizations: parser.getAuthorizations(fileContent, file.path),
        environmentVariables: parser.getEnvironmentVariables(fileContent, file.path),
        author: sh(returnStdout: true, script: "git log --format=%an ${file.path} | tail -1").trim()
    ]
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
    def destination = config.destination ?: ''

    if (!config.path) {
        path = config.useTemplate ? 'config/*.properties' : 'jobs/**/*.groovy'
    } else {
        path = config.path
    }
    
    if (!config.gitRemoteUrl || !config.gitConfigJenkinsBranch || config.gitRemoteUrl == "null" || config.gitConfigJenkinsBranch == "null") {
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
        resourcesDestination = "$JENKINS_HOME/job_resources/${destination}"
        sh "mkdir -p $resourcesDestination"
        sh "cp -r * $resourcesDestination"
    }

    findFiles(glob: path).each { file ->
        if (config.useTemplate) {
            try {
                def localPath = config.localPath
                def properties = readProperties file: file.path
                if (!properties['jenkins.job.template']) {
                    throw new NoTemplateException()
                }
                def filesTemplate = findFiles(glob: properties['jenkins.job.template'])
                def name = filesTemplate.size() > 1 ? null : properties['jenkins.job.name']
                filesTemplate.each { fileTemplate ->
                    def fileContent = sh returnStdout: true, script: "cat ${fileTemplate.path}"
                    properties.each { key, value ->
                        fileContent = fileContent.replace(/{{${key}}}/, value)
                    }
                    if (resourcesDestination) {
                        fileContent = fileContent.replace(/{{sources.directory}}/, resourcesDestination)
                    }
                    // note: we comment the first line in case a shebang is present
                    fileContent = infoMessage(localPath, fileTemplate.path, file.path) + insureNoShebang(fileContent)
                    arrFiles << fileDescription(parser, config, fileContent, fileTemplate, name, properties['jenkins.job.folder'], properties['jenkins.job.folder.description'])
                }
            } catch (NoTemplateException exception) {
                println "You did not specify a template in $file.path, pass"
            }
        } else {
            def fileContent = sh returnStdout: true, script: "cat ${file.path}"
            arrFiles << fileDescription(parser, config, fileContent, file, null, null, null)
        }
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
                    gitRemoteUrl: config.gitRemoteUrl,
                    gitConfigJenkinsBranch: config.gitConfigJenkinsBranch,
                    localPath: config.localPath ?: ''
                ]
            ]
}
