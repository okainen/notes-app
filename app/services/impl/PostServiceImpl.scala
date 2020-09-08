package services.impl

import daos.PostDao
import exceptions.Exceptions.NotFoundException
import helpers.{TimeHelper, UuidHelper}
import io.jvm.uuid.UUID
import models.Post
import models.dtos.PostDto
import monix.eval.Task
import services.PostService


class PostServiceImpl(postDao: PostDao) extends PostService(postDao) {
  def create(userId: UUID, postDto: PostDto)(implicit uuidHelper: UuidHelper): Task[Post] = {
    val post = postDto.toPost(userId)
    postDao.create(post).map { _ =>
      post
    }
  }

  def get(userId: UUID, id: UUID): Task[Post] =
    withExistingAndOwned(userId, id) { post =>
      Task.now(post)
    }

  def getAll(userId: UUID): Task[Seq[Post]] = postDao.getAll(userId)

  def update(userId: UUID, id: UUID, postDto: PostDto)(implicit timeHelper: TimeHelper): Task[Post] =
    withExistingAndOwned(userId, id) { post =>
      val updatedPost = post.update(postDto)
      postDao.update(updatedPost).map { _ =>
        updatedPost
      }
    }

  def delete(userId: UUID, id: UUID): Task[Unit] =
    withExistingAndOwned(userId, id) { post =>
      postDao.delete(post.id)
    }
  
  private def withExistingAndOwned[A](userId: UUID, id: UUID)(block: Post => Task[A]): Task[A] = {
    postDao.get(id).flatMap {
      case Some(post) =>
        if (post.userId == userId) block(post)
        else Task.raiseError(NotFoundException)
      case None => Task.raiseError(NotFoundException)
    }
  }

}
