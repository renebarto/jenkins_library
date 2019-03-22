def call(String executable, String resultsDir, String resultsFile) {
  def errorCode = makeDir(resultsDir)
  if (haveErrors(errorCode)) {
    return errorCode
  }
  errorCode = runCommand("rm -rf ${resultsDir}/*")
  if (haveErrors(errorCode)) {
    return errorCode
  }
  return runCommand("valgrind --suppressions=valgrind.supp --xml=yes --xml-file=${resultsDir}/${resultsFile} ${executable}")
}
