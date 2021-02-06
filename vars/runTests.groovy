def call(String executable, String resultsDir, String resultsFile) {
  runCommand("${executable} --gtest_output=xml:${resultsDir}/${resultsFile}")
}
