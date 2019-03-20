def call(String executable, String resultsDir, String resultsFile) {
  def (errorCode, output) = makeDir(resultsDir)
  def accumulatedOutput = output
  if (haveErrors(errorCode)) {
    return [errorCode, accumulatedOutput]
  }
  (errorCode, output) = runCommand("rm -rf ${resultsDir}/*")
  accumulatedOutput = "${accumulatedOutput}${output}"
  if (haveErrors(errorCode)) {
    return [errorCode, accumulatedOutput]
  }
  (errorCode, output) = runCommand("valgrind --suppressions=valgrind.supp --xml=yes --xml-file=${resultsDir}/${resultsFile} ${executable}")
  accumulatedOutput = "${accumulatedOutput}${output}"
  return [errorCode, accumulatedOutput]
}
