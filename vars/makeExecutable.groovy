def call(String path) {
  try {
    runCommand("chmod 755 ${path}")
  }
  catch (Exception ex) {
    echo "Cannot chmod ${path}: ${ex}"
	throw ex
  }
}
