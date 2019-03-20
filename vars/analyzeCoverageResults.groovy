def call(String resultsDir) {
  cobertura(
    autoUpdateHealth: false, 
    autoUpdateStability: false, 
    coberturaReportFile: "${resultsDir}/gcovr.xml", 
    failUnhealthy: false, 
    failUnstable: false, 
    maxNumberOfBuilds: 0, 
    methodCoverageTargets: '80, 0, 0', 
    lineCoverageTargets: '80, 0, 0', 
    conditionalCoverageTargets: '70, 0, 0', 
    onlyStable: false, 
    sourceEncoding: 'ASCII', 
    zoomCoverageChart: false
  )
  return true
}
