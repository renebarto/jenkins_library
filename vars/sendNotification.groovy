def call(String recipients, String buildStatus = 'STARTED') {
  def buildFinishedEarly = false
  if (buildStatus != null) {
    buildFinishedEarly = true
  }
  if (buildStatus == 'SUCCESS') {
    // build status of SUCCESS actually means we skipped the build due to no new commits.
    buildStatus = 'SKIPPED'
  }
  // build status of null means successfull
  buildStatus = buildStatus ?: 'SUCCESS'
  echo "[sendNotification] Send email to: ${recipients}"
  echo "[sendNotification] BuildStatus: ${buildStatus}"
  // Default values
  def subject = "${env.node_name}: Job '${env.JOB_NAME}' completed with status ${buildStatus}"
  // If  build finished early, there is no deployment (failure or no new releases)
  def details = """<p><h1>${buildStatus}: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]</h1></p>
      <p>Check console output at &QUOT;<a href='${env.BUILD_URL}console'>'${env.JOB_NAME}' [${env.BUILD_NUMBER}]</a>&QUOT;</p>
          """
  // if (!buildFinishedEarly) {
  //   details = """<p><h1>${buildStatus}: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]</h1></p>
  //     <p>Check console output at &QUOT;<a href='${env.BUILD_URL}console'>'${env.JOB_NAME}' [${env.BUILD_NUMBER}]</a>&QUOT;</p>
  //     <p>Publish location: &QUOT;<a href='${env.wip_dir}'>${env.deploy_target_dir}</a>&QUOT;</p>
  //         """
  // }
  emailext (
    to: recipients,
    subject: subject,
    body: details,
    mimeType: 'text/html',
    recipientProviders: [
      [$class: 'RequesterRecipientProvider']
    ]
  )
}
