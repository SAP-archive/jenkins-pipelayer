#!/usr/bin/env groovy

pipeline {
    agent {
        docker {
            image 'gradle'
        }
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '100'))
        timeout(time: 30, unit: 'MINUTES')
    }
    stages {
        stage('Lint') {
            steps {
                lock(resource: "${env.JOB_NAME}/10", inversePrecedence: true) {
                    milestone 10
                    checkout scm
                    sh 'gradle codenarcMain'
                }
            }
        }
    }
}
