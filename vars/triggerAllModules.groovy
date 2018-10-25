#!/usr/bin/env groovy


/**
 * List jobs that were generated with corydoras
 * and had a `jenkins.job.template` property set to look for Jenkinsfile
 */
private jobsToTrigger() {
    arrFiles = []
    jobs = []
    findFiles(glob: '**/*/Jenkinsfile').each { file ->
        def jobName = file.path - ~/\/Jenkinsfile$/
        jobs << jobName
    }    
      for (jobName in jobs) {
              arrFiles << jobName.replace('/', '-')
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
