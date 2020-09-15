package controllers

import exceptions.Exceptions.NotFoundException
import helpers.{TimeHelper, UuidHelper}
import io.jvm.uuid.UUID
import models.Post
import models.dtos.PostDto
import monix.eval.Task
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.PostService
import testUtils.TestUtils

class PostControllerTest extends PlaySpec with MockFactory with TestUtils {
  val serviceMock: PostService = mock[PostService]
  val controller = new PostController(serviceMock, stubControllerComponents())

  def fakePostRequest(title: String, content: String): FakeRequest[AnyContent] = {
    val jsonBody = Json.obj(
      "title" -> title,
      "content" -> content
    )
    FakeRequest().withBody(AnyContent(jsonBody))
  }

  val correctPostRequest: FakeRequest[AnyContent] =
    fakePostRequest("Hello", "World!")
  val correctDto: PostDto = PostDto.form.bindFromRequest()(correctPostRequest).value.get
  val badRequest: FakeRequest[AnyContent] = fakePostRequest("", "")
  val formErrors: Seq[FormError] = PostDto.form.bindFromRequest()(badRequest).errors
  val post: Post = correctDto.toPost(userId)

  "create" should {

    "return Created" in {
      (serviceMock.create(_: UUID, _: PostDto)(_: UuidHelper))
        .expects(userId, correctDto, *) returns Task.now(post)
      controller.create(correctPostRequest.authorized).await mustBe Created(post.toJson)
    }

    "return Unauthorized" in {
      controller.create(correctPostRequest).await mustBe Unauthorized
    }

    "return BadRequest(errors)" in {
      controller.create(badRequest.authorized).await mustBe
        BadRequest(Json.obj(
          "errors" -> formErrors.map(e => Json.obj(e.key -> e.message))
        ))
    }
  }

  "get" should {
    "return Ok(post)" in {
      (serviceMock.get(_: UUID, _: UUID))
        .expects(userId, correctResourceId) returns Task.now(post)
      controller.get(correctResourceId.toString)(FakeRequest().authorized).await mustBe Ok(post.toJson)
    }

    "return Unauthorized" in {
      controller.get(correctResourceId.toString)(FakeRequest()).await mustBe Unauthorized
    }

    "return NotFound" in {
      controller.get(incorrectResourceId)(FakeRequest().authorized).await mustBe NotFound
    }
  }

  "getAll" should {
    "return Ok(posts)" in {
      (serviceMock.getAll(_: UUID)).expects(userId) returns Task.now(Seq(post))
      controller.getAll()(FakeRequest().authorized).await mustBe Ok(Json.obj(
        "posts" -> Seq(post)
      ))
    }

    "return Unauthorized" in {
      controller.getAll()(FakeRequest()).await mustBe Unauthorized
    }
  }

  "update" should {
    "return NoContent" in {
      (serviceMock.update(_: UUID, _: UUID, _: PostDto)(_: TimeHelper))
        .expects(userId, correctResourceId, correctDto, *) returns Task.now(post)
      controller.update(correctResourceId.toString)(correctPostRequest.authorized).await mustBe Ok(post.toJson)
    }

    "return Unauthorized" in {
      controller.update(correctResourceId.toString)(correctPostRequest).await mustBe Unauthorized
    }

    "return NotFound" in {
      controller.update(incorrectResourceId)(correctPostRequest.authorized).await mustBe NotFound
    }

    "return NotFound if resource with specified id exists but not owned by user" in {
      (serviceMock.update(_: UUID, _: UUID, _: PostDto)(_: TimeHelper))
        .expects(userId, correctResourceId, correctDto, *) returns Task.raiseError(NotFoundException)
      controller.update(correctResourceId.toString)(correctPostRequest.authorized).await mustBe NotFound
    }
  }

  "delete" should {
    "return NoContent" in {
      (serviceMock.delete(_: UUID, _: UUID))
        .expects(userId, correctResourceId) returns Task.now(())
      controller.delete(correctResourceId.toString)(FakeRequest().authorized).await mustBe NoContent
    }

    "return Unauthorized" in {
      controller.delete(correctResourceId.toString)(FakeRequest()).await mustBe Unauthorized
    }

    "return NotFound" in {
      controller.delete(incorrectResourceId)(FakeRequest().authorized).await mustBe NotFound
    }

    "return NotFound if resource with specified id exists but not owned by user" in {
      (serviceMock.delete(_: UUID, _: UUID))
        .expects(userId, correctResourceId) returns Task.raiseError(NotFoundException)
      controller.delete(correctResourceId.toString)(FakeRequest().authorized).await mustBe NotFound
    }
  }
}
