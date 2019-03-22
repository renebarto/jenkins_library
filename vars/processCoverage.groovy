def call(String resultsDir, String resultsFile) {
  def errorCode = makeDir(resultsDir)
  if (haveErrors(errorCode)) {
    return errorCode
  }
  errorCode = runCommand("rm -rf ${resultsDir}/*")
  if (haveErrors(errorCode)) {
    return errorCode
  }
  return runCommand("gcovr --xml --xml-pretty --output=${resultsDir}/${resultsFile} -r .")
}
