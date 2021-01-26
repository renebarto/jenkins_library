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
    with_ninja = false    // Build with Ninja
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
      stage('Validate parameters') {
        steps {
          script {
            env.with_ninja = config.with_ninja
            if ((config.with_ninja == null) || (config.with_ninja == "")) {
              env.with_ninja = "false"
            }
          }
        }
      }
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
                url: 'https://github.com/renebarto/networking/'
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
            env.current_commit_hash = output
            env.buildID = "${env.current_commit_hash}_${env.timestamp}_${currentBuild.number}"

            (errorCode, output) = getLastCommitSummary()
            if (haveErrors(errorCode)) {
              echo "Failure running getLastCommitSummary ${errorCode}"
              currentBuild.result = 'FAILURE'
            }
            echo "Last commit: ${output}"
          }
        }
      }
      stage('Static analysis') {
        steps {
          script {
            if (needToBuild()) {
              runCppCheck(
                [
                  '--enable=warning,performance,portability,style',
                  '--language=c++',
				  '--library=googletest',
                  '--xml-version=2',
                  '--inline-suppr',
                  '-i code/external',
                  'code',
                ],
                "${WORKSPACE}/cppcheck-results", "cppcheck.xml")
            }
          }
        }
      }
      stage('Report static analysis results') {
        steps {
          script {
            if (needToBuild()) {
              def cppcheckIssues = scanForIssues(
                sourceCodeEncoding: 'US-ASCII', 
                tool: cppCheck(
                  pattern: 'cppcheck-results/**/*.xml', 
                  reportEncoding: 'US-ASCII'
                )
              )
              publishIssues(
                issues: [cppcheckIssues], 
                qualityGates: [[
                  threshold: 100, 
                  type: 'TOTAL', 
                  unstable: false
                ]]
              )
            }
          }
        }
      }
      stage('Build') {
        steps {
          script {
            if (needToBuild()) {
              env.build_dir = "${WORKSPACE}/build"
              if (env.with_ninja == "true") {
                def errorCode = runCMake(env.build_dir, [
                  CMAKE_BUILD_TYPE: 'Debug',
                  CMAKE_EXPORT_COMPILE_COMMANDS: 'ON',
                  CMAKE_INSTALL_PREFIX: "/home/rene/install/usr",
                ],
                "Ninja",
                [
                  "ninja clean",
                  "ninja"
                ])
                if (haveErrors(errorCode)) {
                  echo "Failure building: ${env.errorCode}"
                  currentBuild.result = 'FAILURE'
                }
              } else {
                def errorCode = runCMake(env.build_dir, [
                  CMAKE_BUILD_TYPE: 'Debug',
                  CMAKE_EXPORT_COMPILE_COMMANDS: 'ON',
                  CMAKE_INSTALL_PREFIX: "/home/rene/install/usr",
                ],
                "Unix Makefiles",
                [
                  "make clean",
                  "make"
                ])
                if (haveErrors(errorCode)) {
                  echo "Failure building: ${env.errorCode}"
                  currentBuild.result = 'FAILURE'
                }
              }
            }
          }
        }
      }
      stage('Test') {
        steps {
          script {
            if (needToBuild()) {
              runTests("${WORKSPACE}/output/Linux/Debug/bin/osal-test", "${WORKSPACE}/test-results", "networking.test.xml")
            }
          }
        }
      }
      stage('Report test results') {
        steps {
          script {
            if (needToBuild()) {
              analyzeTestResults("test-results")
            }
          }
        }
      }
      stage('Deploy') {
        steps {
          script {
            if (needToBuild()) {
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
          // sendNotification("${config.recipients}", currentBuild.result)
          // if (isDirectoryMounted(env.wip_mount_dir)) {
          //   echo "Unmount ${env.wip_mount_dir}"
          // }
          echo "The END."
        }
      }
    }
  }
}
