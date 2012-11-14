package jp.utokyo.photogather.model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.{RequestVar, SessionVar}

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with LongKeyedMetaMapper[User] with CRUDify[Long, User]{

  private object _currentUserId extends SessionVar[Option[Long]](None)
  private object _currentUser extends RequestVar[Option[User]](None)

  def currentUser = _currentUser getOrElse {
    val user = User.findByKey(_currentUserId.is.get)
    _currentUser.set(Some(user.open_!))
    user.open_!
  }

  def currentUserId = _currentUserId.is.get

  def loggedIn_? = _currentUserId.is.isDefined

  def logUserIn(u : User) = {
    _currentUserId.set(Some(u.id.is))
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


