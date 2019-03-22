def call(List options, String resultsDir, String resultsFile) {
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
  def parameterString = ""
  directories.each{ parameterString= "${parameterString}$it " }
  parameterString = parameterString.trim()

  def outfile = "stdout.out"
  try {
    errorCode = sh(returnStatus: true, script: "cppcheck ${parameterString} >${outfile} 2>${resultsDir}/${resultsFile}")
    output = readFile(outfile).trim()
  }
  catch (Exception ex) {
    echo "runCommand failed with exception: '${ex}'"
  }
  accumulatedOutput = "${accumulatedOutput}${output}"
  return [errorCode, accumulatedOutput]
}
