def call(String build_dir, Map parameters, String generator, List makeCommands) {
  def errorCode = removeDir(build_dir)
  if (haveErrors(errorCode)) {
    echo "Failure removing directory ${build_dir}: ${errorCode}"
    return errorCode
  }
  errorCode = makeDir(build_dir)
  if (haveErrors(errorCode)) {
    echo "Failure creating directory ${build_dir}: ${errorCode}"
    return errorCode
  }

  def parameterString = ""
  parameters.each{ k, v -> parameterString= "${parameterString} -D${k}=${v}" }
  parameterString = parameterString.trim()

  def commandFile = "${WORKSPACE}/command_.sh"

  def makeCommandsString = ""
  makeCommands.each{ makeCommandsString = "${makeCommandsString}\n$it" }

  errorCode = runCommand("#!/bin/bash\nset -e\npushd ${build_dir}\ncmake .. -G \"${generator}\" ${parameterString}${makeCommandsString}\npopd > ${commandFile}")
  if (haveErrors(errorCode)) {
    return errorCode
  }
  errorCode = makeExecutable(commandFile)
  if (haveErrors(errorCode)) {
    return errorCode
  }
  return runCommand("${commandFile}")
}
