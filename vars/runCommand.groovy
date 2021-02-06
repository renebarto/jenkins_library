def call(cmd) {
  echo "Execute command: '${cmd}'"
  try {
    sh(script: "${cmd}")
  }
  catch (Exception ex) {
    echo "runCommand failed with exception: '${ex}'"
	throw ex
  }
}
