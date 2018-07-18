#!/usr/bin/env groovy

def call(commit) {
    arrFiles = []
    path = '**/Jenkinsfile'
    if (!commit || !commit.GIT_URL) {
        error 'Cannot generate Jobs. Job must be triggered by a commit.\nIf you are running a multibranch job. Run Scan Multibranch Pipeline Now'
        return
    }

    findFiles(glob: path).each { file ->
        if (file.path.endsWith('/Jenkinsfile')) {
            arrFiles << [
                path: file.path,
                name: (file.path - ~/\/Jenkinsfile$/)
            ]
        }
    }

    def targetFile = 'seed/multijobs.groovy'
    def jobDefinition = libraryResource "com/sap/corydoras/${targetFile}"
    writeFile file: targetFile, text: jobDefinition


    jobDsl removedJobAction: 'DELETE',
            removedViewAction: 'DELETE',
            targets: targetFile,
            unstableOnDeprecation: true,
            additionalParameters: [
                pipelineJobs: arrFiles,
                props: [
                    gitRemoteUrl: commit.GIT_URL.replace(':', '/')
                        .replace('git@', 'https://')
                        .replace('https///', 'https://')
                ]
            ]
}
