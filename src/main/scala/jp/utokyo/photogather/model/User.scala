package jp.utokyo.photogather.model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.SessionVar

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with LongKeyedMetaMapper[User] with CRUDify[Long, User]{

  private object _currentUser extends SessionVar[Option[User]](None)

  def currentUser = _currentUser.is

  def currentUserId = _currentUser.is.get.id.is

  def loggedIn_? = currentUser.isDefined

  def logUserIn(u : User) = {
    _currentUser.set(Some(u))
  }

}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User extends LongKeyedMapper[User] with IdPK with OneToMany[Long,User] {
  def getSingleton = User // what's the "meta" server


  object username extends MappedString(this,100){
    override def uniqueFieldId = Full("UNK_USERNAME")
  }
  object nickname extends MappedString(this,100)

  object password extends MappedString(this,100)


  object registered extends MappedDateTime(this)
  object lastActive extends MappedDateTime(this)


  object photos extends MappedOneToMany(Photo,Photo.user,OrderBy(Photo.uploaded,Ascending))



}


