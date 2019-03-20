def call(String resultsDir, String resultsFile) {
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
  (errorCode, output) = runCommand("gcovr --xml --xml-pretty --output=${resultsDir}/${resultsFile} -r .")
  accumulatedOutput = "${accumulatedOutput}${output}"
  return [errorCode, accumulatedOutput]
}
