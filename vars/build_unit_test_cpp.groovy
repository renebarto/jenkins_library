import common.util
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  /*
  Configuration example:
  @Library('globalLibrary') _
  buildProduction {
    label = ''
    branch = ''
    timeoutInHours = 4
    recipients = ''
  }
  */
  def util =  new util(this)
  pipeline {
    agent any
    // {
    //   node {
    //     label "${config.label}"
    //   }
    // }
    options {
      timeout (
        time: config.timeoutInHours,
        unit: 'HOURS'
      )
      ansiColor('xterm')
    }
    environment {
      errorCode = 0
    }
    stages {
      stage('Checkout') {
        steps {
          script {
            checkout([
              $class: 'GitSCM', 
              branches: [[
                name: "refs/heads/${config.branch}"
              ]], 
              doGenerateSubmoduleConfigurations: false, 
              extensions: [], 
              submoduleCfg: [], 
              userRemoteConfigs: [[
                url: 'https://github.com/renebarto/unittest-cpp/'
              ]]
            ])
          }
        }
      }
      stage('Determine baseline') {
        steps {
          script {
            env.timestamp = new Date().format('yyyy-MM-dd_HH-mm-ss')
            def (errorCode, output) = getCommitHash('HEAD')
            if (haveErrors(errorCode)) {
              echo "Failure running getCommitHash ${errorCode}"
              currentBuild.result = 'FAILURE'
            }
            env.current_commit_hash = env.output

            (errorCode, output) = getLastCommitSummary()
            if (haveErrors(errorCode)) {
              echo "Failure running getLastCommitSummary ${errorCode}"
              currentBuild.result = 'FAILURE'
            }
            echo "Last commit: ${output}"
          }
        }
      }
      stage('Build') {
        steps {
          script {
            if (needToBuild()) {
            }
            //   env.os_version_base="wrlinux-9"
            //   env.os_version_extended="${env.os_version_base}_20181218154004904_21044"
            //   if (util.needToBuild()) {
            //     echo "./buildWRL9.sh ${WORKSPACE}/${env.os_version_base} /opt/WindRiver/${env.os_version_base}/${env.os_version_extended}/WRLinux-9-LTS-CVE/${env.os_version_base}"
            //     runCommand("./buildWRL9.sh ${WORKSPACE}/${env.os_version_base} /opt/WindRiver/${env.os_version_base}/${env.os_version_extended}/WRLinux-9-LTS-CVE/${os_version_base}")
            //     if (util.haveErrors()) {
            //       echo "Failure running 'buildWRL9' ${env.errorCode}"
            //       currentBuild.result = 'FAILURE'
            //     }
            // }
          }
        }
      }
      stage('Stage') {
        steps {
          script {
            if (needToBuild()) {
            }
          }
        }
      }
      stage('Test') {
        steps {
          script {
            if (needToBuild()) {
            }
          }
        }
      }
      stage('Deploy') {
        steps {
          script {
            if (needToBuild()) {
            //   env.deploy_target_dir = ""
            //   if (config.developerBuild) {
            //     env.deploy_dir = "${env.wip_mount_dir}/DevBuilds/FDCSIB_OS/DTC_OS_Zynq_platform_WRL9_PreInt/${env.buildID}"
            //     env.deploy_target_dir = "${env.wip_dir}/DevBuilds/FDCSIB_OS/DTC_OS_Zynq_platform_WRL9_PreInt/${env.buildID}"
            //     env.errorCode = deployResultsToWIP('wrlinux-9/results')
            //     if (util.haveErrors()) {
            //       echo "Failure running deployResultsToWIP ${env.errorCode}"
            //       currentBuild.result = 'FAILURE'
            //     }
            //   } else {
            //     env.deploy_dir = "${env.wip_mount_dir}/CIBuilds/FDCSIB_OS/DTC_OS_Zynq_platform_WRL9_PreInt/${env.buildID}"
            //     env.deploy_target_dir = "${env.wip_dir}/CIBuilds/FDCSIB_OS/DTC_OS_Zynq_platform_WRL9_PreInt/${env.buildID}"
            //     env.errorCode = deployResultsToWIP('wrlinux-9/results')
            //     if (util.haveErrors()) {
            //       echo "Failure running deployResultsToWIP ${env.errorCode}"
            //       currentBuild.result = 'FAILURE'
            //     } else {
            //       if (env.nextBaseline != env.currentBaseline) {
            //         createTag("${env.nextBaseline}")
            //         if (util.haveErrors()) {
            //           echo "Failure running createTag ${env.errorCode}"
            //           currentBuild.result = 'FAILURE'
            //         }
            //       }
            //     }
            //   }
            //   env.deploy_target_dir = env.deploy_target_dir.replace("/", "\\")
            }
          }
        }
      }
      stage('SetBuildDescription') {
        steps {
          script {
            currentBuild.description = "${env.node_name}<br/>${env.current_commit_hash}<br/>${env.buildID}"
          }
        }
      }
    }
    post {
      always {
        script {
          sendNotification("${config.recipients}", currentBuild.result)
          // if (isDirectoryMounted(env.wip_mount_dir)) {
          //   echo "Unmount ${env.wip_mount_dir}"
          // }
          echo "The END."
        }
      }
    }
  }
}
