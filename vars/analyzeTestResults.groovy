def call(String resultsDir) {
  xunit {
    thresholds: [
      failed(
        failureThreshold: '0', 
        unstableThreshold: '0'
      )
    ], 
    tools: [
      GoogleTest(
        deleteOutputFiles: false, 
        excludesPattern: '', 
        failIfNotNew: false, 
        pattern: "${resultsDir}/**/*.xml", 
        stopProcessingIfError: true
      )
    ] 
  }
  return true
}
