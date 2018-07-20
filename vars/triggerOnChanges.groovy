#!/usr/bin/env groovy

private getChangedFiles() {
    output = sh returnStdout: true, script: '''#!/bin/bash
changeSets=(`git diff-tree --no-commit-id --name-status -r HEAD`)
for(( i=0; i<${#changeSets[@]}; i++))
do
  if [ ${changeSets[$i]} == "A" ] || [ ${changeSets[$i]} == "C" ] || [ ${changeSets[$i]} == "D" ] || [ ${changeSets[$i]} == "M" ] || [ ${changeSets[$i]} == "R" ] || [ ${changeSets[$i]} == "T" ] 
  then
    echo ${changeSets[$i+1]}
  fi
done
'''
    output.replace('\r', '').split('\n')
}

private jobsToTrigger() {
    changedFiles = getChangedFiles()
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

@NonCPS
private scanRepo(String downStreamProjectName) {
    Jenkins.instance.getItemByFullName(downStreamProjectName).scheduleBuild()
}

def call(jenkinsContext) {
    for (job in jobsToTrigger()) {
        jenkinsContext.println "build job ${job}"
        scanRepo(job)
    }
}
