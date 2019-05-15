package com.sap.corydoras.jenkins

class Changes {

    def changedFilesList() {
        output = sh returnStdout: true, script: '''#!/bin/bash
    changeSets=(`git diff-tree --no-commit-id --name-status -r HEAD`)
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

    def jobsToTrigger() {
        changedFiles = changedFilesList()
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
        arrFiles.unique()
    }
}
