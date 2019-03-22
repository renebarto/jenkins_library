def call(cmd) {
  echo "Execute command: '${cmd}'"
  def result = 0
  try {
    result = sh(returnStatus: true, script: "${cmd}")
  }
  catch (Exception ex) {
    echo "runCommand failed with exception: '${ex}'"
  }
  echo "runCommand: result: ${result}"
  return result
}
