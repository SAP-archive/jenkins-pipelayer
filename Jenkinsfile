#!/usr/bin/env groovy

pipeline {
    agent {
        docker {
            image 'docker.wdf.sap.corp:50000/eos/gradle'
            label 'linux_x64'
            alwaysPull true
        }
    }
    options {
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '100'))
        timeout(time: 30, unit: 'MINUTES')
    }
    stages {
        stage('Lint') {
            steps {
                lock(resource: "${env.JOB_NAME}/10", inversePrecedence: true) {
                    milestone 10
                    script {
                        commit = checkout scm
                        try {
                            sh 'gradle codenarcMain'
                        } catch (error) {
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                }
            }
        }
    }
}
