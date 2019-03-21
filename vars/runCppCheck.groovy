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
  def outfile = "stdout.out"
  try {
    errorCode = sh(returnStatus: true, script: "cppcheck --enable=warning,performance,portability,style --language=c++ --xml-version=2 --inline-suppr include src test  >${outfile} 2>${resultsDir}/${resultsFile}")
    output = readFile(outfile).trim()
  }
  catch (Exception ex) {
    echo "runCommand failed with exception: '${ex}'"
  }
  accumulatedOutput = "${accumulatedOutput}${output}"
  return [errorCode, accumulatedOutput]
}
