
package com.softwaremill.bootzooka.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class UserPasswordStrategy(protected val app: ScalatraBase, login: String, password: String, val userService: UserService) extends ScentryStrategy[UserJson] {

  override def name: String = UserPassword.name

  override def isValid(implicit request: HttpServletRequest) = {
    !login.isEmpty && !password.isEmpty
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    userService.authenticate(login, password)
  }

}

object UserPassword {

  val name = "UserPassword"

}
