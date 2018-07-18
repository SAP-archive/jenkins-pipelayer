#!/usr/bin/env groovy

import com.sap.corydoras.Parser

def call(String path, commit) {
    arrFiles = []
    def parser = new Parser()
    if (!path) {
        path = 'config/*.properties'
    }
    // if (!commit || !commit.GIT_URL || !commit.GIT_BRANCH) {
    //     error 'Cannot generate Jobs. Job must be triggered by a commit.\nIf you are running a multibranch job. Run Scan Multibranch Pipeline Now'
    //     return
    // }

    findFiles(glob: path).each { propertyFile ->
        def properties = readProperties file: propertyFile.path
        if (properties['jenkins.job.template']) {
            def fileContent = sh returnStdout: true, script: "cat ${properties['jenkins.job.template']}"
            properties.each { key, value ->
                fileContent = fileContent.replace(/{{${key}}}/, value)
            }
            fileContent = "//template: ${properties['jenkins.job.template']}  properties: ${propertyFile}" + fileContent

            def filePath = properties['jenkins.job.template']
            arrFiles <<  [
                // Template path
                content: fileContent,

                // name of the job
                name: properties['jenkins.job.name'],

                // get description from the first comment /* */ of the file
                description: utils.getDescription(fileContent, filePath),

                // copy as is triggers definition
                triggers: utils.getTriggers(fileContent, filePath),


                // try to extract parameters in order to generate parameters with the job
                // normaly we would have to wait a first run
                // of the pipeline in order to generate the parameters.
                // Instead we create them in the jobdsl script
                parameters: utils.getParameters(fileContent, filePath),


                // extract autorization matrix rights
                authorizations: utils.getAuthorizations(fileContent, filePath),


                // extract environment variables
                environmentVariables: utils.getEnvironmentVariables(fileContent, filePath),


                // owner is creator of the file
                author: sh(returnStdout: true, script: "git log --format=%an ${filePath} | tail -1").trim()
            ]
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
                    // root folder to generate the pipeline jobs
                    basePath: destination,
                    // address to fetch the jobs
                    gitRemoteUrl: "${commit.GIT_URL}",
                    // branch the jenkins jobs are on
                    gitConfigJenkinsBranch: "${commit.GIT_BRANCH.replaceAll(/^origin\//, '')}"
                ]
            ]
}
