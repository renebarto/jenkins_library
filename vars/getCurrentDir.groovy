def call() {
  def (errorCode, output) = runCommandCapture("pwd")
  return haveErrors(errorCode) ? "" : output
}
