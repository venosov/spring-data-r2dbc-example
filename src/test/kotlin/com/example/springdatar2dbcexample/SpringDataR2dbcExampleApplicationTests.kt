package com.example.springdatar2dbcexample

import com.example.springdatar2dbcexample.SpringDataR2dbcExampleApplication.*
import com.example.springdatar2dbcexample.SpringDataR2dbcExampleApplication.PersonEvent
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import reactor.test.StepVerifier.FirstStep


@SpringBootTest
class SpringDataR2dbcExampleApplicationTests(
		@Autowired private val personRepository: PersonRepository,
		@Autowired private val personEventRepository: PersonEventRepository) {
	@Test
	fun findAll() {
		val client = WebTestClient.bindToController(PersonController(personRepository, personEventRepository)).build()
		client.get()
				.uri("/")
				.exchange()
				.expectStatus()
				.isOk
				.returnResult(Person::class.java)
				.responseBody
				.`as`<FirstStep<Person>> { publisher: Flux<Person> -> StepVerifier.create(publisher) } //
				.assertNext { (firstName) -> firstName == "Walter" } //
				.assertNext { (firstName) -> firstName == "Jesse" } //
				.assertNext { (firstName) -> firstName == "Hank" } //
				.verifyComplete()
	}

	@Test
	fun findAllEvents() {
		val client = WebTestClient.bindToController(PersonController(personRepository, personEventRepository)).build()
		client.get()
				.uri("/events")
				.exchange()
				.expectStatus()
				.isOk
				.returnResult(PersonEvent::class.java)
				.responseBody
				.`as`<FirstStep<PersonEvent>> { publisher: Flux<PersonEvent> -> StepVerifier.create(publisher) } //
				.verifyComplete()
	}

	@Test
	fun findAllByLastName() {
		val client = WebTestClient.bindToController(PersonController(personRepository, personEventRepository)).build()
		client.get()
				.uri("/by-name/White")
				.exchange()
				.expectStatus()
				.isOk
				.returnResult(Person::class.java)
				.responseBody
				.`as`<FirstStep<Person>> { publisher: Flux<Person> -> StepVerifier.create(publisher) } //
				.assertNext { (firstName) -> firstName == "Walter" } //
				.verifyComplete()
	}

}
