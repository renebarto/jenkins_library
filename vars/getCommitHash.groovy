def call(branch) {
  return runCommand("git rev-parse ${branch}")
}
