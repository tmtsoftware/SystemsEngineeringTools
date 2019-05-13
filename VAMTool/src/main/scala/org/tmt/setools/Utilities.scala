package org.tmt.setools

object Utilities {

  case class Requirement(id: String, fullText: String, verifiedByTestSuite: Boolean)

  case class TestFile(relativePath: String, filename: String)

  case class UserStoryReference(reference: String)

  case class TestReference(file: TestFile, packageName: String, className: String, testName: String, lineNumber: Int)

}
