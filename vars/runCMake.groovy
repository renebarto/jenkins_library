def call(String build_dir, Map parameters) {
  def parameterString = ""
  parameters.each{ k, v -> parameterString= "-D${k}=${v} ${parameterString}" }
  parameterString = parameterString.trim()

  def commandFile = "${WORKSPACE}/command_.sh"

  def errorCode = runCommand("echo \"pushd ${build_dir}\ncmake .. ${parameterString}\nmake clean\nmake\npopd\" > ${commandFile}")
  if (haveErrors(errorCode)) {
    return errorCode
  }
  errorCode = makeExecutable(commandFile)
  if (haveErrors(errorCode)) {
    return errorCode
  }
  return = runCommand("${commandFile}")
}
