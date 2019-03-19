def call(String build_dir, Map parameters) {
  def parameterString = ""
  parameters.each{ k, v -> parameterString= "-D${k}=${v} ${parameterString}" }
  parameterString = parameterString.trim()
  def (errorCode, output) = runCommand("cd ${build_dir}; cmake .. ${parameterString}")
  def assembledOutput = output
  if (haveErrors(errorCode)) {
    return [errorCode, assembledOutput]
  }
  (errorCode, output) = runCommand("cd ${build_dir}; make clean")
  assembledOutput = "${assembledOutput}${output}"
  if (haveErrors(errorCode)) {
    return [errorCode, assembledOutput]
  }
  (errorCode, output) = runCommand("cd ${build_dir}; make")
  assembledOutput = "${assembledOutput}${output}"
  return [errorCode, assembledOutput]
}
