package services.impl

import java.time.Instant

import exceptions.Exceptions.NotFoundException
import io.jvm.uuid.UUID
import models.dtos.PostDto
import monix.eval.Task
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import daos.PostDao
import helpers.{TimeHelper, UuidHelper}
import models.Post
import testUtils.TestUtils

class PostServiceImplTest extends PlaySpec with MockFactory with TestUtils {
  protected implicit val timeHelper: TimeHelper = () => Instant.EPOCH
  protected implicit val uuidHelper: UuidHelper = () => UUID(0, 0)
  protected val postDaoMock: PostDao = mock[PostDao]
  protected val service = new PostServiceImpl(postDaoMock)
  protected val postDto: PostDto = PostDto("title", "content")
  protected val postDtoMock: PostDto = mock[PostDto]
  protected val post: Post = postDto.toPost(userId)

  "create" should {
    "create post and return (): Unit" in {
      (postDaoMock.create(_: Post)) expects post returns Task.now(post)
      (postDtoMock.toPost(_: UUID)(_: TimeHelper, _: UuidHelper))
        .expects(userId, *, *) returns post
      service.create(userId, postDtoMock).await mustBe post
    }
  }

  "get" should {
    "return post" in {
      (postDaoMock.get(_: UUID)) expects post.id returns Task.now(Some(post))
      service.get(userId, post.id).await mustBe post
    }

    "raise NotFoundException if resource exists but not owned by user or does not exist at all" in {
      (postDaoMock.get(_: UUID)) expects post.id returns Task.now(None)
      a[NotFoundException.type] should be thrownBy service.get(anotherUserId, post.id).await
    }
  }

  "getAll" should {
    "return posts" in {
      (postDaoMock.getAll(_: UUID)) expects userId returns Task.now(Seq(post))
      service.getAll(userId).await mustBe Seq(post)
    }
  }

  "update" should {

    "update post and return (): Unit" in {
      (postDaoMock.get(_: UUID)) expects post.id returns Task.now(Some(post))
      (postDaoMock.update(_: Post)) expects post returns Task.now(post)
      service.update(userId, post.id, postDto).await mustBe post
    }

    "raise NotFoundException if resource does not exist" in {
      val nonExistentId = UUID(0, 1)
      (postDaoMock.get(_: UUID)) expects nonExistentId returns Task.now(None)
      a[NotFoundException.type] should be thrownBy service.update(userId, nonExistentId, postDto).await
    }

    "raise NotFoundException if resource exists but not owned by user" in {
      (postDaoMock.get(_: UUID)) expects post.id returns Task.now(Some(post))
      a[NotFoundException.type] should be thrownBy service.update(anotherUserId, post.id, postDto).await
    }
  }

  "delete" should {

    "delete post and return (): Unit" in {
      (postDaoMock.get(_: UUID)) expects post.id returns Task.now(Some(post))
      (postDaoMock.delete(_: UUID)) expects post.id returns Task.now(())
      service.delete(userId, post.id).await mustBe ()
    }

    "raise NotFoundException if resource exists but not owned by user or does not exist" in {
      (postDaoMock.get(_: UUID)) expects post.id returns Task.now(Some(post))
      a[NotFoundException.type] should be thrownBy service.delete(anotherUserId, post.id).await
    }
  }
}
