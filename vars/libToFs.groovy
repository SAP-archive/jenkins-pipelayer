#!/usr/bin/env groovy

import com.sap.corydoras.RemoteExecute

def isValidSHA1(String s) {
    s =~ /^[a-fA-F0-9]{40}$/
}

def extractBranch(uri) {
    def tag = '.git@'
    if (uri =~ /\${tag}/) {
        def branchPos = uri.indexOf(tag) + tag.size()
        def branch = uri.substring(branchPos)
        if (isValidSHA1(s)) {
            return branch
        }
        return 'origin/' + branch
    }
    return 'origin/master'
}

def call(credentialId, jenkinsContext, libraries) {
    def scriptText = libraryResource 'com/sap/corydoras/script/setLibrary.groovy'
    libraries.each {
        def libPath = "$JENKINS_HOME/shared_libraries/${it.getKey()}"
        def uri = it.getValue().replaceAll(/\.git@.*/, '.git')
        def branch = extractBranch(it.getValue())
        def branchNoPrefix = branch.replaceAll(/^origin\//, '')

        sh """
if [ -d ${libPath} ]; then
    cd ${libPath}
    git fetch origin --prune
    git reset --hard ${branch}
else
    if echo "${branch}" | grep -q origin; then
        git clone -b ${branchNoPrefix} --single-branch ${uri} ${libPath}
    else
        git clone ${uri} ${libPath}
        git checkout ${branchNoPrefix}
    fi
fi
"""

        scriptText += """
createIfMissing('${it.getKey()}', 'file://${libPath}', '${branchNoPrefix}')
"""
    }
    new RemoteExecute(jenkinsContext, credentialId).runScript(scriptText)
}
return this