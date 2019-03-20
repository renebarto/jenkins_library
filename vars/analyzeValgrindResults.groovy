def call(String resultsDir) {
  xunit(
    thresholds: [
      failed(
        failureThreshold: '0', 
        unstableThreshold: '0'
      )
    ], 
    tools: [
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
