#!/usr/bin/env groovy

/**
 * List commited files that were added, deleted or modified since last commit
 */
private getChangedFiles() {
    output = sh returnStdout: true, script: '''#!/bin/bash
changeSets=(`git diff-tree --no-commit-id --name-status -r HEAD HEAD^1`)
for(( i=0; i<${#changeSets[@]}; i++))
do
  if [[ "${changeSets[$i]}" =~ [ACDMRT] ]]
  then
    echo ${changeSets[$i+1]}
  fi
done
'''
    output.replace('\r', '').split('\n')
}

/**
 * List jobs that were generated with corydoras
 * and had a `jenkins.job.template` property set to look for Jenkinsfile
 */
private jobsToTrigger() {
    changedFiles = getChangedFiles()
    print 'changedFiles:'
    print changedFiles.toString()
    arrFiles = []
    jobs = []
    findFiles(glob: '**/*/Jenkinsfile').each { file ->
        def jobName = file.path - ~/\/Jenkinsfile$/
        jobs << jobName
    }
    for (changedFile in changedFiles) {
        for (jobName in jobs) {
            if (changedFile.startsWith(jobName)) {
                arrFiles << jobName.replace('/', '-')
                break
            }
        }
    }
    print 'arrFile'
    print arrFiles.toString()
    arrFiles.unique()
}

@NonCPS
private scheduleBuild(String downStreamProjectName) {
    Jenkins.instance.getItemByFullName(downStreamProjectName).scheduleBuild()
}

def call(jenkinsContext, folder = '', multibranch = false) {
    for (job in jobsToTrigger()) {
        if (folder != '') {
            job = "${folder}/${job}"
        }
        jenkinsContext.println "build job ${job}"
        try {
            scheduleBuild(job)
        } catch(Exception e) {
            println e
            println "Exception on job ${job} (doesn't exist?)"
        }
    }
}
