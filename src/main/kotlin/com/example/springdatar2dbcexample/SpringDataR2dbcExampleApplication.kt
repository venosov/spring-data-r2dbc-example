@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package com.example.springdatar2dbcexample

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.domain.Persistable
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@SpringBootApplication
@EnableR2dbcRepositories(considerNestedRepositories = true)
class SpringDataR2dbcExampleApplication() {
	@Bean
	fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
		val initializer = ConnectionFactoryInitializer()
		initializer.setConnectionFactory(connectionFactory)
		initializer
				.setDatabasePopulator(CompositeDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("schema.sql"), ClassPathResource("data.sql"))))
		return initializer
	}

	@RestController
	class PersonController(val personRepository: PersonRepository,
						   val personEventRepository: PersonEventRepository) {
		@GetMapping("/")
		fun findAll(): Flux<Person> {
			return personRepository.findAll()
		}

		@GetMapping("events")
		fun findAllEvents(): Flux<PersonEvent> {
			return personEventRepository.findAll()
		}

		@GetMapping("by-name/{lastName}")
		fun findAllByLastName(@PathVariable lastName: String): Flux<Person> {
			return personRepository.findAllByLastName(lastName)
		}

		@PostMapping(value = ["create/{firstName}/{lastName}"])
		fun create(@PathVariable firstName: String, @PathVariable lastName: String): Mono<Void> {
			val person = Person(firstName, lastName)
			val event = PersonEvent(firstName, lastName, "CREATED")

			return personRepository.save(person)
					.then(personEventRepository.save(event).then())
		}
	}

	interface PersonRepository : ReactiveCrudRepository<Person, Int> {
		@Query("SELECT * FROM person WHERE last_name = :lastname")
		fun findAllByLastName(lastname: String): Flux<Person>
	}

	interface PersonEventRepository : ReactiveCrudRepository<PersonEvent, Int>

	@Table
	data class Person(val firstName: String, val lastName: String) {
		@Id
		var id: Int? = null
	}

	@Table
	class PersonEvent : Persistable<Int> {
		@Id
		private var identifier: Int? = null
		var firstName: String? = null
		var lastName: String? = null
		var action: String? = null

		@PersistenceConstructor
		constructor(id: Int?, firstName: String?, lastName: String?, action: String) {
			this.identifier = id
			this.firstName = firstName
			this.lastName = lastName
			this.action = action
		}

		constructor(firstName: String?, lastName: String?, action: String) {
			this.firstName = firstName
			this.lastName = lastName
			this.action = action
		}

		override fun isNew(): Boolean {
			return true
		}

		override fun getId(): Int? {
			return id
		}
	}
}

fun main(args: Array<String>) {
	runApplication<SpringDataR2dbcExampleApplication>(*args)
}
