#!/usr/bin/env groovy

import com.sap.corydoras.RemoteExecute

def call(credentialId, jenkinsContext) {
    def scriptText = libraryResource 'com/sap/corydoras/script/approveScripts.groovy'
    new RemoteExecute(jenkinsContext, credentialId).runScript(scriptText)
}
return this
