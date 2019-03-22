def call(cmd) {
  echo "Execute command: '${cmd}'"
  def errorCode = 0
  def stdout = ""
  def outfile = "stdout.out"
  try {
    errorCode = sh(returnStatus: true, script: "${cmd} >${outfile} 2>&1")
    stdout = readFile(outfile).trim()
  }
  catch (Exception ex) {
    echo "runCommandCapture failed with exception: '${ex}'"
  }
  echo "runCommandCapture: errorCode: ${errorCode} output: '${stdout}'"
  return [errorCode, stdout]
}
