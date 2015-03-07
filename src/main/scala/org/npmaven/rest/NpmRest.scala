package org.npmaven.rest

import net.liftweb.http.NotFoundResponse
import net.liftweb.http.rest.RestHelper

object NpmRest extends RestHelper {
  serve {
    case "repo" :: "npm" :: _ Get _ => {
      NotFoundResponse()
    }
  }
}
