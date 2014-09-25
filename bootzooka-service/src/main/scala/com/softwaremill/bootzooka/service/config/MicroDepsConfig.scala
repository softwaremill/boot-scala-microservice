package com.softwaremill.bootzooka.service.config

import com.typesafe.config.Config

trait MicroDepsConfig extends BaseConfig {
  def rootConfig: Config

  private lazy val zookeeperConfig = rootConfig.getConfig("zookeeper")

  lazy val zookeeperUrl: String = getString("url", "localhost:2181")
  lazy val microserviceContext: String = getString("microserviceContext", "/")
  lazy val microservicePort: Int = getInt("microservicePort", 8080)
  lazy val microserviceHost: String = getString("microserviceHost", "localhost")
}
