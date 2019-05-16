#!/usr/bin/env groovy
package com.sap.corydoras

/**
 * execute script on jenkins.
 * Note: double quotes " not supported or must be escaped from within the script
 */
class RemoteExecute {
    def self = null
    def credentialId = null

    RemoteExecute(jenkinsContext, credentialId) {
        this.self = jenkinsContext
        this.credentialId = credentialId
    }

    def runScript(scriptText) {
        this.self.withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: this.credentialId,
                    usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD']]) {
            def CRUMB = this.self.sh(returnStdout: true, script: """
                curl -k -u '${this.self.env.JENKINS_USER}':'${this.self.env.JENKINS_PASSWORD}' \
                '${this.self.env.JENKINS_URL}crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)'
            """).trim()
            def crumbTag = "-H \"${CRUMB}\" "
            if (CRUMB.toLowerCase().indexOf('error') != -1) {
                this.self.println "CRUMB error. CSRF might not be activated on your Jenkins: $CRUMB"
                crumbTag = ''
            }
            def returnScriptText = scriptText + '''
return
'''
            this.self.sh """
                curl -kig -u '${this.self.env.JENKINS_USER}':'${this.self.env.JENKINS_PASSWORD}' \
                ${crumbTag} --data-urlencode "script=${returnScriptText}" ${this.self.env.JENKINS_URL}scriptText
            """
        }
    }
}
