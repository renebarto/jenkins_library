def call(String executable, String resultsDir, String resultsFile) {
  makeDir(resultsDir)
  runCommand("rm -rf ${resultsDir}/*")
  runCommand("valgrind --suppressions=valgrind.supp --xml=yes --xml-file=${resultsDir}/${resultsFile} ${executable}")
}
