package com.foo.rules.service

import akka.actor.{Actor, ActorLogging, Props, Stash}
import akka.pattern.pipe
import com.foo.rules.service.DynamicRulesService.Protocol._
import com.foo.rules.spi.{BusinessRuleT, BusinessRulesProviderT}
import java.io.File
import java.lang.reflect.{Constructor, Method}
import java.net.{URL, URLClassLoader}
import java.time.LocalDate

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Failure

/**
  * Created by al on 10/13/16.
  */
class DynamicRulesService(implicit ec:ExecutionContext)
  extends Actor
  with Stash
    with ActorLogging {

  override def receive: Receive = initialized(Seq())

  private def loading: Receive = {
    case BusinessRulesLoaded(version, releaseDate, rules) ⇒
      println(s">>> Loaded Rules Version: $version Release Date: $releaseDate" )

      rules.foreach { r ⇒
        log.debug(s">>> Rule: ${r.shortName} Description: ${r.description} ")
      }
      unstashAll

      log.debug(">>> Moving to initialized")

      context become initialized(rules)

    case akka.actor.Status.Failure(exc) ⇒
      unstashAll()
      exc.printStackTrace()
      context become initialized(Seq())

    case other ⇒ stash()

  }

  private def initialized(businessRules: Seq[BusinessRuleT]): Receive = {

    case LoadRule(resource) ⇒
      log.debug(">>> Moving to uninitialized")
      context become loading
      loadRules(resource) pipeTo self

    case GetRules ⇒ sender ! businessRules
  }

  private def loadRules(resource:URL): Future[RulesCommand] = {

    Future {

//      new File(resource).exists() match {
//        case true ⇒

          // Getting the jar URL which contains target class
          val classLoaderUrls = Seq(resource).toArray
          // Create a new URLClassLoader
          val urlClassLoader: URLClassLoader = new URLClassLoader(classLoaderUrls)

          // Load the target class
          val clazz: Class[_] = urlClassLoader.loadClass("foo.rules.impl.BusinessRuleService")

          // Create a new instance from the loaded class
          val constructor: Constructor[_] = clazz.getConstructor()

          val providerInstance = constructor.newInstance()

          // Getting a method from the loaded class and invoke it
          val getRules: Method = clazz.getMethod("rules")
          val rules = getRules.invoke(providerInstance).asInstanceOf[Seq[BusinessRuleT]]

          val getVersion: Method = clazz.getMethod("version")
          val version = getVersion.invoke(providerInstance).asInstanceOf[String]

          val getReleaseDate: Method = clazz.getMethod("releaseDate")
          val releaseDate = getReleaseDate.invoke(providerInstance).asInstanceOf[LocalDate]

          BusinessRulesLoaded(version, releaseDate, rules)

//        case false ⇒
//          val notFoundMsg: String = s"Rules service implementation not found at $resource"
//          log.error(notFoundMsg)
//
//          BusinessRuleServiceLoadFailure(notFoundMsg)
//      }

    }
  }
}

object DynamicRulesService {

  def props(implicit ec:ExecutionContext) = Props(new DynamicRulesService)

  object Protocol {

    sealed trait RulesCommand
    case class LoadRule(resource: URL) extends RulesCommand
    final case class BusinessRuleServiceLoadFailure(err: String) extends RulesCommand
    final case class BusinessRulesLoaded(version:String, releaseDate: LocalDate, seq: Seq[BusinessRuleT]) extends RulesCommand
    case object GetRules extends RulesCommand

  }

}

