#!/usr/bin/env groovy

def runRemoteScript(credentialId, scriptText, jenkinsContext) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialId,
                usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD']]) {
        def jenkinsUrl = jenkinsContext.env.JENKINS_URL
        def CRUMB = sh(returnStdout: true, script: """
            curl -k -u '${JENKINS_USER}':'${JENKINS_PASSWORD}' '${jenkinsUrl}crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)'
        """).trim()
        sh """
            curl -k  --user '${JENKINS_USER}':'${JENKINS_PASSWORD}' -H "${CRUMB}" --data-urlencode "script=${scriptText}" ${jenkinsUrl}scriptText
        """
    }
}

def call(credentialId, jenkinsContext) {
    def scriptText = '''
import org.jenkinsci.plugins.scriptsecurity.scripts.*

ScriptApproval sa = ScriptApproval.get()

// approve scripts
for (ScriptApproval.PendingScript pending : sa.getPendingScripts()) {
    try {
        sa.approveScript(pending.getHash())
    } catch (Exception ex) {
        println ex
    }
}

// approve signatures
for (ScriptApproval.PendingSignature pending : sa.getPendingSignatures()) {
    try {
        sa.approveSignature(pending.signature)
    } catch (Exception ex) {
        println ex
    }
}
    '''
    runRemoteScript(credentialId, scriptText, jenkinsContext)
}
return this
