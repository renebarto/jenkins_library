def call(String executable, String resultsDir, String resultsFile) {
  return runCommand("${executable} --gtest_output=xml:${resultsDir}/${resultsFile}")
}
