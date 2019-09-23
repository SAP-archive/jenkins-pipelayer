#!/usr/bin/env groovy

pipeline {
    agent {
        docker any
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '100'))
        timeout(time: 30, unit: 'MINUTES')
    }
    stages {
        stage('Test') {
            steps {
                sh 'echo test'
            }
        }
    }
}
