import org.jenkinsci.plugins.scriptsecurity.scripts.*

ScriptApproval sa = ScriptApproval.get()
def approvedScriptCount = 0

def approveScripts() {
    def _approvedScriptCount = 0
    for (ScriptApproval.PendingScript pending : sa.getPendingScripts().clone()) {
        try {
            sa.approveScript(pending.getHash())
            _approvedScriptCount++
        } catch (Exception ex) {
            println ex
        }
    }
    approvedScriptCount += _approvedScriptCount
    if (_approvedScriptCount != 0) {
        sleep(500)
        approveScripts()
    }
}
approveScripts()

println 'Approved scripts:' + approvedScriptCount
