package com.crawljax.web.jaxrs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.crawljax.web.db.dao.PersonDao;
import com.crawljax.web.db.model.Person;
import com.google.inject.persist.Transactional;

@Path("rest/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

	private PersonDao personDao;

	@Inject
	public PersonResource(PersonDao personDao) {
		this.personDao = personDao;
	}

	@GET
	public List<Person> getPersons() {
		return personDao.findAll();
	}

	@GET
	@Path("add")
	@Transactional
	public Person add(@QueryParam("first") String firstName, @QueryParam("last") String lastName) {
		checkArgument(!isNullOrEmpty(firstName), "Provide a firstname");
		checkArgument(!isNullOrEmpty(lastName), "Provide a lastname");

		Person newPerson = new Person(firstName, lastName);
		personDao.persist(newPerson);
		return newPerson;
	}
}
