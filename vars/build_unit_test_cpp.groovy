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
                  '--xml-version=2',
                  '--inline-suppr',
                  'include',
                  'src',
                  'test'
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
                  threshold: 1, 
                  type: 'TOTAL', 
                  unstable: false
                ]], 
                referenceJobName: 'unit-test-cpp'
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
                  BUILD_UNIT_TESTS: 'ON',
                  MEASURE_COVERAGE: 'ON',
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
                  BUILD_UNIT_TESTS: 'ON',
                  MEASURE_COVERAGE: 'ON',
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
              runTests("${WORKSPACE}/output/debug/bin/unittest-cpp.test", "${WORKSPACE}/test-results", "unittest-cpp.test.xml")
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
      stage('Process coverage') {
        steps {
          script {
            if (needToBuild()) {
              processCoverage("${WORKSPACE}/gcov-results", "gcovr.xml")
            }
          }
        }
      }
      stage('Report coverage results') {
        steps {
          script {
            if (needToBuild()) {
              analyzeCoverageResults("gcov-results")
            }
          }
        }
      }
      stage('Dynamic analysis') {
        steps {
          script {
            if (needToBuild()) {
              runValgrind("${WORKSPACE}/output/debug/bin/unittest-cpp.test", "${WORKSPACE}/valgrind-results", "all-tests.test.xml")
            }
          }
        }
      }
      stage('Report dynamic analysis results') {
        steps {
          script {
            if (needToBuild()) {
              analyzeValgrindResults("valgrind-results")
            }
          }
        }
      }
      stage('Deploy') {
        steps {
          script {
            if (needToBuild()) {
              env.build_dir = "${WORKSPACE}/build"
              if (env.with_ninja == "true") {
                def errorCode = runCMake(build_dir, [
                  CMAKE_BUILD_TYPE: 'Release',
                  CMAKE_INSTALL_PREFIX: "/home/rene/install/usr",
                  CMAKE_EXPORT_COMPILE_COMMANDS: 'ON',
                  BUILD_UNIT_TESTS: 'ON',
                ],
                "Ninja",
                [
                  "ninja clean",
                  "ninja",
                  "ninja dpkg"
                ])
                if (haveErrors(errorCode)) {
                  echo "Failure building: ${env.errorCode}"
                  currentBuild.result = 'FAILURE'
                }
              } else {
                def errorCode = runCMake(build_dir, [
                  CMAKE_BUILD_TYPE: 'Release',
                  CMAKE_INSTALL_PREFIX: "/home/rene/install/usr",
                  CMAKE_EXPORT_COMPILE_COMMANDS: 'ON',
                  BUILD_UNIT_TESTS: 'ON',
                ],
                "Unix Makefiles",
                [
                  "make clean",
                  "make",
                  "make dpkg"
                ])
              }
              if (haveErrors(errorCode)) {
                echo "Failure building: ${env.errorCode}"
                currentBuild.result = 'FAILURE'
              }
            }
            if (needToBuild()) {
              archiveArtifacts '*.deb'
            }
            removeDir("html")
            makeDir("html")

            runCommand("echo <!DOCTYPE html> >> html/index.html")
            runCommand("echo <html lang=\"en\"> >> html/index.html")
            runCommand("echo <head> >> html/index.html")
            runCommand("echo <meta charset=\"utf-8\"> >> html/index.html")
            runCommand("echo <title>title</title> >> html/index.html")
            runCommand("echo </head> >> html/index.html")
            runCommand("echo <body> >> html/index.html")
            runCommand("echo Hello >> html/index.html")
            runCommand("echo </body> >> html/index.html")
            runCommand("echo </html> >> html/index.html")

            publishHTML(
              [
                allowMissing: false, 
                alwaysLinkToLastBuild: false, 
                keepAll: false, 
                reportDir: 'html/', 
                reportFiles: 'index.html', 
                reportName: 'HTML Report', 
                reportTitles: ''
              ]
            )
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
