def call(String build_dir, Map parameters, String generator, List makeCommands) {
  removeDir(build_dir)
  makeDir(build_dir)

  def parameterString = ""
  parameters.each{ k, v -> parameterString= "${parameterString} -D${k}=${v}" }
  parameterString = parameterString.trim()

  def commandFile = "${WORKSPACE}/command_.sh"

  def makeCommandsString = ""
  makeCommands.each{ makeCommandsString = "${makeCommandsString}\n$it" }

  runCommand("#!/bin/bash\nset -e\npushd ${build_dir}\ncmake .. -G \"${generator}\" ${parameterString}${makeCommandsString}\npopd")
}
