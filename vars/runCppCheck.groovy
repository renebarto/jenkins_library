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
  (errorCode, output) = runCommand("cppcheck --enable=warning,performance,portability,style --language=c++ --xml-version=2 --inline-suppr include src test 2>${resultsDir}/${resultsFile}")
  accumulatedOutput = "${accumulatedOutput}${output}"
  return [errorCode, accumulatedOutput]
}
