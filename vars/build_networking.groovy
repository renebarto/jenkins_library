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
	tests = ''
    with_ninja = false    // Build with Ninja
  }
  */
  def util =  new util(this)
  pipeline {
    agent any
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
            if (config.with_ninja?.trim()) {
              env.with_ninja = "false"
            }
            if (config.with_ninja == "true") {
              echo "Using Ninja to build"           
            } else {
              echo "Not using Ninja to build"           
            }
            if (config.branch?.trim()) {
              env.branch = "master"
            }
			env.tests = config.tests
			if (config.tests?.trim()) {
				env.tests = 'osal-test,core-test,tracing-test,utility-test'
			}
            echo "Running tests ${env.tests}"           
          }
        }
      }
      stage('Checkout') {
        steps {
          script {
            echo "Checking out repository: https://github.com/renebarto/networking/ branch: ${env.branch}"       
            checkout([
              $class: 'GitSCM', 
              branches: [[
                name: "refs/heads/${env.branch}"
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
            env.timestampStart = new Date().format('yyyy-MM-dd_HH-mm-ss')
            echo "Start time: env.timestampStart"           
            env.current_commit_hash = getCommitHash('HEAD')
            env.buildID = "${env.current_commit_hash}_${env.timestampStart}_${currentBuild.number}"
            echo "Build ID: ${env.buildID}"

            def output = getLastCommitSummary()
            echo "Last commit: ${output}"
            echo "Current build result: ${currentBuild.result}"
          }
        }
      }
      stage('Static analysis') {
        steps {
          script {
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
            echo "Current build result: ${currentBuild.result}"
          }
        }
      }
      stage('Report static analysis results') {
        steps {
          script {
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
            echo "Current build result: ${currentBuild.result}"
          }
        }
      }
      stage('Build') {
        steps {
          script {
            env.build_dir = "${WORKSPACE}/build"
            if (env.with_ninja == "true") {
              runCMake(env.build_dir, [
                CMAKE_BUILD_TYPE: 'Debug',
                CMAKE_EXPORT_COMPILE_COMMANDS: 'ON',
                CMAKE_INSTALL_PREFIX: "/home/rene/install/usr",
              ],
              "Ninja",
              [
                "ninja clean",
                "ninja"
              ])
            } else {
              runCMake(env.build_dir, [
                CMAKE_BUILD_TYPE: 'Debug',
                CMAKE_EXPORT_COMPILE_COMMANDS: 'ON',
                CMAKE_INSTALL_PREFIX: "/home/rene/install/usr",
              ],
              "Unix Makefiles",
              [
                "make clean",
                "make"
              ])
            }
            echo "Current build result: ${currentBuild.result}"
          }
        }
      }
      stage('Test') {
        steps {
          script {
            env.resultsDir = 'test-results'
            makeDir(env.resultsDir)
            runCommand("rm -rf ${env.resultsDir}/*")
			def testList = env.tests.trim().split(',').collect{it.trim()}
            //tests = [
            //  'osal-test',
            //  'core-test',
            //  'utility-test',
            //]
            testList.each { testName ->
              println "Running test: ${testName}"
              runTests("${WORKSPACE}/output/Linux/Debug/bin/${testName}", "${WORKSPACE}/${env.resultsDir}", "${testName}.xml")
            }
            echo "Current build result: ${currentBuild.result}"
          }
        }
      }
      stage('Report test results') {
        steps {
          script {
            xunit (
              thresholds: [
                failed(failureNewThreshold: '0', failureThreshold: '0', unstableNewThreshold: '0', unstableThreshold: '0')
              ], 
              tools: [
                GoogleTest(deleteOutputFiles: false, excludesPattern: '', pattern: "${env.resultsDir}/*.xml", skipNoTestFiles: true, stopProcessingIfError: true)
              ]
            )
            echo "Current build result: ${currentBuild.result}"
          }
        }
      }
      stage('Deploy') {
        steps {
          script {
            echo "Deploy"
            echo "Current build result: ${currentBuild.result}"
          }
        }
      }
      stage('SetBuildDescription') {
        steps {
          script {
            currentBuild.description = "${env.node_name}<br/>${env.current_commit_hash}<br/>${env.buildID}"
            echo "Current build result: ${currentBuild.result}"
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
