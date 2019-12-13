package moonbox.grid.deploy.rest.routes

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Route
import io.swagger.annotations._
import moonbox.grid.deploy.rest.entities.{ExecuteRequest, Response}
import moonbox.grid.deploy.rest.service.{LoginService, WorkbenchService}
import moonbox.grid.deploy.security.Session

import scala.util.{Failure, Success}

/**
  * WorkbenchRoute is responsible for execute/explain/save/cancel query and recreate connection
  *
  * @param loginService
  */

@Api(
  value = "Workbench",
  consumes = "application/json",
  produces = "application/json", authorizations = Array(new Authorization("Bearer")))
@Path("/workbench")
class WorkbenchRoute(override val loginService: LoginService, workbenchService: WorkbenchService) extends SecurityRoute {

  @ApiOperation(value = "Execute Query", nickname = "execute", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "Execute Query", value = "Execute Query Parameter Information", required = true, paramType = "body", dataType = "moonbox.grid.deploy.rest.entities.ExecuteRequest")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 451, message = "request process failed"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  @Path("/console/execute")
  def executeQuery = (session: Session) => {
    put {
      entity(as[ExecuteRequest]) { in =>
        onComplete(workbenchService.executeQuery(in, session)) {
          case Success(result) =>
            complete(OK, Response(code = 200, msg = "Success", payload = Some(result)))
          case Failure(e) =>
            complete(OK, Response(code = 451, msg = e.getMessage))
        }
      }
    }
  }

  @ApiOperation(value = "Cancel Query", nickname = "cancel", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "consoleId", value = "console id", required = true, paramType = "path", dataType = "string")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 451, message = "request process failed"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  @Path("/console/cancel/{consoleId}")
  def cancelQuery = (session: Session) => path(Segment) { consoleId =>
    put {
      onComplete(workbenchService.cancel(consoleId)) {
        case Success(_) =>
          complete(OK, Response(code = 200, msg = "Success"))
        case Failure(e) =>
          complete(OK, Response(code = 451, msg = e.getMessage))
      }
    }
  }

  @ApiOperation(value = "Recreate Connection", nickname = "recreate", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "consoleId", value = "console id", required = true, paramType = "path", dataType = "string")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 451, message = "request process failed"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  @Path("/{consoleId}")
  def reconnect = (session: Session) => path(Segment) { consoleId =>
    put {
      onComplete(workbenchService.reconnect(consoleId)) {
        case Success(_) =>
          complete(OK, Response(code = 200, msg = "Success"))
        case Failure(e) =>
          complete(OK, Response(code = 451, msg = e.getMessage))
      }
    }
  }

  override def createSecurityRoute: Array[Session => Route] = Array(
    executeQuery, cancelQuery, reconnect
  )
}