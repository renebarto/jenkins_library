def call() {
  def (errorCode, output) = runCommandCapture('git show --summary HEAD^..HEAD')
  if (haveErrors(errorCode))
    return [errorCode, ""]
  return [errorCode, output]
}
