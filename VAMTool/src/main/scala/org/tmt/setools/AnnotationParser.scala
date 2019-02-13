package org.tmt.setools

object AnnotationParser {
  private def appendStoryId(origStories: Option[Set[String]], newStoryId: StoryId) = {
    if (origStories.isDefined) {
      origStories.get ++ newStoryId.value().toSet
    } else {
      newStoryId.value().toSet
    }
  }

  private val storyIdClass = classOf[StoryId]
  def getTestToStoryMap(clazz: Class[_]): Map[String, Set[String]] = {
    val map = if (clazz.isAnnotationPresent(storyIdClass)) {
        clazz
          .getDeclaredMethods
          .filter(_.isAnnotationPresent(classOf[org.junit.Test]))
          .map(m => m.getName -> clazz.getAnnotation(storyIdClass).value().toSet)
          .toMap
      } else {
      Map[String, Set[String]]()
    }


    map ++ clazz
      .getDeclaredMethods
      .filter(_.isAnnotationPresent(storyIdClass))
      .map(m => m.getName -> appendStoryId(map.get(m.getName), m.getAnnotation(storyIdClass))).toMap

  }

}

