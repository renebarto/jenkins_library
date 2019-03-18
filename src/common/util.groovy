package com.philips.fdcsib
// import java package
// import java.io.File
// import java.util.ArrayList
// import java.time.LocalDateTime
// import java.time.format.DateTimeFormatter

class Utilities implements Serializable {
  def framework
  Utilities(framework) {this.framework = framework}

  def convertMultiLineToArray(tags) {
    def lines = tags.split('\n')
    return lines
  }

  def sortNumberedStrings(unsorted) {
    Closure numericSort = { String a, String b ->
      def c = a.isBigDecimal() ? new BigDecimal(a) : a
      def d = b.isBigDecimal() ? new BigDecimal(b) : b
  
      if (c.class == d.class) {
        return c <=> d
      } else if (c instanceof BigDecimal) {
        return -1
      } else {
        return 1
      }
    }
    
    framework.echo "Unsorted ${unsorted}"
    def sorted = unsorted
    sorted.sort(numericSort)
    framework.echo "Sorted ${sorted}"
    return sorted
  }

  def getLatestBaseline(tags)
  {
    def sortedTags = sortNumberedStrings(tags)
    def latestBaseLine = ''
    if (sortedTags.size() > 0) {
      def latestBaseLineIndex = sortedTags.size() - 1
      latestBaseLine = sortedTags[latestBaseLineIndex]
    }
    framework.echo "Latest baseline ${latestBaseLine}"
    return latestBaseLine
  }

  def getNextBaseline(currentBaseline) {
    def nextVersion = currentBaseline.split("\\.")
    def lastElementIndex = nextVersion.size() - 1
    nextVersion[lastElementIndex]++
    def nextBaseline = nextVersion.join(".")
    return nextBaseline
  }

  def haveErrors() {
    return framework.env.errorCode != "0"
  }

  def needToBuild() {
    return framework.currentBuild.result == null
  }

  @NonCPS
  String getLastModifiedDirectoryContaining(def pathName, def containsItem) {
    def directoryList = new File(pathName).listFiles()?.sort{a, b -> b.lastModified() <=> a.lastModified()}
    framework.echo "directoryList: ${directoryList}"
    def foundFile
    for (File file : directoryList) {
      def checkItem = new File(file, containsItem)
      framework.echo "check item: ${checkItem.name}"
      if (checkItem.exists()) {
        framework.echo "Containing item ${checkItem.name} exists within path ${file.getAbsolutePath()}"
        foundFile = file
        break
      }
    }
    def returnValue = foundFile.name ?: ''
    return foundFile.name
  }

  // // NOTE: java.io.file is executed on the master, not on a slave
  // //https://support.cloudbees.com/hc/en-us/articles/217639408-Using-java-io-File-in-a-Pipeline-description
  // // use fileExists to check if file exists in a workspace
  // boolean checkIfFileOrFolderExists(def pathName) {
  //   File tmp = new File(pathName)
  //   boolean exists = tmp.exists()
  //   return exists
  // }

  // String getTimeStamp(String dateFormat) {
  //   LocalDateTime now = LocalDateTime.now()
    
  //   DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat)
  //   String formattedDateTime = now.format(formatter)
  
  //   return formattedDateTime
  // }
}
