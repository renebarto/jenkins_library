def call(String resultsDir, String resultsFile) {
  makeDir(resultsDir)
  runCommand("rm -rf ${resultsDir}/*")
  runCommand("gcovr --xml --xml-pretty --output=${resultsDir}/${resultsFile} -r .")
}
