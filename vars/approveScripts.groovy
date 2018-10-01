

def runRemoteScript(credentialId, scriptText) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialId,
                usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD']]) {
    def CRUMB = sh(returnStdout: true, script: """
        curl -k -u '${JENKINS_USER}':'${JENKINS_PASSWORD}' '${env.JENKINS_URL}crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)'
    """).trim()
    sh """
        curl -k  --user '${JENKINS_USER}':'${JENKINS_PASSWORD}' -H "${CRUMB}" --data-urlencode "script=${scriptText}" ${env.JENKINS_URL}scriptText
    """
}
}

def call(credentialId) {
    def scriptText = '''
import org.jenkinsci.plugins.scriptsecurity.scripts.*

ScriptApproval sa = ScriptApproval.get();

// approve scripts
for (ScriptApproval.PendingScript pending : sa.getPendingScripts()) {
    sa.approveScript(pending.getHash());
    println "Approved : " + pending.script
}

// approve signatures
for (ScriptApproval.PendingSignature pending : sa.getPendingSignatures()) {
    sa.approveSignature(pending.signature);
    println "Approved : " + pending.signature
}
    '''
    runRemoteScript(credentialId, scriptText)
}
return this