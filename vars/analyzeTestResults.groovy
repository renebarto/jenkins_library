def call(String resultsDir) {
  xunit 
    thresholds: [failed(failureNewThreshold: '0', failureThreshold: '0', unstableNewThreshold: '0', unstableThreshold: '0')], tools: [GoogleTest(deleteOutputFiles: false, excludesPattern: '', pattern: '"${resultsDir}/**/*.xml"', skipNoTestFiles: true, stopProcessingIfError: true)]
}
