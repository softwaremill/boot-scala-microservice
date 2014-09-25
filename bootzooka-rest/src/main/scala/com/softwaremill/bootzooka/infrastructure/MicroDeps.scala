package com.softwaremill.bootzooka.infrastructure

import com.ofg.infrastructure.discovery.util.MicroDepsService
import com.softwaremill.bootzooka.service.config.MicroDepsConfig
import com.typesafe.scalalogging.slf4j.Logging

class MicroDeps(config: MicroDepsConfig) extends Logging{
  val micro: MicroDepsService = new MicroDepsService(config.zookeeperUrl, config.microserviceContext,
    config.microserviceHost, config.microservicePort)

  def start(): Unit = {
    logger.info("Starting microdeps")
    micro.start()
  }

  def stop(): Unit = {
    logger.info("Stopping microdeps")
    micro.stop()
  }
}
