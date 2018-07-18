#!/usr/bin/env groovy

/*
    seed job that generates multibranch pipeline jobs from jenkinsfile(s) in subfolders
    this file is written in JobDSL and is called by the Jenkinsfile in stage "Generate Job"

    IMPORTANT: if you update this script, you'll have to approve it
               from In-process Script Approval available in Manage page of jenkins
 */


pipelineJobs.each { file ->
    try {
        folderPath = file.path - ~/\/Jenkinsfile$/
        multibranchPipelineJob(file.name.replace('/', '-')) {
            displayName file.name.replace('/', ' - ')
            branchSources {
                git {
                    remote(props.gitRemoteUrl)
                    includes("${folderPath}/.*")
                }
                //discover branches. to be replaced when issue JENKINS-45504 is closed
                configure {
                    def traits = it / sources / data / 'jenkins.branch.BranchSource' / source / traits
                    traits << 'jenkins.plugins.git.traits.BranchDiscoveryTrait' {
                        strategyId(3) // detect all branches
                    }
                }
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath file.path
                }
            }
            orphanedItemStrategy {
                discardOldItems {
                    numToKeep(100)
                }
            }
            triggers {
                periodic(1)
            }
        }
    } catch (Exception err) {
        println "Error with file ${file.path}"
        println err
    }
}
