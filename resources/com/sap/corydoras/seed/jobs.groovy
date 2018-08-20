#!/usr/bin/env groovy

/*
    seed job that generates all jobs in folder jobs
    this file is written in JobDSL and is called by the Jenkinsfile in stage "Generate Job"

    IMPORTANT: if you update this script, you'll have to approve it
               from In-process Script Approval available in Manage page of jenkins

    this file is called by the Jenkinsfile, props object is defined there
 */

basePath = ''
if (props['basePath']) {
    if (props['basePath'] == '.') {
        basePath = ''
    } else {
        basePath = props['basePath']
    }
    folder(basePath) {
        description """
    This folder is automaticaly generated. It contains pipeline unit jobs.<br>
    Jobs are imported from folders 'jobs' and 'jobdsl' under branch ${props.gitConfigJenkinsBranch} versioned at your github project
    """
    }
}

pipelineJobs.each { file ->
    try {
        def jobPath = file.name
        if (basePath != '') {
            jobPath = "${basePath}/${jobPath}"
        }
        pipelineJob(jobPath) {

            if (file.displayName){
                displayName "${file.displayName}"
            }

            //here we force job to get a description
            description "${file.description}"

            if (file.parameters) {
                parameters evaluate(file.parameters)
            }
            if (file.triggers) {
                triggers evaluate(file.triggers)
            }
            if (file.environmentVariables) {
                environmentVariables evaluate(file.environmentVariables)
            }
            logRotator {
                numToKeep(19)
            }
            wrappers {
                colorizeOutput()
                timeout { absolute(120) }
                timestamps()
            }
            if (file.authorizations) {
                authorization evaluate(file.authorizations)
            }
            properties {
                def baseProjectGithubUrl = props.gitRemoteUrl.replaceAll(/\.git$/, '').replaceAll(/^git@/, 'https://').replaceAll(/com\:/, 'com/')

                githubProjectUrl("${baseProjectGithubUrl}/blob/${props.gitConfigJenkinsBranch}/${file.path}")
                rebuild {
                    autoRebuild(file.authorizations ? false : true)
                }
                ownership {
                    primaryOwnerId(file.author)
                }
            }
            definition {
                if (file.content) {
                    cps {
                        script(file.content)
                    }
                } else {
                    cpsScm {
                        scm {
                            git {
                                remote {
                                    name 'origin'
                                    url props.gitRemoteUrl
                                }
                                extensions {
                                    pruneBranches()
                                }
                                branch props.gitConfigJenkinsBranch
                            }
                        }
                        scriptPath "${file.path}"
                    }
                }

            }
        }
    } catch (Exception err) {
        println "Error with file ${file.path}"
        println err
    }
}
