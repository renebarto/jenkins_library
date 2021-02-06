def call(String path) {
  try {
	runCommand("rm -rf ${path}")
  }
  catch (Exception ex) {
    echo "Cannot remove directory ${path}: ${ex}"
	throw ex
  }
}
