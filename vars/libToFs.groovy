#!/usr/bin/env groovy

import com.sap.corydoras.RemoteExecute


def extractTreeish(uri) {
    def tag = '.git@'
    if (uri =~ /\${tag}/) {
        def treeishPos = uri.indexOf(tag) + tag.size()
        def treeish = uri.substring(treeishPos)
        return treeish
    }
    return 'master'
}

def call(credentialId, jenkinsContext, libraries) {
    def scriptText = libraryResource 'com/sap/corydoras/script/setLibrary.groovy'
    libraries.each {
        def libPath = "$JENKINS_HOME/shared_libraries/${it.getKey()}"
        def uri = it.getValue().replaceAll(/\.git@.*/, '.git')
        def treeish = extractTreeish(it.getValue())
        def treeishNoPrefix = treeish.replaceAll(/^origin\//, '')

        sh """
if [ -d ${libPath} ]; then
    cd ${libPath}
    git fetch --all --prune
    git reset --hard ${treeish}
else
    if echo "${treeish}" | grep -q origin; then
        git clone -b ${treeishNoPrefix} --single-branch ${uri} ${libPath}
    else
        git clone ${uri} ${libPath}
        git fetch --all
        git checkout ${treeishNoPrefix}
    fi
fi
"""

        scriptText += """
createIfMissing('${it.getKey()}', 'file://${libPath}', '${treeishNoPrefix}')
"""
    }
    new RemoteExecute(jenkinsContext, credentialId).runScript(scriptText)
}
return this
