def call(String path) {
  try {
    runCommand("mkdir -p ${path}")
  }
  catch (Exception ex) {
    echo "Cannot create directory ${path}: ${ex}"
	throw ex
  }
}
