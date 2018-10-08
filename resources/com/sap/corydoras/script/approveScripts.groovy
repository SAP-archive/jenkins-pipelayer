import org.jenkinsci.plugins.scriptsecurity.scripts.*

ScriptApproval sa = ScriptApproval.get()

def approveScripts(sa, approvedScriptCount) {
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
        approvedScriptCount = approveScripts(sa, approvedScriptCount)
    }
    approvedScriptCount
}
def approvedScriptCount = approveScripts(sa, 0)

println 'Approved scripts:' + approvedScriptCount
