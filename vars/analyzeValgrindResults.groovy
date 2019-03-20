def call(String resultsDir) {
  xunit(
    [
      Valgrind(
        deleteOutputFiles: true, 
        failIfNotNew: true, 
        pattern: "${resultsDir}/**/*.xml", 
        skipNoTestFiles: false, 
        stopProcessingIfError: true
      )
    ]
  )
  return true
}
