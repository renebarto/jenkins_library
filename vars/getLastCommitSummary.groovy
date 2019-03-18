def call() {
  def (errorCode, output) = runCommand('git show --summary HEAD^..HEAD')
  if (haveErrors(errorCode))
    return [errorCode, ""]
  return [errorCode, output]
}
