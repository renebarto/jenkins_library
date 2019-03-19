def call(String build_dir, Map parameters) {
  def parameterString = ""
  parameters.each{ k, v -> parameterString= "-D${k}=${v} ${parameterString}" }
  parameterString = parameterString.trim()

  def commandFile = "command_.sh"

  def (errorCode, output) = runCommand("echo \"pushd ${build_dir}\ncmake .. ${parameterString}\nmake clean\nmake\npopd\" > ${commandFile}")
  if (haveErrors(errorCode)) {
    return [errorCode, output]
  }
  def (errorCode, output) = makeExecutable(commandFile)
  if (haveErrors(errorCode)) {
    return [errorCode, output]
  }
  (errorCode, output) = runCommand("${build_dir}")
  return [errorCode, output]
}
