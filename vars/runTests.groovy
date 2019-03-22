def call(String executable, String resultsDir, String resultsFile) {
  def errorCode = makeDir(resultsDir)
  if (haveErrors(errorCode)) {
    return [errorCode, accumulatedOutput]
  }
  errorCode = runCommand("rm -rf ${resultsDir}/*")
  if (haveErrors(errorCode)) {
    return errorCode
  }
  return runCommand("${executable} --xml ${resultsDir}/${resultsFile}")
}
