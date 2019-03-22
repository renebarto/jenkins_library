def call(branch) {
  return runCommandCapture("git rev-parse ${branch}")
}
