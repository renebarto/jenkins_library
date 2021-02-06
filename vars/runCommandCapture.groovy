def call(cmd) {
  echo "Execute command: '${cmd}'"
  def stdout = ""
  def outfile = "stdout.out"
  try {
    sh(script: "${cmd} >${outfile} 2>&1")
    stdout = readFile(outfile).trim()
  }
  catch (Exception ex) {
    echo "runCommandCapture failed with exception: '${ex}'"
	throw ex
  }
  echo "runCommandCapture: output: '${stdout}'"
  return [stdout]
}
