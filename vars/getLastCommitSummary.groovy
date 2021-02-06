def call() {
  return runCommandCapture('git show --summary HEAD^..HEAD')
}
