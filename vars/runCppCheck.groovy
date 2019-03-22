def call(List options, String resultsDir, String resultsFile) {
  def errorCode = makeDir(resultsDir)
  if (haveErrors(errorCode)) {
    return errorCode
  }
  errorCode = runCommand("rm -rf ${resultsDir}/*")
  if (haveErrors(errorCode)) {
    return errorCode
  }
  def parameterString = ""
  options.each{ parameterString= "${parameterString}$it " }
  parameterString = parameterString.trim()

  reutrn runCommand("cppcheck ${parameterString} 2>${resultsDir}/${resultsFile}")
}
