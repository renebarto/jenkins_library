def call(List options, String resultsDir, String resultsFile) {
  makeDir(resultsDir)
  runCommand("rm -rf ${resultsDir}/*")
  def parameterString = ""
  options.each{ parameterString = "${parameterString}$it " }
  parameterString = parameterString.trim()

  runCommand("cppcheck ${parameterString} 2>${resultsDir}/${resultsFile}")
}
