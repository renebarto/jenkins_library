def call(cmd) {
  echo "Execute command: '${cmd}'"
  def result = 0
  try {
    result = sh(returnStatus: true, script: "${cmd}")
  }
  catch (Exception ex) {
    echo "runCommandNoOutput failed with exception: '${ex}'"
  }
  echo "runCommandNoOutput: result: ${result}"
  return result
}
