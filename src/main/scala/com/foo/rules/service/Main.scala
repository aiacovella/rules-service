package com.foo.rules.service

import java.net.URL

import akka.actor.ActorSystem
import com.foo.rules.service.DynamicRulesService.Protocol.{GetRules, LoadRule}
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import akka.util.Timeout
import com.foo.rules.spi.BusinessRuleT

import scala.concurrent.duration._

/**
  * Created by al on 10/13/16.
  */
object Main {

  def main(args: Array[String]): Unit = {

    implicit val timeOut = Timeout(5 seconds)

    implicit val actorSystem: ActorSystem = ActorSystem("rules-service")
    implicit val ctx = actorSystem.dispatcher

    val rulesService = actorSystem.actorOf(DynamicRulesService.props, "business-rules-service")

    //  val rulesOneUrl = new URL("file:///Users/al/.ivy2/local/com.foo/rules-module-one_2.11/1.0-SNAPSHOT/jars/rules-module-one_2.11.jar")
    val rulesOneUrl = new URL("http://localhost:8000/rules-module-one_2.11-1.0-SNAPSHOT.jar")

    rulesService ! LoadRule(rulesOneUrl)

    (rulesService ? GetRules).mapTo[Seq[BusinessRuleT]].onSuccess{
      case rules => println("Rules: " + rules.map(_.shortName))
    }

    println("==== Rules: " + (rulesService ? GetRules).mapTo[Seq[BusinessRuleT]])

    //    val rulesTwoUrl = new URL("file:///Users/al/.ivy2/local/com.foo/rules-module-two_2.11/1.0-SNAPSHOT/jars/rules-module-two_2.11.jar")
    val rulesTwoUrl = new URL("http://localhost:8000/rules-module-two_2.11-2.1.3-SNAPSHOT.jar")

    rulesService ! LoadRule(rulesTwoUrl)

    (rulesService ? GetRules).mapTo[Seq[BusinessRuleT]].onSuccess{
      case rules => println("Rules: " + rules.map(_.shortName))
    }


  }

}
