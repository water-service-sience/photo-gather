package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import js.jquery.JQueryArtifacts
import sitemap._
import Loc._
import mapper._

import jp.utokyo.photogather.model._
import jp.utokyo.photogather.stateless.{LoginHandler, APIHandler, JsonHandler, PhotoHandler}
import java.net.URLEncoder


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:data/photogather.db",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }
    
    
    
    val models = List(User,Photo)


    def ifLoggedIn = If( () => S.loggedIn_?, () => {

      val req = S.request.open_!.request
      val _url = req.uri + req.queryString.map(q => "?" + q).openOr("")
      val url = if(req.contextPath != null && req.contextPath.length > 0){
        _url.substring(req.contextPath.length + 1)
      }else _url

      RedirectResponse("/sign_in?from=" + URLEncoder.encode(url,"utf-8"))
    })

    def ifNotLoggedIn = Test(r => !User.loggedIn_?)

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _,
      models :_*)

    // where to search snippet
    LiftRules.addToPackages("jp.utokyo.photogather")

    def LoginLoc( name : String, path : Link[Unit]) = {
      Loc(name  , path , name,   ifLoggedIn)
    }

    LiftRules.statelessDispatchTable.append(JsonHandler)
    LiftRules.statelessDispatchTable.append(PhotoHandler)
    LiftRules.statelessDispatchTable.append(APIHandler)
    LiftRules.dispatch.append(LoginHandler)

    // Build SiteMap
    def sitemap = SiteMap(
      Menu(LoginLoc("Home"   , "index" :: Nil  )) :: // the simple way to declare a menu
      Menu(Loc("SignIn" , "sign_in" :: Nil, "Sign in page" , Hidden)) ::
      Menu(Loc("SignUp" , "sign_up" :: Nil, "Sign up page" , Hidden)) ::
        Menu(LoginLoc("UploadPhoto"   , "photo" :: "upload" :: Nil  )) ::
        Menu(LoginLoc("PhotoList"   , "photo" :: "upload_list" :: Nil  )) ::
        Menu(LoginLoc("PhotoEdit"   , "photo" :: "photo_edit" :: Nil  )) ::
        Menu(LoginLoc("PhotoMap"   , "photo" :: "uploaded_map" :: Nil  )) ::
        Menu(LoginLoc("PhotoOnCalendar"   , "photo" :: "photo_on_calendar" :: Nil  )) ::
        Menu(LoginLoc("PhotoInDay"   , "photo" :: "photo_day" :: Nil  )) ::

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")) ::
      models.flatMap(_.menus) :_*)


    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemap)

    // Use jQuery 1.4
    //LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts


    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
  }
}
